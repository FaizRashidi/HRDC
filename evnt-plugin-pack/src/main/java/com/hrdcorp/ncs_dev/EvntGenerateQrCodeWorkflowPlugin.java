
package com.hrdcorp.ncs_dev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import static com.hrdcorp.ncs_dev.EvntAuditAndEmailPotentialClientBinder.sendEmail;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.lib.EmailTool;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.commons.util.SetupManager;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.property.service.PropertyUtil;


public class EvntGenerateQrCodeWorkflowPlugin extends DefaultApplicationPlugin {
    
    @Override
    public String getName() {
        return ("HRDC - EVNT - Generate Qr Code and Send emaail");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To ganerate qr code, save to db and send email");
    }

    @Override
    public String getLabel() {
        return ("HRDC - EVNT - Generate Qr Code and Send emaail");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
      return "";
    }
    
    PluginManager pm = null;
    WorkflowManager wm = null;
    WorkflowAssignment wfAssignment = null;
    
    @Override
    public Object execute(Map props) {
       
        wfAssignment = (WorkflowAssignment) props.get("workflowAssignment");     
        pm =  (PluginManager)props.get("pluginManager");
        wm = (WorkflowManager) pm.getBean("workflowManager");
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        
        String recordid = appService.getOriginProcessId(wfAssignment.getProcessId());
        
        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Proces ID = " + recordid);
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
                        
        try {
            con = ds.getConnection();
            String sql = "SELECT  u.* FROM  app_fd_event_reg_user u JOIN app_fd_event_registration r ON u.c_parentId = r.id WHERE r.id = ?;" ;
            
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, recordid);
            
            ResultSet rs = stmt.executeQuery();          
            
            LogUtil.info("HRDC EVENT Genrate QR Code ---->","Sql = " + sql);
            if(rs.next()){
                do{
                    String userId = rs.getString("id");
                  
                    LogUtil.info("HRDC EVENT Genrate QR Code ---->","user ID = " + userId);

                    try{
                        String data = userId;
                        String path = SetupManager.getBaseDirectory() + "app_formuploads/event_reg_user/" + userId+"/"+ userId+".png";
                        String charset = "UTF-8";
                        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                        
                        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Checking if path exist: " + path);
                                                
                        File f = new File(path);
                        
                        if (!f.exists()) {
                            LogUtil.info("HRDC EVENT Genrate QR Code ---->","Path not exist");
                            f.getParentFile().mkdirs();
                            createQR(data, path, charset, hashMap, 200, 200);
                        } else if (!f.isDirectory()) {
                            LogUtil.info("HRDC EVENT Genrate QR Code ---->","Path is not a directory");
                        } else{
                            LogUtil.info("HRDC EVENT Genrate QR Code ---->","Path Exist");
                            createQR(data, path, charset, hashMap, 200, 200);
                        }
                        try{
                            String updateSql = "UPDATE app_fd_event_reg_user SET c_evnt_pd_qrCode ='"+userId+".png' WHERE id = ?;";
                        
                            LogUtil.info("HRDC EVENT Genrate QR Code ---->","Update Sql = " + updateSql);

                            PreparedStatement updateStmt = con.prepareStatement(updateSql);
                            updateStmt.setString(1, userId);

                            ResultSet res = updateStmt.executeQuery();
                        }catch(Exception ex){
                            LogUtil.info("HRDC EVENT Genrate QR Code ---->","Cannot Update User QR = " + ex);
                        }
                                                                        
                    }catch (WriterException e) {
                        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Could not generate QR Code, WriterException");
                        System.out.println("HRDC EVENT Genrate QR Code ----> Could not generate QR Code, WriterException :: " + e.getMessage());
                    } catch (IOException e) {
                        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Could not generate QR Code, IOException");
                        System.out.println("HRDC EVENT Genrate QR Code ----> Could not generate QR Code, IOException :: " + e.getMessage());
                    }
                    
                    
                    String user_email = rs.getString("c_evnt_pd_email_add");
                    String user_name = rs.getString("c_evnt_pd_name");
                    String comp_name = rs.getString("c_evnt_pd_name_of_comp");
                    String icPassport = rs.getString("c_evnt_pd_ic");
                    String qr = "https://ncs-dev.hrdcorp.gov.my/jw/web/client/app/evnt_management/1/form/download/registration_form/"+userId+"/"+userId+".png";
                    String eventId = rs.getString("c_evnt_pd_eventId");
                    String eventName = rs.getString("c_evnt_pd_eventName");
                    String subject = "INVITATION TO "+eventName+" BY PEMBANGUNAN SUMBER MANUSIA BERHAD (PSMB)";
                    String msg = "<head>\n" +
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
                                "        }\n" +
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
                                "      .card {\n" +
                                "            border: 1px solid #ccc;\n" +
                                "            border-radius: 5px;\n" +
                                "            padding: 10px;\n" +
                                "            background-color: #f9f9f9;\n" +
                                "            margin-top: 20px;\n" +
                                "        }\n" +
                                "        .qr-code {\n" +
                                "            width: 200px; /* Adjust the size as needed */\n" +
                                "            height: 200px; /* Adjust the size as needed */\n" +
                                "            margin-right: 10px;\n" +
                                "        }\n" +
                                "        .event-details {\n" +
                                "            display: flex;\n" +
                                "            align-items: center;\n" +
                                "        }\n" +
                                "      \n" +
                                "    </style>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "    <div class=\"container\">\n" +
                                "        <h1 style=\"text-align: center\">E-TICKET FOR "+eventName+"</h1>\n" +
                                "               \n" +
                                "        <p>PSMB has invited you to "+eventName+". Please bring the to email to scan as attendance. Below are details of your course:</p>\n" +
                                "        \n" +
                                "        <table>\n" +
                                "            <tr>\n" +
                                "                <td>Event ID:</td>\n" +
                                "                <td><span class=\"bold\">"+eventId+"</span></td>\n" +
                                "            </tr>\n" +
                                "            <tr>\n" +
                                "                <td>Event Name:</td>\n" +
                                "                <td><span class=\"bold\">"+eventName+"</span></td>\n" +
                                "            </tr>\n" +
                                "            <tr>\n" +
                                "                <td>Publish Date:</td>\n" +
                                "                <td><span class=\"bold\">#date.EEE,d MMM yyyy#</span></td>\n" +
                                "            </tr>\n" +
                                "            <tr>\n" +
                                "                <td>Trainers:</td>\n" +
                                "                <td>\n" +
                                "                    <span class=\"bold\">1) #TRAINER 1</span><br>\n" +
                                "                    <span class=\"bold\">2) #TRAINER 2#</span>\n" +
                                "                </td>\n" +
                                "            </tr>\n" +
                                "        </table>\n" +
                                "        \n" +
                                "        <!-- Card Section -->\n" +
                                "        <div class=\"card\">\n" +
                                "            <div class=\"event-details\">\n" +
                                "                <img src=\""+qr+"\" alt=\"QR Code\" class=\"qr-code\">\n" +
                                "                <div>\n" +
                                "                    <p><span class=\"bold\">Name:</span> "+user_name+"</p>\n" +
                                "                    <p><span class=\"bold\">My Card No:</span> "+icPassport+"</p>\n" +
                                "                    <p><span class=\"bold\">Email:</span> "+user_email+"</p>\n" +
                                "                    <p><span class=\"bold\">Company Name:</span> "+comp_name+"</p>\n" +
                                "                </div>\n" +
                                "            </div>\n" +
                                "        </div>\n" +
                                "        <!-- End Card Section -->\n" +
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
                                "\n" +
                                "    <div class=\"footer\">\n" +
                                "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\n" +
                                "    </div>\n" +
                                "</body>";
                    try{
                        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Trying to send email to: "+"("+user_name+") - "+user_email);
                        sendEmail(user_email, "", subject, msg);
                    }catch(Exception ex){
                        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Fail trying to send email to: "+"("+user_name+") - "+user_email);
                    }
                    
                }while(rs.next());
            }else{
                LogUtil.info("HRDC EVENT Genrate QR Code ---->","No result form query");
            }
        }catch (Exception ex){
            LogUtil.error("HRDC EVENT Genrate QR Code ---->", ex, "Error Connecting to DB, Audit Trail not saved");
        }finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error("HRDC EVENT Genrate QR Code ---->", ex, "Error closing the jdbc connection");
            }
        } 
        
        return null;
    }
    
    
    // Function to create the QR code
    public static void createQR(String data, String path, String charset, Map hashMap, int height, int width) throws WriterException, IOException {
 
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),BarcodeFormat.QR_CODE, width, height);
        
        MatrixToImageWriter.writeToFile( matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
        
        LogUtil.info("HRDC EVENT Genrate QR Code ---->","CreateQr - data = " + data);
        LogUtil.info("HRDC EVENT Genrate QR Code ---->","CreateQr - Path = " + path);
    }  
    
    public static void sendEmail(String user_email, String bcc, String subject, String msg) {  
        
        LogUtil.info("HRDC EVENT Genrate QR Code ---->","Actually Sending Email to: " +user_email);
        
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
            LogUtil.error("HRDC EVENT Genrate QR Code ---->", ex, "Error sending email");
        }
        
        LogUtil.info("HRDC EVENT Genrate QR Code ---->","email successfully sent to participant" + user_email);      
    }
}
