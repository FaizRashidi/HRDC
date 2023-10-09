
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
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.LogUtil;


public class EvntEmailPotentialClient extends DefaultApplicationPlugin {

    @Override
    public String getName() {
        return "HRDC TRM Email Approve(wan)";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "HRDC TRM Email Approve(wan)";
    }
    
    @Override
    public String getLabel() {
        return "HRDC TRM Email Approve(wan)";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public Object getProperty(String property) {
        return super.getProperty(property);
    }
    
    @Override
    public String getPropertyOptions() {
      return "";
    }
    
    
    public static void sendEmail(String user_email, String bcc, String subject, String content, String formDefId, Object[] formUplField,Map map) {  

        EmailTool et = new EmailTool();

        PluginDefaultPropertiesDao dao = (PluginDefaultPropertiesDao) AppUtil.getApplicationContext().getBean("pluginDefaultPropertiesDao");
        PluginDefaultProperties pluginDefaultProperties = dao.loadById("org.joget.apps.app.lib.EmailTool", AppUtil.getCurrentAppDefinition());
        Map properties = PropertyUtil.getPropertiesValueFromJson(pluginDefaultProperties.getPluginProperties());
        
        

        properties.put("from", "no-reply@your-company-name.com");
        properties.put("toSpecific", user_email);
        properties.put("bcc", "bcc");
        properties.put("subject", subject);
        properties.put("message", "Notify Approved");
        properties.put("isHtml", "true");

        if(formDefId != null && formUplField != null && formUplField.length != 0) {
            properties.put("formDefId", formDefId);
            properties.put("fields", user_email);
        }

        et.execute(properties);
        
        LogUtil.info("HRDC EVENT Email Potential Participant ","email successfully sent to participant" + user_email);      
    }

    @Override
    public Object execute(Map map) {
        
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String reqId = appService.getOriginProcessId(workflowAssignment.getProcessId());
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
    
        String user_name = "",user_email = "";
        String subject = "";
        String msg = "";           

            LogUtil.info("HRDC EVENT Email Potential Participant","Process Id: "+ reqId);
            String sql = "Select * from app_fd_trm_soic ";
                        
        try{

            con = ds.getConnection();  

            PreparedStatement stmt;    
            stmt = con.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                do{
                    user_email = rs.getString("c_email");
                    user_name = rs.getString("c_name");
                }while(rs.next());
            }
            
            LogUtil.info("HRDC EVENT Email Potential Participant","Get user email: "+user_email);
            
            subject += "Invitation to #Event_Name#";
            msg += "body";
            
            sendEmail(user_email, "", subject, msg, "", null, null);

        }catch (SQLException ex) {
            LogUtil.info("HRDC EVENT Email Potential Participant","Could not get user email and name from DB");
        }finally {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(EvntEmailPotentialClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }    
}


    

      

            
            
   


