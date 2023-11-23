/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import com.hrdcorp.ncs_dev.util.AuditTrail;

/**
 *
 * @author farih
 */

// This is used in workflow (workflow tool) to store audit trail after submitting form. ex: https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/process/builder#course_register > tool29: Custom - Save to Audit Trail

public class CourseAuditTrailWorkflowPlugin extends DefaultApplicationPlugin{
    @Override
    public String getName() {
        return ("HRDC - COURSE - Audit Trail Workflow Plugin");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To add audit trail through workflow plugin");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Audit Trail Workflow Plugin");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/cr_audit_trail.json", null, true);
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
        String status = wm.getProcessVariable(wfAssignment.getProcessId(), "status");
        String processId = wfAssignment.getProcessId();
        
        LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Record ID = " + id);
        // LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Process ID = " + processId);
        // LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Status = " + status);
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        
                        
        try(Connection con = ds.getConnection();) {

            // Call function to store audit trail from util (Only here audit trail is actually saved). Received 4 param, Plugin name, processId, id and connection.
            AuditTrail.addAuditTrail("Audit Trail Workflow Plugin","", processId, id, con);

        }catch (Exception ex){
            LogUtil.error("HRDC - COURSE - Audit Trail Workflow Plugin ---->", ex, "Error Connecting to DB, Audit Trail not saved");
        }
        
        
        
        return null;
    }
}
