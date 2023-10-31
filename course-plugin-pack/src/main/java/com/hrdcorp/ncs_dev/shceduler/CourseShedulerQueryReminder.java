package com.hrdcorp.ncs_dev.shceduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;

import com.hrdcorp.ncs_dev.util.AuditTrail;
import com.hrdcorp.ncs_dev.util.SendEmail;

public class CourseShedulerQueryReminder extends DefaultApplicationPlugin{
    @Override
    public String getName() {
        return ("HRDC - COURSE - Scheduler Query Reminder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To remind TP when there is any query based on scheduler date");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Scheduler Query Reminder");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/NotifyCourseQueryPluginForm.json", null, true);
    }
    
    @Override
    public Object execute(Map props) {
        LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Start scheduler");
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
                     
        try {

            String action_review_status = props.get("action_review_status").toString();
            String interval = props.get("interval").toString();
            String interval_unit = props.get("interval_unit").toString();
            int max_reminder = Integer.parseInt(props.get("max_reminder").toString());

            // Establish a database connection
            Connection con = ds.getConnection();
            try {
                
                String sql = "SELECT c.id, c.c_course_name as docName, c.c_course_id as docId, c.c_action_query_reason, c.dateModified, c.c_query_notify_counter, u.email, CONCAT(u.firstname,\" \", u.lastname) as longname, w.processId "
                            + "FROM app_fd_course_register c "
                            + "LEFT JOIN dir_user u ON c.createdBy = u.username "
                            + "LEFT JOIN wf_process_link w ON w.originProcessId = c.id "
                            + "WHERE dateModified < DATE_ADD(NOW(), INTERVAL -"+interval+" "+interval_unit+" ) "
                            + "AND c_action_review_status = '"+action_review_status+"' "
                            + "UNION "
                            + "SELECT l.id, l.c_name as docName, l.c_ltm_id as docId, l.c_action_query_reason, l.dateModified, l.c_query_notify_counter, u.email, CONCAT(u.firstname,\" \", u.lastname) as longname, w.processId "
                            + "FROM app_fd_course_ltm l "
                            + "LEFT JOIN dir_user u ON l.createdBy = u.username "
                            + "LEFT JOIN wf_process_link w ON w.originProcessId = l.id "
                            + "WHERE dateModified < DATE_ADD(NOW(), INTERVAL -"+interval+" "+interval_unit+" ) "
                            + "AND c_action_review_status = '"+action_review_status+"' ";

                PreparedStatement stmt= con.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();

                if(rs.next()){
                    String uuid;
                    String user_name;
                    String user_email;
                    String doc_name;
                    String doc_id;
                    String query_reason;
                    String notify_counter;
                    String date_modified;
                    String processId;
                    int query_notify_counter;
                    String subject;
                    String msg;
                    String table;
                    LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","We have Result");

                    try{
                        do{
                            LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Loop start for: " +  rs.getString("id") +" - "+rs.getString("docName"));
                            uuid = rs.getString("id");
                            user_email = rs.getString("email");
                            user_name = rs.getString("longname");
                            doc_name = rs.getString("docName");
                            doc_id = rs.getString("docId");
                            query_reason = rs.getString("c_action_query_reason");
                            date_modified = rs.getString("dateModified");
                            processId = rs.getString("processId");
                            notify_counter = rs.getString("c_query_notify_counter") == null || rs.getString("c_query_notify_counter").isEmpty() ? "0" : rs.getString("c_query_notify_counter");
                            query_notify_counter = Integer.parseInt(notify_counter);
                            subject = processId.endsWith("course_register") || processId.endsWith("course_amendment")? "APPLICATION FOR CREATION OF EVENT BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB): "+doc_name+"":"APPLICATION FOR LICENSE TRAINING MATERIAL APPROVAL: "+doc_name+"";
                            table = processId.endsWith("course_register") || processId.endsWith("course_amendment")? "app_fd_course_register":"app_fd_course_ltm";
                            msg = emailBody(processId, uuid, user_name, user_email, doc_name, doc_id, query_reason, date_modified);

                            try{
                                LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Check notified how many times");
                                if(query_notify_counter < max_reminder){
                                    LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Sending reminder. Reminder number "+query_notify_counter+" + 1, out of "+ max_reminder +" times.");
                                    SendEmail.sendEmail("Scheduler Notify Query Reminder", user_email, "", subject, msg);
                                    try{
                                            LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Try updating notify counter");
                                            int new_notify_counter = query_notify_counter +1;
                                            String sql2 = "UPDATE "+table+" " +
                                                        "SET c_query_notify_counter = '"+new_notify_counter+"' " +
                                                        "WHERE " +
                                                        "id = '"+uuid+"'";

                                            PreparedStatement stmt2 = con.prepareStatement(sql2);
                                            
                                            stmt2.executeQuery();

                                            LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Update counter successful");
                                            
                                    }catch(Exception ex){
                                        LogUtil.error("HRDC - COURSE - Scheduler Notify Query Reminder ---->",ex,"Update notify counter unsuccessful");
                                    }
                                }else{
                                    LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Terminating flow. Reminded "+query_notify_counter+" out of "+max_reminder+" times.");

                                    try{
                                        LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Try Teminating flow/process"); 

                                        if (processId != null && !processId.isEmpty()) {
                                            FormUtil.abortRunningProcessForRecord(processId);
                                        }else{
                                                LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Flow/process not exist"); 
                                        }

                                    }catch (Exception ex){
                                        LogUtil.error("HRDC - COURSE - Scheduler Notify Query Reminder ---->", ex, "Fail to end process");
                                    }
                                    
                                    try{                 
                                        LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","Updating status to Rejected");
                                        String sql4 = "UPDATE "+table+" " +
                                                    "SET c_status = 'Rejected', " +
                                                    "c_action_review_status = 'Rejected', " +
                                                    "c_action_remarks = 'Rejected by system', " +
                                                    "c_review_status = 'Rejected', " +
                                                    "c_action_name = 'System', " +
                                                    "modifiedByName = 'System', " +
                                                    "dateModified = NOW() " +
                                                    "WHERE " +
                                                    "id = '"+uuid+"' ";

                                        PreparedStatement stmt4 = con.prepareStatement(sql4);
                                        stmt4.executeQuery();

                                        AuditTrail.addAuditTrail("Scheduler Notify Query Reminder","autoreject", processId, uuid, con);
                                        String msgQuery = emailBodyQuery(processId, uuid, user_name, user_email, doc_name, doc_id, query_reason, date_modified);
                                        SendEmail.sendEmail("Scheduler Notify Query Reminder", user_email, "", subject, msgQuery);

                                    }catch(Exception ex){
                                        LogUtil.error("HRDC - COURSE - Scheduler Notify Query Reminder ---->", ex, "Fail updating status to Rejected");
                                    }
                                }
                                
                            }catch(Exception ex){
                                LogUtil.error("HRDC - COURSE - Scheduler Notify Query Reminder ---->",ex,"Fail trying to send email to: "+"("+user_name+") - "+user_email);
                            }

                        }while(rs.next());
                    }catch(Exception ex){
                        LogUtil.error("HRDC - COURSE - Scheduler Notify Query Reminder ---->",ex,"Error looping");
                    }finally{
                        con.close();
                    }
                    
                }else{
                    LogUtil.info("HRDC - COURSE - Scheduler Notify Query Reminder ---->","We don't have Result");
                }

            }catch(SQLException e){
                LogUtil.info("HRDC - COURSE - Scheduler Query Reminder ---->","Fail Updating");
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String emailBody (String processId, String uuid, String user_name, String user_email, String doc_name, String doc_id, String query_reason, String date_modified) {
        String msg;
        if(processId.endsWith("course_register") || processId.endsWith("course_amendment")){
            msg = "<!DOCTYPE html>\r\n" + //
                "<html lang=\"en\">\r\n" + //
                "<head>\r\n" + //
                "    <meta charset=\"UTF-8\">\r\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                "    <style>\r\n" + //
                "        body {\r\n" + //
                "            font-family: Arial, sans-serif;\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            background-color: #f5f5f5;\r\n" + //
                "        }\r\n" + //
                "        .container {\r\n" + //
                "            max-width: 600px;\r\n" + //
                "            margin: 0 auto;\r\n" + //
                "            padding: 20px;\r\n" + //
                "            background-color: #ffffff;\r\n" + //
                "            border: 1px solid #ccc;\r\n" + //
                "            border-radius: 5px;\r\n" + //
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\r\n" + //
                "        }\r\n" + //
                "        h1 {\r\n" + //
                "            color: #d9534f;\r\n" + //
                "            margin: 0 0 20px;\r\n" + //
                "        }\r\n" + //
                "        p {\r\n" + //
                "            color: #333;\r\n" + //
                "            margin: 20px 0;\r\n" + //
                "            margin-bottom: 10px; \r\n" + //
                "            text-align: justify;\r\n" + //
                "        }\r\n" + //
                "        .signature {\r\n" + //
                "            margin-top: 20px;\r\n" + //
                "            color: #666;\r\n" + //
                "        }\r\n" + //
                "        .footer {\r\n" + //
                "            margin-top: 30px;\r\n" + //
                "            text-align: center;\r\n" + //
                "            color: #999;\r\n" + //
                "        }\r\n" + //
                "      .bold{\r\n" + //
                "        font-weight: bold;\r\n" + //
                "      }\r\n" + //
                "      \r\n" + //
                "    </style>\r\n" + //
                "</head>\r\n" + //
                "<body>\r\n" + //
                "    <div class=\"container\">\r\n" + //
                "        <h1 style=\"text-align: center\">REMINDER TO REPLY QUERY - Application for Registration of Training Course(s) "+doc_id+"</h1>\r\n" + //
                "        <p><span class=\"bold\">MANAGING DIRECTOR</span><br>\r\n" + //
                "        #ADDRESS1#<br>\r\n" + //
                "        #ADDRESS2#<br>\r\n" + //
                "        #ADDRESS3#<br>\r\n" + //
                "        #ADDRESS4#<br>\r\n" + //
                "        #POSTCODE# #CITY#,<br>\r\n" + //
                "        #STATE FULL NAME#, #COUNTRY#.</p>\r\n" + //
                "        \r\n" + //
                "        <p>Dear Sir/Madam,</p>\r\n" + //
                "        \r\n" + //
                "        <p class=\"bold\">APPLICATION FOR REGISTRATION OF TRAINING COURSE(S) UNDER HRD CORP CLAIMABLE COURSES SCHEME BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)</p>\r\n" + //
                "        \r\n" + //
                "        <p>Please be informed that your Training Programme application has been QUERIED by PSMB.</p>\r\n" + //
                "                \r\n" + //
                "        <table>\r\n" + //
                "            <tr>\r\n" + //
                "                <td>Query Reason:</td>\r\n" + //
                "                <td><span class=\"bold\">"+query_reason+"</span></td>\r\n" + //
                "            </tr>\r\n" + //
                "            <tr>\r\n" + //
                "                <td>Course ID:</td>\r\n" + //
                "                <td><span class=\"bold\">"+doc_id+"</span></td>\r\n" + //
                "            </tr>\r\n" + //
                "            <tr>\r\n" + //
                "                <td>Query Date:</td>\r\n" + //
                "                <td><span class=\"bold\">"+date_modified+"</span></td>\r\n" + //
                "            </tr>\r\n" + //
                "          \r\n" + //
                "        </table>\r\n" + //
                "        \r\n" + //
                "        <p>1. <span class=\"bold\">"+doc_name+"</span></p>\r\n" + //
                "      \r\n" + //
                "      <p>* <span class=\"bold\">"+query_reason+"</span></p>\r\n" + //
                "                \r\n" + //
                "        <p>Should you require further assistance, please contact our support team at 1 800 88 4800 or email support@hrdcorp.gov.my.</p>\r\n" + //
                "        \r\n" + //
                "        <p>Thank You.</p>\r\n" + //
                "        \r\n" + //
                "        <p>Regards,</p>\r\n" + //
                "        <p class=\"signature\">\r\n" + //
                "            RIVALDO JUING ANAK UNING<br>\r\n" + //
                "            for Chief Executive<br>\r\n" + //
                "            Pembangunan Sumber Manusia Berhad\r\n" + //
                "        </p>\r\n" + //
                "        \r\n" + //
                "        <p>This is a computer-generated email and does not require a signature.</p>\r\n" + //
                "    </div>\r\n" + //
                "\r\n" + //
                "    <div class=\"footer\">\r\n" + //
                "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\r\n" + //
                "    </div>\r\n" + //
                "</body>";
        }else{
            msg = "<!DOCTYPE html>\r\n" + //
                "<html lang=\"en\">\r\n" + //
                "<head>\r\n" + //
                "    <meta charset=\"UTF-8\">\r\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                "    <style>\r\n" + //
                "        body {\r\n" + //
                "            font-family: Arial, sans-serif;\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            background-color: #f5f5f5;\r\n" + //
                "        }\r\n" + //
                "        .container {\r\n" + //
                "            max-width: 600px;\r\n" + //
                "            margin: 0 auto;\r\n" + //
                "            padding: 20px;\r\n" + //
                "            background-color: #ffffff;\r\n" + //
                "            border: 1px solid #ccc;\r\n" + //
                "            border-radius: 5px;\r\n" + //
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\r\n" + //
                "        }\r\n" + //
                "        h1 {\r\n" + //
                "            color: #d9534f;\r\n" + //
                "            margin: 0 0 20px;\r\n" + //
                "        }\r\n" + //
                "        p {\r\n" + //
                "            color: #333;\r\n" + //
                "            margin: 20px 0;\r\n" + //
                "            margin-bottom: 10px; \r\n" + //
                "            text-align: justify;\r\n" + //
                "        }\r\n" + //
                "        .signature {\r\n" + //
                "            margin-top: 20px;\r\n" + //
                "            color: #666;\r\n" + //
                "        }\r\n" + //
                "        .footer {\r\n" + //
                "            margin-top: 30px;\r\n" + //
                "            text-align: center;\r\n" + //
                "            color: #999;\r\n" + //
                "        }\r\n" + //
                "      .bold{\r\n" + //
                "        font-weight: bold;\r\n" + //
                "      }\r\n" + //
                "      \r\n" + //
                "    </style>\r\n" + //
                "</head>\r\n" + //
                "<body>\r\n" + //
                "    <div class=\"container\">\r\n" + //
                "        <h1 style=\"text-align: center\">REMINDER TO REPLY QUERY - Application for License Training Material (LTM) "+doc_id+"</h1>\r\n" + //
                "        <p><span class=\"bold\">MANAGING DIRECTOR</span><br>\r\n" + //
                "        #ADDRESS1#<br>\r\n" + //
                "        #ADDRESS2#<br>\r\n" + //
                "        #ADDRESS3#<br>\r\n" + //
                "        #ADDRESS4#<br>\r\n" + //
                "        #POSTCODE# #CITY#,<br>\r\n" + //
                "        #STATE FULL NAME#, #COUNTRY#.</p>\r\n" + //
                "        \r\n" + //
                "        <p>Dear Sir/Madam,</p>\r\n" + //
                "        \r\n" + //
                "        <p class=\"bold\">APPLICATION FOR REGISTRATION OF LICENSE TRAINING MATERIAL UNDER HRD CORP CLAIMABLE COURSES SCHEME BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)</p>\r\n" + //
                "        \r\n" + //
                "        <p>Please be informed that your License Training Material has been QUERIED by PSMB.</p>\r\n" + //
                "                \r\n" + //
                "        <table>\r\n" + //
                "            <tr>\r\n" + //
                "                <td>Query Reason:</td>\r\n" + //
                "                <td><span class=\"bold\">"+query_reason+"</span></td>\r\n" + //
                "            </tr>\r\n" + //
                "            <tr>\r\n" + //
                "                <td>Course ID:</td>\r\n" + //
                "                <td><span class=\"bold\">"+doc_id+"</span></td>\r\n" + //
                "            </tr>\r\n" + //
                "            <tr>\r\n" + //
                "                <td>Query Date:</td>\r\n" + //
                "                <td><span class=\"bold\">"+date_modified+"</span></td>\r\n" + //
                "            </tr>\r\n" + //
                "          \r\n" + //
                "        </table>\r\n" + //
                "        \r\n" + //
                "        <p>1. <span class=\"bold\">"+doc_name+"</span></p>\r\n" + //
                "      \r\n" + //
                "      <p>* <span class=\"bold\">"+query_reason+"</span></p>\r\n" + //
                "                \r\n" + //
                "        <p>Should you require further assistance, please contact our support team at 1 800 88 4800 or email support@hrdcorp.gov.my.</p>\r\n" + //
                "        \r\n" + //
                "        <p>Thank You.</p>\r\n" + //
                "        \r\n" + //
                "        <p>Regards,</p>\r\n" + //
                "        <p class=\"signature\">\r\n" + //
                "            RIVALDO JUING ANAK UNING<br>\r\n" + //
                "            for Chief Executive<br>\r\n" + //
                "            Pembangunan Sumber Manusia Berhad\r\n" + //
                "        </p>\r\n" + //
                "        \r\n" + //
                "        <p>This is a computer-generated email and does not require a signature.</p>\r\n" + //
                "    </div>\r\n" + //
                "\r\n" + //
                "    <div class=\"footer\">\r\n" + //
                "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\r\n" + //
                "    </div>\r\n" + //
                "</body>";
        }

            return msg;
    }
    
    public String emailBodyQuery (String processId, String uuid, String user_name, String user_email, String doc_name, String doc_id, String query_reason, String date_modified) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();
        String msg;
        if(processId.endsWith("course_register") || processId.endsWith("course_amendment")){
            msg = "<!DOCTYPE html>\r\n" + //
                    "<html lang=\"en\">\r\n" + //
                    "<head>\r\n" + //
                    "    <meta charset=\"UTF-8\">\r\n" + //
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                    "    <style>\r\n" + //
                    "        body {\r\n" + //
                    "            font-family: Arial, sans-serif;\r\n" + //
                    "            margin: 0;\r\n" + //
                    "            padding: 0;\r\n" + //
                    "            background-color: #f5f5f5;\r\n" + //
                    "        }\r\n" + //
                    "        .container {\r\n" + //
                    "            max-width: 600px;\r\n" + //
                    "            margin: 0 auto;\r\n" + //
                    "            padding: 20px;\r\n" + //
                    "            background-color: #ffffff;\r\n" + //
                    "            border: 1px solid #ccc;\r\n" + //
                    "            border-radius: 5px;\r\n" + //
                    "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\r\n" + //
                    "        }\r\n" + //
                    "        h1 {\r\n" + //
                    "            color: #d9534f;\r\n" + //
                    "            margin: 0 0 20px;\r\n" + //
                    "        }\r\n" + //
                    "        p {\r\n" + //
                    "            color: #333;\r\n" + //
                    "            margin: 20px 0;\r\n" + //
                    "            margin-bottom: 10px; \r\n" + //
                    "            text-align: justify;\r\n" + //
                    "        }\r\n" + //
                    "        .signature {\r\n" + //
                    "            margin-top: 20px;\r\n" + //
                    "            color: #666;\r\n" + //
                    "        }\r\n" + //
                    "        .footer {\r\n" + //
                    "            margin-top: 30px;\r\n" + //
                    "            text-align: center;\r\n" + //
                    "            color: #999;\r\n" + //
                    "        }\r\n" + //
                    "      .bold{\r\n" + //
                    "        font-weight: bold;\r\n" + //
                    "      }\r\n" + //
                    "      \r\n" + //
                    "    </style>\r\n" + //
                    "</head>\r\n" + //
                    "<body>\r\n" + //
                    "    <div class=\"container\">\r\n" + //
                    "        <h1 style=\"text-align: center\">REJECTED DUE TO NO RESPONSE - Application for Registration of Training Course(s) "+doc_name+"</h1>\r\n" + //
                    "        <p><span class=\"bold\">MANAGING DIRECTOR</span><br>\r\n" + //
                    "        #ADDRESS1#<br>\r\n" + //
                    "        #ADDRESS2#<br>\r\n" + //
                    "        #ADDRESS3#<br>\r\n" + //
                    "        #ADDRESS4#<br>\r\n" + //
                    "        #POSTCODE# #CITY#,<br>\r\n" + //
                    "        #STATE FULL NAME#, #COUNTRY#.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Dear Sir/Madam,</p>\r\n" + //
                    "        \r\n" + //
                    "        <p class=\"bold\">APPLICATION FOR REGISTRATION OF TRAINING COURSE(S) UNDER HRD CORP CLAIMABLE COURSES SCHEME BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>The above matter refers. Course ID No: <span class=\"bold\">"+doc_id+"</span></p>\r\n" + //
                    "        \r\n" + //
                    "        <p>2. PSMB has evaluated your proposed training course(s) to ensure that it meets the criteria of the HRD Corp claimable courses scheme. Below are details of your course:</p>\r\n" + //
                    "        \r\n" + //
                    "        <table>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>Course ID:</td>\r\n" + //
                    "                <td><span class=\"bold\">"+doc_name+"</span></td>\r\n" + //
                    "            </tr>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>Course Name:</td>\r\n" + //
                    "                <td><span class=\"bold\">"+doc_id+"</span></td>\r\n" + //
                    "            </tr>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>Reject Date:</td>\r\n" + //
                    "                <td><span class=\"bold\">"+dtf.format(now)+"</span></td>\r\n" + //
                    "            </tr>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>Trainers:</td>\r\n" + //
                    "                <td>\r\n" + //
                    "                    <span class=\"bold\">1) #TRAINER 1</span><br>\r\n" + //
                    "                    <span class=\"bold\">2) #TRAINER 2#</span>\r\n" + //
                    "                </td>\r\n" + //
                    "            </tr>\r\n" + //
                    "        </table>\r\n" + //
                    "        \r\n" + //
                    "        <p style=\"font-style: italic;\"><span class=\"bold\">Important Notice:</span> Conference program approval period is only valid until the end of the conference (one-off approval).</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>3. Training providers are only allowed to market approved training courses using the course title, and trainer(s) that have been registered under the HRD Corp claimable courses scheme. Any changes on the trainer need to receive prior approval from PSMB before running the course. Apart from this, training providers and trainers must also ensure that the quality of your training courses and delivery meets the expectations of the employers.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>4. Training Providers are advised to refer to the terms and conditions for HRD Corp claimable courses scheme implementation in the relevant Training Providers Circular / Employers Circular.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>5. Pembangunan Sumber Manusia Berhad (PSMB) is not responsible in terms of legal implications in the event of issues regarding intellectual property which defies the Copyright Protection Act.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Should you require further assistance, please contact our support team at 1 800 88 4800 or email support@hrdcorp.gov.my.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Thank You.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Regards,</p>\r\n" + //
                    "        <p class=\"signature\">\r\n" + //
                    "            RIVALDO JUING ANAK UNING<br>\r\n" + //
                    "            for Chief Executive<br>\r\n" + //
                    "            Pembangunan Sumber Manusia Berhad\r\n" + //
                    "        </p>\r\n" + //
                    "        \r\n" + //
                    "        <p>This is a computer-generated email and does not require a signature.</p>\r\n" + //
                    "    </div>\r\n" + //
                    "\r\n" + //
                    "    <div class=\"footer\">\r\n" + //
                    "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\r\n" + //
                    "    </div>\r\n" + //
                    "</body>";
        }else{
            msg = "<!DOCTYPE html>\r\n" + //
                    "<html lang=\"en\">\r\n" + //
                    "<head>\r\n" + //
                    "    <meta charset=\"UTF-8\">\r\n" + //
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                    "    <style>\r\n" + //
                    "        body {\r\n" + //
                    "            font-family: Arial, sans-serif;\r\n" + //
                    "            margin: 0;\r\n" + //
                    "            padding: 0;\r\n" + //
                    "            background-color: #f5f5f5;\r\n" + //
                    "        }\r\n" + //
                    "        .container {\r\n" + //
                    "            max-width: 600px;\r\n" + //
                    "            margin: 0 auto;\r\n" + //
                    "            padding: 20px;\r\n" + //
                    "            background-color: #ffffff;\r\n" + //
                    "            border: 1px solid #ccc;\r\n" + //
                    "            border-radius: 5px;\r\n" + //
                    "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\r\n" + //
                    "        }\r\n" + //
                    "        h1 {\r\n" + //
                    "            color: #d9534f;\r\n" + //
                    "            margin: 0 0 20px;\r\n" + //
                    "        }\r\n" + //
                    "        p {\r\n" + //
                    "            color: #333;\r\n" + //
                    "            margin: 20px 0;\r\n" + //
                    "            margin-bottom: 10px; \r\n" + //
                    "            text-align: justify;\r\n" + //
                    "        }\r\n" + //
                    "        .signature {\r\n" + //
                    "            margin-top: 20px;\r\n" + //
                    "            color: #666;\r\n" + //
                    "        }\r\n" + //
                    "        .footer {\r\n" + //
                    "            margin-top: 30px;\r\n" + //
                    "            text-align: center;\r\n" + //
                    "            color: #999;\r\n" + //
                    "        }\r\n" + //
                    "      .bold{\r\n" + //
                    "        font-weight: bold;\r\n" + //
                    "      }\r\n" + //
                    "      \r\n" + //
                    "    </style>\r\n" + //
                    "</head>\r\n" + //
                    "<body>\r\n" + //
                    "    <div class=\"container\">\r\n" + //
                    "        <h1 style=\"text-align: center\">REJECTED DUE TO NO RESPONSE - Application for License Training Material (LTM) "+doc_name+"</h1>\r\n" + //
                    "        <p><span class=\"bold\">MANAGING DIRECTOR</span><br>\r\n" + //
                    "        #ADDRESS1#<br>\r\n" + //
                    "        #ADDRESS2#<br>\r\n" + //
                    "        #ADDRESS3#<br>\r\n" + //
                    "        #ADDRESS4#<br>\r\n" + //
                    "        #POSTCODE# #CITY#,<br>\r\n" + //
                    "        #STATE FULL NAME#, #COUNTRY#.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Dear Sir/Madam,</p>\r\n" + //
                    "        \r\n" + //
                    "        <p class=\"bold\">APPLICATION FOR REGISTRATION OF LICENSE TRAINING MATERIAL UNDER HRD CORP CLAIMABLE COURSES SCHEME BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>The above matter refers. LTM ID No: <span class=\"bold\">"+doc_id+"</span></p>\r\n" + //
                    "        \r\n" + //
                    "        <p>2. PSMB has evaluated your proposed License Training Material to ensure that it meets the criteria of the HRD Corp claimable courses scheme. Below are details of your course:</p>\r\n" + //
                    "        \r\n" + //
                    "        <table>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>LTM ID:</td>\r\n" + //
                    "                <td><span class=\"bold\">"+doc_name+"</span></td>\r\n" + //
                    "            </tr>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>LTM Name:</td>\r\n" + //
                    "                <td><span class=\"bold\">"+doc_id+"</span></td>\r\n" + //
                    "            </tr>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>Reject Date:</td>\r\n" + //
                    "                <td><span class=\"bold\">"+dtf.format(now)+"</span></td>\r\n" + //
                    "            </tr>\r\n" + //
                    "            <tr>\r\n" + //
                    "                <td>Trainers:</td>\r\n" + //
                    "                <td>\r\n" + //
                    "                    <span class=\"bold\">1) #TRAINER 1</span><br>\r\n" + //
                    "                    <span class=\"bold\">2) #TRAINER 2#</span>\r\n" + //
                    "                </td>\r\n" + //
                    "            </tr>\r\n" + //
                    "        </table>\r\n" + //
                    "        \r\n" + //
                    "        <p style=\"font-style: italic;\"><span class=\"bold\">Important Notice:</span> Conference program approval period is only valid until the end of the conference (one-off approval).</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>3. Training providers are only allowed to market approved training courses using the course title, and trainer(s) that have been registered under the HRD Corp claimable courses scheme. Any changes on the trainer need to receive prior approval from PSMB before running the course. Apart from this, training providers and trainers must also ensure that the quality of your training courses and delivery meets the expectations of the employers.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>4. Training Providers are advised to refer to the terms and conditions for HRD Corp claimable courses scheme implementation in the relevant Training Providers Circular / Employers Circular.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>5. Pembangunan Sumber Manusia Berhad (PSMB) is not responsible in terms of legal implications in the event of issues regarding intellectual property which defies the Copyright Protection Act.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Should you require further assistance, please contact our support team at 1 800 88 4800 or email support@hrdcorp.gov.my.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Thank You.</p>\r\n" + //
                    "        \r\n" + //
                    "        <p>Regards,</p>\r\n" + //
                    "        <p class=\"signature\">\r\n" + //
                    "            RIVALDO JUING ANAK UNING<br>\r\n" + //
                    "            for Chief Executive<br>\r\n" + //
                    "            Pembangunan Sumber Manusia Berhad\r\n" + //
                    "        </p>\r\n" + //
                    "        \r\n" + //
                    "        <p>This is a computer-generated email and does not require a signature.</p>\r\n" + //
                    "    </div>\r\n" + //
                    "\r\n" + //
                    "    <div class=\"footer\">\r\n" + //
                    "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\r\n" + //
                    "    </div>\r\n" + //
                    "</body>";
        }
            return msg;
    }
}
