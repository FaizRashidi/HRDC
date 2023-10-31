package com.hrdcorp.ncs_dev.util;

import java.util.Map;

import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.lib.EmailTool;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.property.service.PropertyUtil;

public class SendEmail {

    public static void sendEmail(String plugin_name, String user_email, String bcc, String subject, String msg) {  
        
        LogUtil.info("HRDC - COURSE - "+ plugin_name +" ---->"," Send Email Util: Actually Sending Email to: " +user_email);
        
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
            LogUtil.error("HRDC - COURSE - "+ plugin_name +" ---->", ex, "Error sending email");
        }
        
        LogUtil.info("HRDC - COURSE - "+ plugin_name +" ---->","Email successfully sent to participant: " + user_email);      
    }

}
