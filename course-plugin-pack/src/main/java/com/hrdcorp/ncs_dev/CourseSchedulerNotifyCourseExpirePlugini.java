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
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.property.service.PropertyUtil;

/**
 *
 * @author farih
 */
public class CourseSchedulerNotifyCourseExpirePlugini extends DefaultApplicationPlugin {
    @Override
    public String getName() {
        return ("HRDC - COURSE - Scheduler Notify Course Expire");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To notify tp on course that about to expire due to no grant applied");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Scheduler Notify Course Expire");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/NotifyCourseExpiryPluginForm.json", null, true);
    }
    
    
    public Object execute(Map props) {
        LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Start scheduler");
        
        int interval = Integer.parseInt(props.get("interval").toString());
        String interval_unit = props.get("interval_unit").toString();
        String date_of = props.get("date_of").toString();
        int frequency = Integer.parseInt(props.get("frequency").toString());
        
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
                     
        try {
            // Establish a database connection
            Connection con = ds.getConnection();
            try {
                LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Connection sucess, try updating");
                                    
                String sql = "SELECT \n" +
                            "	cr.*, \n" +
                            "   du.username, \n" +
                            "   du.email,\n" +
                            "   CONCAT(du.firstName,\" \",du.lastName) as longname\n" +
                            "FROM app_fd_course_register AS cr\n" +
                            "LEFT JOIN dir_user AS du ON cr.createdBy = du.username\n" +
                            "WHERE NOT EXISTS (\n" +
                            "    SELECT 1\n" +
                            "    FROM app_fd_stp_dummy_grant AS dg\n" +
                            "    WHERE FIND_IN_SET(cr.id, REPLACE(dg.c_course_title, ';', ','))\n" +
                            ") \n" +
                            "AND STR_TO_DATE(cr."+date_of+", '%Y-%m-%d %H:%i:%s') < DATE_SUB(NOW(), INTERVAL "+interval+" "+interval_unit+")\n" +
                            "AND cr.c_active_inactive_status = 'Active'\n" +
                            "order by dateModified Desc;";

                PreparedStatement stmt = con.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();
                
                if(rs.next()){
                    String uuid;
                    String user_name;
                    String user_email;
                    String course_name;
                    String course_id;
                    int notify_counter;
                    String subject;
                    String msg;
                    LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","We have Result. Start do loop");
                    do{
                        uuid = rs.getString("id");
                        user_email = rs.getString("email");
                        user_name = rs.getString("longname");
                        course_name = rs.getString("c_course_name");
                        course_id = rs.getString("c_course_id");
                        notify_counter = Integer.parseInt(rs.getString("c_notify_counter") == null || rs.getString("c_notify_counter").isEmpty() ? "0" : rs.getString("c_notify_counter"));
                        subject = "APPLICATION FOR CREATION OF EVENT BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB): "+course_name+"";
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
                            "            margin: 20pxflksdjflsdjf 0;\n" +
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
                            "        <p class=\"bold\">REMINDER COURSE IS ABOUT TO EXPIRE: "+course_name+"</p>\n" +
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
                            LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Trying to send email to: "+"("+user_name+") - "+user_email);
                            if(notify_counter < frequency){
                                LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Sending reminder. Reminder number "+notify_counter+" + 1, out of "+frequency+" times.");
                                sendEmail(user_email, "", subject, msg);
                               Connection con2 = ds.getConnection();
                               try{
                                    LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Try updating notify counter");
                                    int new_notify_counter = notify_counter +1;
                                    
                                    String sql2 = "UPDATE app_fd_course_register AS cr\n" +
                                                "SET cr.c_notify_counter = '"+new_notify_counter+"'\n" +
                                                "WHERE\n" +
                                                "    cr.id = '"+uuid+"';";

                                    PreparedStatement stmt2 = con2.prepareStatement(sql2);

                                    stmt2.executeQuery();

                                    LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Update successful");
                                    con2.close();
                               }catch(Exception ex){
                                   LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Update notify counter unsuccessful: "+rs);
                                   try {
                                        if (con2 != null) {
                                            con2.close();
                                        }
                                    } catch (Exception e) {
                                        LogUtil.error("HRDC - COURSE - Scheduler Notify Course Expire ---->", e, "Error closing the jdbc connection");
                                    }
                               }
                            }else{
                                LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Not sending reminder. Reminded "+notify_counter+" out of "+frequency+" times.");
                            }
                            
                        }catch(Exception ex){
                            LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Fail trying to send email to: "+"("+user_name+") - "+user_email);
                            
                        }

                    }while(rs.next());
                }else{
                    LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","We don't have Result");
                }
            }catch(SQLException e){
               Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
               try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception ex) {
                    LogUtil.error("HRDC - COURSE - Scheduler Notify Course Expire ---->", ex, "Error closing the jdbc connection");
                }
            }finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception ex) {
                    LogUtil.error("HRDC - COURSE - Scheduler Notify Course Expire ---->", ex, "Error closing the jdbc connection");
                }
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    
     public static void sendEmail(String user_email, String bcc, String subject, String msg) {  
        
        LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","Actually Sending Email to: " +user_email);
        
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
            LogUtil.error("HRDC - COURSE - Scheduler Notify Course Expire ---->", ex, "Error sending email");
        }
        
        LogUtil.info("HRDC - COURSE - Scheduler Notify Course Expire ---->","email successfully sent to participant" + user_email);      
    }   
    
}
