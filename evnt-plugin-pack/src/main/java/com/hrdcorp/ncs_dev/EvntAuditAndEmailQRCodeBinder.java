/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.lib.EmailTool;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.property.service.PropertyUtil;

/**
 *
 * @author farih
 */
public class EvntAuditAndEmailQRCodeBinder extends WorkflowFormBinder{
    
    public EvntAuditAndEmailQRCodeBinder getSuper() {
        return this;
    }
    
    Connection con = null;
    
    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {

        super.store(element, rows, formData);
        
        // Saving Audit Trail
        try{
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            FormRow row = rows.get(0);
            
            con = ds.getConnection();
            
            if(!con.isClosed()){              
                
                UuidGenerator uuid = UuidGenerator.getInstance();
                String pId = uuid.getUuid();
                String parentId = formData.getPrimaryKeyValue();
                
                String activityName = row.getProperty("action_activity");
                String workflowName = row.getProperty("action_workflow");
                                
                String username = AppUtil.processHashVariable("#currentUser.username#", null, null, null);
                String name = AppUtil.processHashVariable("#currentUser.firstName# #currentUser.lastName#", null, null, null);
                String department = AppUtil.processHashVariable("#currentUser.department.name#", null, null, null);
                
                String querySubject = row.getProperty("action_query_subject")!= null ?  row.getProperty("action_query_subject") : "";
                String queryAddEmail = row.getProperty("action_additional_email")!= null ? row.getProperty("action_additional_email") : "";
                String queryReason = row.getProperty("action_query_reason")!= null ? row.getProperty("action_additional_email") : "";
                String remarks = row.getProperty("action_remarks") != null ? row.getProperty("action_remarks") : ""; 
                String action_status = row.getProperty("status");
                String action_review_status = row.getProperty("action_review_status");
                
                String insertSql = "INSERT INTO app_fd_evnt_auditTrail (dateCreated,dateModified,c_action_date,id,createdBy,createdByName,modifiedBy,modifiedByName,c_action_workflow,c_action_activity,c_parentId,c_action_name,c_action_department,c_action_query_subject,c_action_additional_email,c_action_query_reason,c_action_remarks,c_status,c_action_review_status)"
                        + "VALUES (NOW(),NOW(),NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                 
                PreparedStatement stmtInsert = con.prepareStatement(insertSql);
                
                stmtInsert.setString(1, pId);
                stmtInsert.setString(2, username);
                stmtInsert.setString(3, name);
                stmtInsert.setString(4, username);
                stmtInsert.setString(5, name); 
                stmtInsert.setString(6, workflowName);
                stmtInsert.setString(7, activityName);
                stmtInsert.setString(8, parentId);
                stmtInsert.setString(9, name);
                stmtInsert.setString(10, department);
                stmtInsert.setString(11, querySubject);
                stmtInsert.setString(12, queryAddEmail);
                stmtInsert.setString(13, queryReason);
                stmtInsert.setString(14, remarks);
                stmtInsert.setString(15, action_status);
                stmtInsert.setString(16, action_review_status);
                
                //Execute SQL statement
                stmtInsert.executeUpdate();
            }
            
        }catch(Exception ex){
            
            LogUtil.error("Event Audit Trail", ex, "Error storing using jdbc");
            
        }finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error("Your App/Plugin Name", ex, "Error closing the jdbc connection");
            }
        }
            
        // Sending email
        try{
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            FormRow row = rows.get(0);

            String user_name;
            String user_email;
            String subject;
            String msg;
            
            String action_status = row.getProperty("status");
            
            LogUtil.info("HRDC EVENT Email Potential Participant ---->","Status should be Published. Status: "+ action_status);
            
             
            if("Published".equals(action_status)){
                
                LogUtil.info("HRDC EVENT Email Potential Participant ---->","Status is Published. Contiue with connection to DB");
                
                con = ds.getConnection(); 
                
                if(!con.isClosed()){
                    
                    LogUtil.info("HRDC EVENT Email Potential Participant ---->","Connection success");

                    
                    String primaryKey = formData.getPrimaryKeyValue();
                    
                    LogUtil.info("HRDC EVENT Email Potential Participant ---->","Primary key is: "+primaryKey);
                    
                    String sql = "SELECT emp.c_empl_email_pri,emp.c_comp_name,emp.id as participant_id FROM app_fd_evnt_sv_participant part "
                            + "JOIN "
                            + "app_fd_empm_reg emp ON part.c_participant_id = emp.id "
                            + "LEFT JOIN "
                            + "app_fd_stp_city city ON emp.c_empl_city = city.id "
                            + "LEFT JOIN "
                            + "app_fd_stp_state state ON emp.c_empl_state = state.id "
                            + "WHERE "
                            + "part.c_parent_id = '"+ primaryKey +"';";
                    
                    LogUtil.info("HRDC EVENT Email Potential Participant ---->","SQL select is: select***");
                    
                    PreparedStatement stmt = con.prepareStatement(sql);

                    ResultSet rs = stmt.executeQuery();
                    
                    LogUtil.info("HRDC EVENT Email Potential Participant ---->","SQL Result: "+rs);
                    
                    if(rs.next()){
                        
                        LogUtil.info("HRDC EVENT Email Potential Participant ---->","We have Result. Start do loop");
                        do{
                            user_email = rs.getString("c_empl_email_pri");
                            user_name = rs.getString("c_comp_name");
                            subject = "INVITATION TO #EVENT_NAME_HERE# BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)";
                            msg = "<head>\n" +
                                "    <meta charset=\"UTF-8\">\n" +
                                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                "    <style>\n" +
                                "        body {\n" +
                                "            font-family: Arial, sans-serif;\n" +
                                "            margin: 0;\n" +
                                "            padding: 0;\n" +
                                "            background-color: #f5f5f5;\n" +
                                "        }\n" +
                                "        .container {\n" +
                                "            max-width: 600px;\n" +
                                "            margin: 0 auto;\n" +
                                "            padding: 20px;\n" +
                                "            background-color: #ffffff;\n" +
                                "            border: 1px solid #ccc;\n" +
                                "            border-radius: 5px;\n" +
                                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\n" +
                                "        }\n"+
                                "       #qrContainer{\n" +
                                "           width: 200px;\n" +
                                "           height: 200px;\n" +
                                "           display: none;\n" +
                            "           }\n" +
                                "        h1 {\n" +
                                "            color: #d9534f;\n" +
                                "            margin: 0 0 20px;\n" +
                                "        }\n" +
                                "        p {\n" +
                                "            color: #333;\n" +
                                "            margin: 20px 0;\n" +
                                "            margin-bottom: 10px; \n" +
                                "            text-align: justify;\n" +
                                "        }\n" +
                                "        .signature {\n" +
                                "            margin-top: 20px;\n" +
                                "            color: #666;\n" +
                                "        }\n" +
                                "        .footer {\n" +
                                "            margin-top: 30px;\n" +
                                "            text-align: center;\n" +
                                "            color: #999;\n" +
                                "        }\n" +
                                "      .bold{\n" +
                                "        font-weight: bold;\n" +
                                "      }\n" +
                                "      \n" +
                                "    </style>\n" +
                                "</head>"+
                                "<body>\n" +
                                "    <div class=\"container\">\n" +
                                "        <h1 style=\"text-align: center\">Invitation for #Event_Name#</h1>\n" +
                                "        <p><span class=\"bold\">MANAGING DIRECTOR</span><br>\n" +
                                "        #ADDRESS1#<br>\n" +
                                "        #ADDRESS2#<br>\n" +
                                "        #ADDRESS3#<br>\n" +
                                "        #ADDRESS4#<br>\n" +
                                "        #POSTCODE# #CITY#,<br>\n" +
                                "        #STATE FULL NAME#, #COUNTRY#.</p>\n" +
                                "        \n" +
                                "        <p>Dear "+user_name+",</p>\n" +
                                "        \n" +
                                "        <p class=\"bold\">INVITATION FOR #EVENT_NAME# BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)</p>\n" +
                                "        \n" +
                                "        <p>The above matter refers. Event ID No: <span class=\"bold\">#form.event.eh_evnt_id#</span></p>\n" +
                                "        \n" +
                                "        <p>2. PSMB has evaluated your proposed training course(s) to ensure that it meets the criteria of the HRD Corp claimable courses scheme. Below are details of your course:</p>\n" +
                                "        \n" +
                                "        <table>\n" +
                                "            <tr>\n" +
                                "                <td>Your Email:</td>\n" +
                                "                <td><span class=\"bold\">"+user_email+"</span></td>\n" +
                                "            </tr>\n" +
                                "            <tr>\n" +
                                "                <td>Your Name:</td>\n" +
                                "                <td><span class=\"bold\">"+user_name+"</span></td>\n" +
                                "            </tr>\n" +
                                "        </table>\n" +
                                "        \n" +
                                "        <p style=\"font-style: italic;\"><span class=\"bold\">Important Notice:</span> Conference program approval period is only valid until the end of the conference (one-off approval).</p>\n" +
                                "        \n" +
                                "        <p>3. Training providers are only allowed to market approved training courses using the course title, and trainer(s) that have been registered under the HRD Corp claimable courses scheme. Any changes on the trainer need to receive prior approval from PSMB before running the course. Apart from this, training providers and trainers must also ensure that the quality of your training courses and delivery meets the expectations of the employers.</p>\n" +
                                "        \n" +
                                "        <p>4. Training Providers are advised to refer to the terms and conditions for HRD Corp claimable courses scheme implementation in the relevant Training Providers Circular / Employers Circular.</p>\n" +
                                "        \n" +
                                "        <p>5. Pembangunan Sumber Manusia Berhad (PSMB) is not responsible in terms of legal implications in the event of issues regarding intellectual property which defies the Copyright Protection Act.</p>\n" +
                                "        \n" +
                                "        <p>Should you require further assistance, please contact our support team at 1 800 88 4800 or email support@hrdcorp.gov.my.</p>\n" +
                                "        \n" +
                                "        <p>Thank You.</p>\n" +
                                "        \n" +
                                "        <p>Regards,</p>\n" +
                                "        <p class=\"signature\">\n" +
                                "            RIVALDO JUING ANAK UNING<br>\n" +
                                "            for Chief Executive<br>\n" +
                                "            Pembangunan Sumber Manusia Berhad\n" +
                                "        </p>\n" +
                                "        \n" +
                                "        <p>This is a computer-generated email and does not require a signature.</p>\n" +
                                "    </div>\n" +
                                "       \n" +
                                "    <div class=\"footer\">\n" +
                                "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\n" +
                                "    </div>\n" +
                                "</body>"+
                                "<script type=\"text/javascript\" src=\"#appResource.qrcode.min.js#\"></script>\n" +
                                "\n" +
                                "<script type=\"text/javascript\">\n" +
                                "\n" +
                                "   $(function(){\n" +
                                "       var id = \"#form.qr_contact.id#\";\n" +
                                "       if( !id.startsWith(\"#\") ){\n" +
                                "           $(\"#qrContainer\").show();\n" +
                                "           var qrcode = new QRCode({\n" +
                                "           content: id,\n" +
                                "           container: \"svg-viewbox\", //Responsive use\n" +
                                "           join: true //Crisp rendering and 4-5x reduced file size\n" +
                                "           });\n" +
                                "           var svg = qrcode.svg();\n" +
                                "           document.getElementById(\"qrContainer\").innerHTML = svg;\n" +
                                "           document.getElementById(\"containerExplain\").innerHTML = \"Scan this QR Code with the scanner menu\";    \n" +
                                "       }\n" +
                                "   });\n" +
                                "</script>";
                            try{
                                LogUtil.info("HRDC EVENT Email Potential Participant ---->","Trying to send email to: "+"("+user_name+") - "+user_email);
                                sendEmail(user_email, "", subject, msg);
                            }catch(Exception ex){
                                LogUtil.info("HRDC EVENT Email Potential Participant ---->","Fail trying to send email to: "+"("+user_name+") - "+user_email);
                            }
                        }while(rs.next());
                    }else{
                        LogUtil.info("HRDC EVENT Email Potential Participant ---->","We don't have Result");
                    }

                    LogUtil.info("HRDC EVENT Email Potential Participant ---->","Done looping through selected data");
                }else{
                    LogUtil.info("HRDC EVENT Email Potential Participant ---->","Connection Fail or Disconnect");
                }
            }else{
                LogUtil.info("HRDC EVENT Email Potential Participant ---->","Document Status is not 'Published'. Email wont be send");
                try {
                    con.close();
                } catch (SQLException ex) {
                    Logger.getLogger(EvntEmailPotentialClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }catch (SQLException ex) {
            
            LogUtil.info("HRDC EVENT Email Potential Participant ---->","Could not get user email and name from DB");
            
        }finally {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(EvntEmailPotentialClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return rows;
    }
    
    public static void sendEmail(String user_email, String bcc, String subject, String msg) {  
        
        LogUtil.info("HRDC EVENT Email Potential Participant ---->","Actually Sending Email to: " +user_email);
        
        try{
            EmailTool et = new EmailTool();

            PluginDefaultPropertiesDao dao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
            PluginDefaultProperties pluginDefaultProperties = dao.loadById("org.joget.apps.app.lib.EmailTool", AppUtil.getCurrentAppDefinition());
            Map properties = PropertyUtil.getPropertiesValueFromJson(pluginDefaultProperties.getPluginProperties());

            properties.put("from", "no-reply@your-company-name.com");
            properties.put("toSpecific", user_email);
            properties.put("bcc", "bcc");
            properties.put("subject", subject);
            properties.put("message", msg);
            properties.put("isHtml", "true");

            et.execute(properties);
        }catch(Exception ex){
            LogUtil.error("HRDC EVENT Email Potential Participant ---->", ex, "Error sending email");
        }
        
        LogUtil.info("HRDC EVENT Email Potential Participant ","email successfully sent to participant" + user_email);      
    }

    @Override
    public String getName() {
        return ("HRDC - EVNT - Auto Reject And Audit");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To update audit trail record to database");
    }

    @Override
    public String getLabel() {
        return ("HRDC - EVNT - Auto Reject And Audit");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/cr_audit_trail.json", null, true);
    }
    
}
