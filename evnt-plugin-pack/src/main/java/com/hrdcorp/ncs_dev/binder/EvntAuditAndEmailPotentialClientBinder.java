/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hrdcorp.ncs_dev.binder;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.property.service.PropertyUtil;
import org.springframework.beans.BeansException;


import com.hrdcorp.ncs_dev.util.SendEmail;

/**
 *
 * @author Fawad Khaliq <khaliq@opendynamics.com.my>
 */
public class EvntAuditAndEmailPotentialClientBinder extends WorkflowFormBinder {

    public EvntAuditAndEmailPotentialClientBinder getSuper() {
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

                String queryReason = row.getProperty("action_query_reason")!= null ? row.getProperty("action_query_reason") : "";
                String remarks = row.getProperty("action_remarks") != null ? row.getProperty("action_remarks") : ""; 
                String action_status = row.getProperty("status");
                String action_attachment = row.getProperty("action_attachment");
                String action_review_status = row.getProperty("action_review_status");
                
                String insertSql = "INSERT INTO app_fd_evnt_auditTrail (dateCreated,dateModified,c_action_date,id,createdBy,createdByName,modifiedBy,modifiedByName,c_action_workflow,c_action_activity,c_parentId,c_action_name,c_action_department,c_action_query_subject,c_action_additional_email,c_action_query_reason,c_action_remarks,c_status,c_action_attachment,c_action_review_status)"
                        + "VALUES (NOW(),NOW(),NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                 
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
                stmtInsert.setString(16, action_attachment);
                stmtInsert.setString(17, action_review_status);
                
                //Execute SQL statement
                try{
                    String workflow_path = null;
                    if (workflowName.endsWith("Event Creation") || workflowName.endsWith("Officer Modifying")){
                        workflow_path = "event";
                    }else if(workflowName.endsWith("Event Registration") || workflowName.endsWith("Participant Withdrawing") || workflowName.endsWith("Participant Modifying")){
                        workflow_path = "event_registration";
                    }
                    
                    String path = SetupManager.getBaseDirectory() + "app_formuploads/"+workflow_path+"/" + parentId+"/";
                    String newPath = SetupManager.getBaseDirectory() + "app_formuploads/evnt_auditTrail/" +pId+"/";
                    
                    LogUtil.info("Event Audit Trail ---->","Checking if path exist: " + path);                    
                    
                    Path sourcePath = Paths.get(path, action_attachment);
                    Path destinationPath = Paths.get(newPath, action_attachment);
                    
                    
                    if (Files.exists(sourcePath)) {
                        LogUtil.info("Event Audit Trail ---->","Source path exist:" + path);      
                        if (!Files.exists(destinationPath.getParent())) {
                            LogUtil.info("Event Audit Trail ---->","Destination path not exist, Creating Folder"+ destinationPath);
                            
                            Files.createDirectories(destinationPath.getParent());
                        }
                                                
                        try {
                            LogUtil.info("Event Audit Trail ---->","Copying File");
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            LogUtil.info("Event Audit Trail ---->","Copy Successful");
                        } catch (IOException e) {
                            e.printStackTrace();
                            LogUtil.info("Event Audit Trail ---->","Error copy");
                        }
                    }else{
                        LogUtil.info("Event Audit Trail ---->","Source path doesn't exist:" + path); 
                        if (!Files.exists(destinationPath.getParent())) {
                            LogUtil.info("Event Audit Trail ---->","Path not exist, Creating Folder"+ destinationPath);
                            
                            Files.createDirectories(destinationPath.getParent());
                        }
                    }
                    
                    stmtInsert.executeUpdate();
                }catch (Exception ex){
                    
                }
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
                                "</body>";
                            try{
                                SendEmail.sendEmail("HRDC EVENT Email Potential Participant", user_email, "", subject, msg);
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
                    Logger.getLogger(EvntAuditAndEmailPotentialClientBinder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }catch (SQLException ex) {
            
            LogUtil.info("HRDC EVENT Email Potential Participant ---->","Could not get user email and name from DB");
            
        }finally {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(EvntAuditAndEmailPotentialClientBinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return rows;
    }
    
    // Method to format date as per the required database format
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public String getName() {
        return ("HRDC - EVNT - Audit Trail Binder And Email Potential");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("Send email notification to potential clients");
    }

    @Override
    public String getLabel() {
        return ("HRDC - EVNT - Audit Trail Binder And Email Potential");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormId() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_ID);
    }

    @Override
    public String getTableName() {
        Form form = FormUtil.findRootForm(getElement());
        return form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
    }
}