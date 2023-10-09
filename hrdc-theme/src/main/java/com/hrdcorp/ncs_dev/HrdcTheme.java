/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

import java.util.Map;
import java.util.Set;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.enterprise.UniversalTheme;

/**
 *
 * @author farih
 */
public class HrdcTheme extends UniversalTheme{
    
    @Override
    public String getName() {
        return "HRDC Theme";
    }
 
    @Override
    public String getVersion() {
        return "7.0.0";
    }
 
    @Override
    public String getDescription() {
        return "HRDC Theme based on Universal Theme to support PWA";
    }
 
    @Override
    public String getLabel() {
        return getName();
    }
 
    @Override
    public String getClassName() {
        return getClass().getName();
    }
     
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/HrdcTheme.json", null, true, "messages/userview/EvaTheme");
    }
    
    
    @Override
    public String getJsCssLib(Map<String, Object> data) {
        String cssJs = super.getJsCssLib(data);

        //change where needed to to inject custom css on top of the universal theme
        String bn = ResourceBundleUtil.getMessage("build.number");
        cssJs += "<link href=\"" + data.get("context_path") + "/plugin/"+getClassName()+"/hrdc.css?build=" + bn + "\" rel=\"stylesheet\" />\n";
        cssJs += "<script src=\"" + data.get("context_path") + "/plugin/"+getClassName()+"/hrdc.js?build=" + bn + "\" async></script>\n";

        //overrides with user's selected form image background if set
        if(getProperty("form_image") != null && !getProperty("form_image").toString().isEmpty()){
            cssJs += "<style type=\"text/css\">";
            cssJs += ".form-container{ background-image : url('" + getProperty("form_image").toString() + "')}";
            cssJs += "</style>";
        }
        return cssJs;
    }

    @Override
    public Set<String> getOfflineStaticResources() {
        String contextPath = AppUtil.getRequestContextPath();
        String bn = ResourceBundleUtil.getMessage("build.number");
        Set<String> urls = super.getOfflineStaticResources();
        urls.add(contextPath + "/plugin/"+getClassName()+"/hrdc.css?build=" + bn);
        urls.add(contextPath + "/plugin/"+getClassName()+"/hrdc.js?build=" + bn);

        return urls;
    }
}
