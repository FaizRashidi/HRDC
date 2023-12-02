package com.hrdcorp.ncs_dev.default_plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;

import com.hrdcorp.ncs_dev.util.EmailTemplate;
import com.hrdcorp.ncs_dev.util.SendEmail;

// This is used in workflow (workflow tool) to send email after submitting form. ex: https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/process/builder#course_register > tool2: Email - Notification (Event Submitted)
// This is used to map email template to the activitiy in the process.

public class EvntBlastEmailMapper extends DefaultApplicationPlugin{
    
    @Override
    public String getName() {
        return ("HRDC - EVENT - Blast Email Mapper");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To send email using email plugin/mapper");
    }

    @Override
    public String getLabel() {
        return ("HRDC - EVENT - Blast Email Mapper");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/EmailBlastMapper.json", null, true);
    }
    
    PluginManager pm = null;
    WorkflowManager wm = null;
    WorkflowAssignment wfAssignment = null;
    
    @Override
    public Object execute(Map props){

        wfAssignment = (WorkflowAssignment) props.get("workflowAssignment");     
        pm =  (PluginManager)props.get("pluginManager");
        wm = (WorkflowManager) pm.getBean("workflowManager");

        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        
        String id = appService.getOriginProcessId(wfAssignment.getProcessId());

        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

        String blast_type = getPropertyString("blast_type");   
        
        try(Connection con = ds.getConnection();) {
            String subject = "";
            String msg = "";
            String newSubject = "";
            String newMsg = "";
            String sendTo = "";
            String userName = "";
            String userId = "";
            String templateRawId ="";
            String newAppType = "";
            Map<String, String> template;
            
            if(blast_type.equals("potential_participant")){
                LogUtil.info("HRDC - EVENT - Email Template Mapper Plugin ---->","Blast for?: " + blast_type);

                String sql1 = "SELECT c_ccd_emailTemplate FROM app_fd_event WHERE id = ?";

                PreparedStatement stmt1 = con.prepareStatement(sql1);
                stmt1.setString(1, id);
                ResultSet rs1 = stmt1.executeQuery();

                if(rs1.next()){
                    templateRawId = rs1.getString("c_ccd_emailTemplate");
                }

                String[] rawValue = templateRawId.split("_"); //0-templateid, 1-workflow

                if(StringUtils.isBlank(templateRawId)){
                    LogUtil.info("HRDC - EVENT - Email Blast Mapper Plugin ---->","templateRawId is blank: " + templateRawId);
                }

                if(rawValue.length < 2){
                    LogUtil.info("HRDC - EVENT - Email Blast Mapper Plugin ---->","rawValue has length less than 2: " + rawValue);
                }

                String templateId = rawValue[0]==null?"":rawValue[0];
                String appType = rawValue[1]==null?"":rawValue[1];

                if(appType.equals("Event Creation") || appType.equals("Event Registration") || appType.equals("Event Amendment")){
                    newAppType = "event_creation";
                }else if(appType.equals("Event Registration") || appType.equals("Officer Modifying") || appType.equals("Class Cancellation")){
                    newAppType = "event_registration";
                }

                LogUtil.info("HRDC - EVENT - Email Blast Mapper Plugin ---->","template id: " + templateId);
                LogUtil.info("HRDC - EVENT - Email Blast Mapper Plugin ---->","appType: " + newAppType);

                String sql = "SELECT part.id as participant_id,emp.c_empl_email_pri,emp.c_comp_name,emp.id as employer_id FROM app_fd_evnt_sv_participant part "
                            + "JOIN "
                            + "app_fd_empm_reg emp ON part.c_participant_id = emp.id "
                            + "LEFT JOIN "
                            + "app_fd_stp_city city ON emp.c_empl_city = city.id "
                            + "LEFT JOIN "
                            + "app_fd_stp_state state ON emp.c_empl_state = state.id "
                            + "WHERE "
                            + "part.c_parent_id = ?;";

                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                template = EmailTemplate.getTemplateFromEventCreation(templateId, con);

                while (rs.next()) {
                    sendTo = rs.getString("c_empl_email_pri");
                    userName = rs.getString("c_comp_name");
                    userId = rs.getString("participant_id");
                    subject = template.get("c_template_subject");
                    msg = template.get("c_template_content");

                    LogUtil.info("HRDC - EVENT - Email Template Mapper Plugin ---->","email: " + sendTo);
                    LogUtil.info("HRDC - EVENT - Email Template Mapper Plugin ---->","username: " + userName);
                    LogUtil.info("HRDC - EVENT - Email Template Mapper Plugin ---->","userId: " + userId);

                    newSubject = EmailTemplate.buildContent(newAppType, "", id, subject, con);
                    newMsg = EmailTemplate.emailContentHeader(newSubject) + EmailTemplate.buildContentWithUserInfo(newAppType, "", "", msg, userId, blast_type, con) + EmailTemplate.emailContentFooter();

                    SendEmail.sendEmail("Email Blast Mapper Plugin", sendTo, "", newSubject, newMsg);
                }

            }else{
                LogUtil.info("HRDC - EVENT - Email Template Mapper Plugin ---->","Blast for?: " + blast_type);
                String sql = "SELECT  u.* FROM  app_fd_event_reg_user u LEFT JOIN app_fd_event_registration r ON u.c_parentId = r.id WHERE r.id = ?;";
                String mail_template = getPropertyString("mail_template");
                String appType = getPropertyString("app_type");
            
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                String eventId = getEventIdUsingEventRegisterId(id, con);

                template = EmailTemplate.getTemplate(mail_template, con);
                while (rs.next()) {
                    sendTo = rs.getString("c_evnt_pd_email_add");
                    userName = rs.getString("c_evnt_pd_name");
                    userId = rs.getString("id");
                    subject = template.get("c_template_subject");
                    msg = template.get("c_template_content");

                    LogUtil.info("HRDC - EVENT - Email Template Mapper Plugin ---->","subject: " + subject);

                    newSubject = EmailTemplate.buildContent(appType, "", eventId, subject, con);
                    newMsg = EmailTemplate.emailContentHeader(newSubject) + EmailTemplate.buildContentWithUserInfo(appType, "", eventId, msg, userId, blast_type, con) + EmailTemplate.emailContentFooter();

                    SendEmail.sendEmail("Email Blast Mapper Plugin", sendTo, "", newSubject, newMsg);
                }

            }
            
        } catch (Exception ex) {
            // Handle the exception, e.g., log or print the error message
            ex.printStackTrace();
        }

        return null;
    }

    private static String getEventIdUsingEventRegisterId(String id, Connection con){
        String res = "";
        String sql = "SELECT c_eventId FROM app_fd_event_registration WHERE id = ?;";
    
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                res = rs.getString("c_eventId");
            }
        } catch (Exception e) {
            LogUtil.error("HRDC - EVENT - Email Template Mapper Plugin ----->", e, "Error getting event id from event register id");
        }
        
        return res;
    }
}
