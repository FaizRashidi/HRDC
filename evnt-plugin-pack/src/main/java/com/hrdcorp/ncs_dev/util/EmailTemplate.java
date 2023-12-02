package com.hrdcorp.ncs_dev.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.util.WorkflowUtil;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


// Used in EventEmailTemplate.java

public class EmailTemplate {

    //Get email template from email template setup
    public static Map<String, String> getTemplate (String mail_template, Connection con) throws SQLException{
        LogUtil.info("HRDC - EVENT - Email Template Util ---->","Getting template: " + mail_template);
        String query = "SELECT "
                    +"et.id, "
                    +"CONCAT(et.c_moduleType, ' - ', et.c_emailType) as template_name, "
                    +"t.c_template_subject, "
                    +"t.c_template_content "
                    +"FROM app_fd_stp_evt_email_tmpl et "
                    +"INNER JOIN app_fd_stp_evt_template t ON et.c_mailTemplate = t.id "
                    +"AND et.id=?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, mail_template);
        ResultSet rs = stmt.executeQuery(); 

        Map<String, String> result = new HashMap<>();
        if(rs.next()) {
            String id = rs.getString("id");
            String template_name = rs.getString("template_name");
            String c_template_subject = rs.getString("c_template_subject");
            String c_template_content = rs.getString("c_template_content");
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Template Subject: " + c_template_subject);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Template Content: ...");

            result.put("id",id);
            result.put("template_name", template_name);
            result.put("c_template_subject", c_template_subject);
            result.put("c_template_content", c_template_content);
        }

