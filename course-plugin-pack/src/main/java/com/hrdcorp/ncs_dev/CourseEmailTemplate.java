package com.hrdcorp.ncs_dev;

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

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;

import com.hrdcorp.ncs_dev.util.EmailTemplate;
import com.hrdcorp.ncs_dev.util.SendEmail;

public class CourseEmailTemplate extends DefaultApplicationPlugin{
    
    @Override
    public String getName() {
        return ("HRDC - COURSE - Email Template Mapper");
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
        return ("HRDC - COURSE - Email Template Mapper");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/EmailTemplate.json", null, true);
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

        String from = getPropertyString("from");        
        String to = getPropertyString("to");
        String to_pt_id = getPropertyString("to_pt_id");
        String cc = getPropertyString("cc");
        String cc_pt_id = getPropertyString("cc_pt_id");
        String bcc = getPropertyString("bcc");
        String bcc_pt_id = getPropertyString("bcc_pt_id");
        String subject = getPropertyString("bcc_pt_id");

        String app_type = getPropertyString("app_type");

        String non_template_setup = getPropertyString("non_template_setup");
        String mail_template = getPropertyString("mail_template");

        String receiver = "";
        String cc_receiver = "";

        AppDefinition appDef = (AppDefinition) props.get("appDef");

        // get participant email addresses
        if ((to_pt_id != null && to_pt_id.trim().length() != 0) || (to != null && to.trim().length() != 0)) {
            Collection<String> tss = AppUtil.getEmailList(to_pt_id, to, wfAssignment, appDef);
            for (String address : tss) {
                receiver += StringUtil.encodeEmail(address)+",";  
            }
        } else {
            LogUtil.info("HRDC - COURSE - Email Template Mapper Plugin ---->","Email not found");
        }

        // get cc email adresses
        if ((cc_pt_id != null && cc_pt_id.trim().length() != 0) || (cc != null && cc.trim().length() != 0)) {
            Collection<String> tss = AppUtil.getEmailList(cc_pt_id, cc, wfAssignment, appDef);
            for (String address : tss) {
                cc_receiver += StringUtil.encodeEmail(address)+",";  
            }
        } else {
            LogUtil.info("HRDC - COURSE - Email Template Mapper Plugin ---->","CC not specified");
        }

        if(cc_receiver.isEmpty()){
            cc_receiver = to_pt_id;
        }
        
        try(Connection con = ds.getConnection();) {
            String msg = "";
            String newSubject = "";
            String newMsg = "";
            String sendTo = "";
            Map<String, String> template;

            LogUtil.info("HRDC - COURSE - Email Template Mapper Plugin ---->","Non-template?: " + non_template_setup);

            if(non_template_setup.equals("true")){
                LogUtil.info("HRDC - COURSE - Email Template Mapper Plugin ---->","Non-template email: " + id);

                template = EmailTemplate.getTemplateFromUser(id, con);

                if(subject.isEmpty()){
                    subject = template.get("c_template_subject");
                }
                msg = template.get("c_template_content");
                sendTo = template.get("c_additional_email");

                receiver += sendTo;

                newSubject = EmailTemplate.buildContent(app_type, id, subject, con);

                newMsg = EmailTemplate.emailContentHeader(newSubject) + EmailTemplate.buildContent(app_type, id, msg, con) + EmailTemplate.emailContentFooter();

            }else{
                LogUtil.info("HRDC - COURSE - Email Template Mapper Plugin ---->","Template email: " + id);

                template = EmailTemplate.getTemplate(mail_template, con);

                if(subject.isEmpty()){
                    subject = template.get("c_template_subject");
                }
                msg = template.get("c_template_content");

                newSubject = EmailTemplate.buildContent(app_type, id, subject, con);

                newMsg = EmailTemplate.emailContentHeader(newSubject) + EmailTemplate.buildContent(app_type, id, msg, con) + EmailTemplate.emailContentFooter();
            }
            
            // LogUtil.info("HRDC - COURSE - Email Template Mapper Plugin ---->","Receiver: " + receiver);
            SendEmail.sendEmail("Email Template Mapper Plugin", receiver, "", newSubject, newMsg);

            
        } catch (Exception ex) {
            // Handle the exception, e.g., log or print the error message
            ex.printStackTrace();
        }

        return null;
    }

}
