package com.hrdcorp.ncs_dev.util;

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
import org.joget.workflow.util.WorkflowUtil;

public class EmailTemplate {

    public static Map<String, String> getTemplate (String mail_template, Connection con) throws SQLException{
        LogUtil.info("HRDC - COURSE - Email Template Util ---->","Getting template: " + mail_template);
        String query = "SELECT "
                    +"et.id, "
                    +"CONCAT(et.c_moduleType, ' - ', et.c_emailType) as template_name, "
                    +"t.c_template_subject, "
                    +"t.c_template_content "
                    +"FROM app_fd_stp_email_template et "
                    +"INNER JOIN app_fd_stp_template t ON et.c_mailTemplate = t.id "
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
            LogUtil.info("HRDC - COURSE - Email Template Util ---->","Template Subject: " + c_template_subject);
            LogUtil.info("HRDC - COURSE - Email Template Util ---->","Template Content: ...");

            result.put("id",id);
            result.put("template_name", template_name);
            result.put("c_template_subject", c_template_subject);
            result.put("c_template_content", c_template_content);
        }

        return result;
    }

    public static Map<String, String> getTemplateFromUser (String id, Connection con) throws SQLException{
        LogUtil.info("HRDC - COURSE - Email Template Util ---->","Getting template from user: " + id);
        
        String query = "SELECT c_parentId, c_additional_email, c_template_subject, c_template_content FROM app_fd_course_user_email WHERE c_parentId = ?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery(); 

        Map<String, String> result = new HashMap<>();
        if(rs.next()) {
            String parentId = rs.getString("c_parentId");
            String c_additional_email = rs.getString("c_additional_email");
            String c_template_subject = rs.getString("c_template_subject");
            String c_template_content = rs.getString("c_template_content");
            LogUtil.info("HRDC - COURSE - Email Template Util ---->","Template Subject from user: " + c_template_subject);
            LogUtil.info("HRDC - COURSE - Email Template Util ---->","Template additional emailr: " +c_additional_email);

            result.put("parentId", parentId);
            result.put("c_additional_email", c_additional_email);
            result.put("c_template_subject", c_template_subject);
            result.put("c_template_content", c_template_content);
        }

        return result;
    }

    public static String buildContent (String app, String id, String msg, Connection con) throws SQLException{
        String tableName = "";

        LogUtil.info("HRDC - COURSE - Email Template Util ---->","Building Content");
        // LogUtil.info("HRDC - COURSE - Email Template Util ---->","app: "+app);
        // LogUtil.info("HRDC - COURSE - Email Template Util ---->","id: " +id);

        if(app.equals("course_register") || app.equals("course_amendment")){
            tableName = "app_fd_course_register";
        }else if(app.equals("class_creation") || app.equals("class_amendment") || app.equals("class_cancellation")){
            tableName = "app_fd_course_class";
        }else if(app.equals("license_training_material")){
            tableName = "app_fd_course_ltm";
        }

        LogUtil.info("HRDC - COURSE - Email Template Util ---->","tableName: " + tableName);
        LogUtil.info("HRDC - COURSE - Email Template Util ---->","Email Content: " + msg);

        Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
        Matcher matcher = pattern.matcher(msg);

        while (matcher.find()) {
            String placeholder = matcher.group(); // Get the placeholder including brackets
            String columnName = matcher.group().replace("[", "").replace("]", ""); // Get the column name inside the brackets

            // Fetch the value from the database for the specific column
            String value = fetchValueFromDatabase(columnName, tableName, id, con);
            // LogUtil.info("HRDC - COURSE - Email Template Util ---->",columnName+": "+value);

            // Replace the placeholder with the actual value
            msg = msg.replace(placeholder, value);
        }

        return msg;
    }

    private static String fetchValueFromDatabase(String columnName, String tableName, String recordId, Connection con) {
        String value = "";
        String query = "";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");  
        LocalDateTime now = LocalDateTime.now();  
        
        String current_year = dtf.format(now);

        if(columnName.equals("c_motto")){
            query = "SELECT * FROM app_fd_empm_stp_mail_motto "
                    + "WHERE curdate() between STR_TO_DATE(concat(c_time_start,'-"+current_year+"'), '%d-%b-%Y') and " 
                    + "STR_TO_DATE(concat(c_time_end,'-"+current_year+"'), '%d-%b-%Y')";
        }else if(columnName.contains("LINK")){
            


        }else{
            query = "SELECT " + columnName + " FROM " + tableName + " WHERE id=?";
            // LogUtil.info("HRDC - COURSE - Email Template Util ---->","query: " +query);
        }
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, recordId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if(rs.getString(columnName) == null || rs.getString(columnName).isEmpty()){
                    value = "";
                }else if(columnName.contains("dateCreated")||columnName.contains("dateModified")||columnName.contains("date")){
                    value = convertDateFormat(rs.getString(columnName));
                }else if(columnName.contains("LINK")){
                    String baseUrl = WorkflowUtil.getHttpServletRequest().getScheme()+"://"+WorkflowUtil.getHttpServletRequest().getServerName();;
                    String html_link = "<a href='";
                    String key = "", isInbox = "";
                    String[] splitField = columnName.split("\\{");
                    String link_type = splitField[0];
                    String secParam = splitField.length>1? splitField[1].replace("}", "") :"";

                    key = rs.getString("c_link")==null?"":rs.getString("c_link").toString();
                    isInbox = rs.getString("c_is_inbox")==null?"":rs.getString("c_is_inbox").toString();
                    
                    value = html_link+baseUrl+key+"?id="+recordId+ "'>" + secParam + "</a>";
                }
                else {
                    value = rs.getString(columnName);
                }
            }
        } catch (SQLException e) {
            LogUtil.error("HRDC - COURSE - Email Template Util ---->",e,"Fail to get column "+columnName+": " + value);
        }
        return value;
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

        footer = "<div class=\"footer\">\r\n" +
                "        <p style=\"text-align: center; margin-bottom:20px\">&copy; 2023 HRD Corporation</p>\r\n" +
                "    </div>\r\n" +
                "</body>";
        return footer;
    }

    public static String convertDateFormat(String inputDate) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yy");
    
        try {
            Date date = inputDateFormat.parse(inputDate);
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Return null in case of an error
        }
    }

}