        return result;
    }

    public static Map<String, String> getTemplateFromEventCreation (String mail_template, Connection con) throws SQLException{
        LogUtil.info("HRDC - EVENT - Email Template Util ---->","Getting from Event Creation: " + mail_template);
        String query = "SELECT "
                    +"et.id, "
                    +"CONCAT(et.c_moduleType, ' - ', et.c_emailType) as template_name, "
                    +"t.c_template_subject, "
                    +"t.c_template_content "
                    +"FROM app_fd_stp_evt_email_tmpl et "
                    +"INNER JOIN app_fd_stp_evt_template t ON et.c_mailTemplate = t.id "
                    +"AND t.id=?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, mail_template);
        ResultSet rs = stmt.executeQuery(); 

        Map<String, String> result = new HashMap<>();
        if(rs.next()) {
            String id = rs.getString("id");
            String template_name = rs.getString("template_name");
            String c_template_subject = rs.getString("c_template_subject");
            String c_template_content = rs.getString("c_template_content");
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Template Subject: " + c_template_subject);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Template Content: ...");

            result.put("id",id);
            result.put("template_name", template_name);
            result.put("c_template_subject", c_template_subject);
            result.put("c_template_content", c_template_content);
        }

        return result;
    }
    
    //Get email template from user saved template (user saved template from: event review form > ajax subform (query_email_template). This form (https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/event_registration_module/1/form/builder/event_user_email) has child form with table name of event_user_email)
    public static Map<String, String> getTemplateFromUser (String id, Connection con) throws SQLException{
        LogUtil.info("HRDC - EVENT - Email Template Util ---->","Getting template from user: " + id);
        
        String query = "SELECT c_parentId, c_additional_email, c_template_subject, c_template_content FROM app_fd_event_user_email WHERE c_parentId = ?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery(); 

        Map<String, String> result = new HashMap<>();
        if(rs.next()) {
            String parentId = rs.getString("c_parentId");
            String c_additional_email = rs.getString("c_additional_email");
            String c_template_subject = rs.getString("c_template_subject");
            String c_template_content = rs.getString("c_template_content");
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Template Subject from user: " + c_template_subject);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Template additional emailr: " +c_additional_email);

            result.put("parentId", parentId);
            result.put("c_additional_email", c_additional_email);
            result.put("c_template_subject", c_template_subject);
            result.put("c_template_content", c_template_content);
        }

        return result;
    }

    public static String buildContent (String app, String nonTemplate, String id, String msg, Connection con) throws SQLException{
        String tableName = "";

        LogUtil.info("HRDC - EVENT - Email Template Util ---->","Building string");
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","app: "+app);
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","msg: " +msg);

        if(app.equals("event_creation") || app.equals("event_officer_modifying")){
            tableName = "app_fd_event";
        }else if(app.equals("event_registration") || app.equals("event_participant_withdrawing") || app.equals("event_participant_modifying")){
            tableName = "app_fd_event_registration";
        }

        // convert app to process name
        String[] process = app.split("_");

        StringBuilder converted = new StringBuilder();

        for (String word : process) {
            // Capitalize the first letter of each word
            String capitalized = word.substring(0, 1).toUpperCase() + word.substring(1);
            converted.append(capitalized).append(" ");
        }

        String processName = converted.toString().trim();

        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","tableName: " + tableName);
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Email Content: ...");

        Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
        Matcher matcher = pattern.matcher(msg);

        while (matcher.find()) {
            String placeholder = matcher.group(); // Get the placeholder including brackets
            String columnName = matcher.group().replace("[", "").replace("]", ""); // Get the column name inside the brackets

            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","placeholder: " +placeholder);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","columnName: " +columnName);

            // Fetch the value from the database for the specific column

            String value = "";
            if(nonTemplate == "true" && columnName.contains("c_action_review_status")){
                value = "Queried";                
            }else if(nonTemplate == "true" && columnName.contains("c_action_activity")){
                value = processName+" - Review";
            }else if(columnName.contains("LINK")){
                value =  fetchLinkFromDatabase(columnName, tableName,id, con);
            }else if(columnName.contains("c_motto")){
                value = fetchMottoFromDatabase(columnName, tableName, id, con);
            }else if(columnName.contains("dateCurrent") || columnName.contains("date_of_letter")){
                value = getDate();
            }else{
                value = fetchValueFromDatabase(columnName, tableName, id, con);
            }
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->",columnName+": "+value);

            // Replace the placeholder with the actual value
            msg = msg.replace(placeholder, value);
        }

        return msg;
    }

    public static String buildContentWithUserInfo (String app, String nonTemplate, String id, String msg, String userId, String blastType, Connection con) throws SQLException{
        String tableName = "";

        LogUtil.info("HRDC - EVENT - Email Template Util ---->","Building string");
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","app: "+app);
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","id: " +id);

        if(app.equals("event_creation") || app.equals("event_officer_modifying")){
            tableName = "app_fd_event";
        }else if(app.equals("event_registration") || app.equals("event_participant_withdrawing") || app.equals("event_participant_modifying")){
            tableName = "app_fd_event_registration";
        }

        // convert app to process name
        String[] process = app.split("_");

        StringBuilder converted = new StringBuilder();

        for (String word : process) {
            // Capitalize the first letter of each word
            String capitalized = word.substring(0, 1).toUpperCase() + word.substring(1);
            converted.append(capitalized).append(" ");
        }

        String processName = converted.toString().trim();

        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","tableName: " + tableName);
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","Email Content: ...");

        Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
        Matcher matcher = pattern.matcher(msg);

        while (matcher.find()) {
            String placeholder = matcher.group(); // Get the placeholder including brackets
            String columnName = matcher.group().replace("[", "").replace("]", ""); // Get the column name inside the brackets

            // Fetch the value from the database for the specific column

            String value = "";
            if(columnName.contains("QR_CODE")){
                value = fetchQrCode(userId, con);
            }else if(columnName.contains("c_evnt_pd") || columnName.contains("recipient")){
                value = fetchRecepientData(columnName, blastType, userId, con);            
            }else if(nonTemplate == "true" && columnName.contains("c_action_review_status")){
                value = "Queried";                
            }else if(nonTemplate == "true" && columnName.contains("c_action_activity")){
                value = processName+" - Review";
            }else if(columnName.contains("LINK")){
                value =  fetchLinkFromDatabase(columnName, tableName,id, con);
            }else if(columnName.contains("c_motto")){
                value = fetchMottoFromDatabase(columnName, tableName, id, con);
            }else if(columnName.contains("dateCurrent") || columnName.contains("date_of_letter")){
                value = getDate();
            }else{
                value = fetchValueFromDatabase(columnName, tableName, id, con);
            }
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->",columnName+": "+value);

            // Replace the placeholder with the actual value
            msg = msg.replace(placeholder, value);
        }

        return msg;
    }

    private static String fetchValueFromDatabase(String columnName, String tableName, String id, Connection con) {
        String value = "";
        String query = "";
        
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","columnName: " + value);
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","tableName: " + value);
        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","id: " + id);
        
        query = "SELECT "+columnName+" FROM "+tableName+" WHERE id=?";

        // LogUtil.info("HRDC - EVENT - Email Template Util ---->","query: " + query);
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if(rs.getString(columnName) == null || rs.getString(columnName).isEmpty()){
                    value = "";
                }else if(columnName.contains("dateCreated")||columnName.contains("dateModified")||columnName.contains("date")){
                    value = convertDateFormat(rs.getString(columnName));
                }else {
                    value = rs.getString(columnName);
                }
                // LogUtil.info("HRDC - EVENT - Email Template Util ---->",columnName+": " + value);
            }
        } catch (SQLException e) {
            LogUtil.info("HRDC - EVENT - Email Template Util ---->","Fail to get column: "+columnName+"; may not exist in table: "+tableName);
            value = "";
            return value;
        }
        return value;
    }
   
    private static String fetchLinkFromDatabase (String columnName, String tableName, String id, Connection con){
        String value= "", query = "";

        query = "SELECT * FROM app_fd_stp_evt_links WHERE c_keyword =?";
        

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            String[] splitField = columnName.split("\\{");
            String link_type = splitField[0];
            String secParam = splitField.length>1? splitField[1].replace("}", "") :"";
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","columnName: " + columnName);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","splitField: " + splitField);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","link_type: " + value);
            // LogUtil.info("HRDC - EVENT - Email Template Util ---->","secParam: " + secParam);
            stmt.setString(1, link_type);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                
                String baseUrl = WorkflowUtil.getHttpServletRequest().getScheme()+"://"+WorkflowUtil.getHttpServletRequest().getServerName()+"/jw/web/userview/event_registration_module/event/_/";
                String html_link = "<a href='";
                String key = "", isInbox = "", url="";

                key = rs.getString("c_link")==null?"":rs.getString("c_link").toString();
                isInbox = rs.getString("c_is_inbox")==null?"":rs.getString("c_is_inbox").toString();
                
                if(!isInbox.isEmpty()){
                    url = baseUrl+key;
                }else{
                    url = baseUrl+key+"?id="+id;
                }

                value = html_link+url+ "'>" + secParam + "</a>";
            }

        }catch(Exception ex){
            LogUtil.info("HRDC - EVENT - Email Template Util ---->","Fail to Link "+columnName+": ");
            value = "";
            return value;
        }

        return value;
    }

    private static String fetchMottoFromDatabase (String columnName, String tableName, String id, Connection con){
        String value= "", query = "";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");  
        LocalDateTime now = LocalDateTime.now();  
        
        String current_year = dtf.format(now);
        query = "SELECT * FROM app_fd_empm_stp_mail_motto "
                + "WHERE curdate() between STR_TO_DATE(concat(c_time_start,'-"+current_year+"'), '%d-%b-%Y') and " 
                + "STR_TO_DATE(concat(c_time_end,'-"+current_year+"'), '%d-%b-%Y')";;
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                value = rs.getString(columnName);

                LogUtil.info("HRDC - EVENT - Email Template Util ---->",columnName+": " + value);
            }
        } catch (SQLException e) {
            LogUtil.info("HRDC - EVENT - Email Template Util ---->","Fail to get column: "+columnName+"; may not exist in table: "+tableName);
            value = "";
            return value;
        }

        return value;
    }

    private static String fetchRecepientData(String columnName, String blastType, String id, Connection con) {
        String value = "";
        String query = "";
        String column = "";

        if(blastType.equals("potential_participant")){

            query = "SELECT "+columnName+" FROM app_fd_evnt_sv_participant WHERE c_participant_id=?";
            
            try (PreparedStatement stmt = con.prepareStatement(query)) {

                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    if(rs.getString(columnName) == null || rs.getString(columnName).isEmpty()){
                        value = "";
                    }else {
                        value = rs.getString(columnName);
                    }
                }
            } catch (SQLException e) {
                LogUtil.info("HRDC - EVENT - Email Template Util ---->","Fail to get column: "+columnName+"; may not exist in table: app_fd_evnt_sv_participant");
                value = "";
                return value;
            }
        }else{
            
            query = "SELECT "+columnName+" FROM app_fd_event_reg_user WHERE id=?";
            
            try (PreparedStatement stmt = con.prepareStatement(query)) {

                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    if(rs.getString(columnName) == null || rs.getString(columnName).isEmpty()){
                        value = "";
                    }else {
                        value = rs.getString(columnName);
                    }
                }
            } catch (SQLException e) {
                LogUtil.info("HRDC - EVENT - Email Template Util ---->","Fail to get column: "+columnName+"; may not exist in table: app_fd_event_reg_user");
                value = "";
                return value;
            }
        }
        return value;
    }

    private static String fetchQrCode (String id, Connection con){
        
        try{
            String data = id;
            String path = SetupManager.getBaseDirectory() + "app_formuploads/event_reg_user/" + id+"/"+ id+".png";
            String charset = "UTF-8";
            Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
            hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            
            LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Checking if path exist: " + path);
                                    
            File f = new File(path);
            
            if (!f.exists()) {
                LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Path not exist");
                f.getParentFile().mkdirs();
                createQR(data, path, charset, hashMap, 200, 200);
            } else if (!f.isDirectory()) {
                LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Path is not a directory");
            } else{
                LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Path Exist");
                createQR(data, path, charset, hashMap, 200, 200);
            }
            try{
                String updateSql = "UPDATE app_fd_event_reg_user SET c_evnt_pd_qrCode ='"+id+".png' WHERE id = ?;";
            
                LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Update Sql = " + updateSql);

                PreparedStatement updateStmt = con.prepareStatement(updateSql);
                updateStmt.setString(1, id);

                ResultSet res = updateStmt.executeQuery();
            }catch(Exception ex){
                LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Cannot Update User QR = " + ex);
            }
                                                            
        }catch (WriterException e) {
            LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Could not generate QR Code, WriterException");
            System.out.println("HRDC - EVENT - Genrate QR Code ----> Could not generate QR Code, WriterException :: " + e.getMessage());
        } catch (IOException e) {
            LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","Could not generate QR Code, IOException");
            System.out.println("HRDC - EVENT - Genrate QR Code ----> Could not generate QR Code, IOException :: " + e.getMessage());
        }

        String qr = "<img src='"+"https://ncs-dev.hrdcorp.gov.my/jw/web/client/app/evnt_management/1/form/download/registration_form/"+id+"/"+id+".png"+"' height='200' width='200'>";
        return qr;
    }

    private static String getDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");  
        LocalDateTime now = LocalDateTime.now();  
        
        return dtf.format(now);
    }

    public static String convertDateFormat(String inputDate) {
        LogUtil.info("HRDC - EVENT - Email Template Util ---->","date: " + inputDate);
        SimpleDateFormat inputDateFormat;
        if (inputDate.contains(".")){
            inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }else{
            inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
        try {
            Date date = inputDateFormat.parse(inputDate);
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Return null in case of an error
        }
    }

    public static String emailContentHeader(String subject){
        String header ="";
        header= "<head>\r\n" +
                "    <meta charset=\"UTF-8\">\r\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" +
                "    <style>\r\n" +
                "        body {\r\n" +
                "            font-family: Arial, sans-serif;\r\n" +
                "            margin: 0;\r\n" +
                "            padding: 0;\r\n" +
                "            background-color: #f5f5f5;\r\n" +
                "        }\r\n" +
                "        .container {\r\n" +
                "            max-width: 600px;\r\n" +
                "            margin: 0 auto;\r\n" +
                "            padding: 20px;\r\n" +
                "            background-color: #ffffff;\r\n" +
                "            border: 1px solid #ccc;\r\n" +
                "            border-radius: 5px;\r\n" +
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\r\n" +
                "        }\r\n" +
                "        h1 {\r\n" +
                "            color: #d9534f;\r\n" +
                "            margin: 0 0 20px;\r\n" +
                "        }\r\n" +
                "        p {\r\n" +
                "            color: #333;\r\n" +
                "            margin: 20px 0;\r\n" +
                "            margin-bottom: 10px; \r\n" +
                "            text-align: justify;\r\n" +
                "        }\r\n" +
                "        .signature {\r\n" +
                "            margin-top: 20px;\r\n" +
                "            color: #666;\r\n" +
                "        }\r\n" +
                "        .footer {\r\n" +
                "            margin-top: 30px;\r\n" +
                "            text-align: center;\r\n" +
                "            color: #999;\r\n" +
                "        }\r\n" +
                "      .bold{\r\n" +
                "        font-weight: bold;\r\n" +
                "      }\r\n" +
                "      \r\n" +
                "    </style>\r\n" +
                "</head>\r\n" +
                "<body>\r\n" +
                "    <div class=\"container\">\r\n" +
                "        <h1 style='text-align: center'>"+subject+"</h1>";
        return header;
    }

    public static String emailContentFooter(){
        String footer = "";

        footer = "</div><div class=\"footer\">\r\n" +
                "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\r\n" +
                "    </div>\r\n" +
                "</body>";
        return footer;
    }

    // Function to create the QR code
    public static void createQR(String data, String path, String charset, Map hashMap, int height, int width) throws WriterException, IOException {
 
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),BarcodeFormat.QR_CODE, width, height);
        
        MatrixToImageWriter.writeToFile( matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
        
        LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","CreateQr - data = " + data);
        LogUtil.info("HRDC - EVENT - Genrate QR Code ---->","CreateQr - Path = " + path);
    }  

}
