/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev.defaultPlugin;

import com.hrdcorp.ncs_dev.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

// This is used in workflow (workflow tool) to restore data after course amend is rejected.
// Used in: https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/process/builder#course_amendment > tool37_2: Data Update - Revert status to Approved AND tool37: Data Update - Revert status to Approved

public class CourseRestoreAmendData extends DefaultApplicationPlugin{
    @Override
    public String getName() {
        return ("HRDC - COURSE - Course Restore Amend Data");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To restore course amend data when the amendment is rejected");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Course Restore Amend Data");
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
        
        LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Record ID = " + id);
        // LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Process ID = " + processId);
        // LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Status = " + status);
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        
                        
        try(Connection con = ds.getConnection();) {

            String industryBased_history = "";
            String focus_area_history = "";
            String industry_history = "";
            String sector_history = "";
            String subsector_history = "";
            String target_group_history = "";
            String methodology_history = "";
            String pro_cert_history = "";
            String pro_cert_file_history = "";
            String action_remarks_history ="";
            String action_attachment_history="";

            String sql = "SELECT * FROM app_fd_course_register WHERE id = ?";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery(); 

            if(rs.next()) {
                industryBased_history = rs.getString("c_industryBased_history") != null || !rs.getString("c_industryBased_history").isEmpty() ? rs.getString("c_industryBased_history"):"";
                focus_area_history = rs.getString("c_focus_area_history") != null || !rs.getString("c_focus_area_history").isEmpty() ? rs.getString("c_focus_area_history"):"";
                industry_history = rs.getString("c_industry_history") != null || !rs.getString("c_industry_history").isEmpty() ? rs.getString("c_industry_history"):"";
                sector_history = rs.getString("c_sector_history") != null || !rs.getString("c_sector_history").isEmpty() ? rs.getString("c_sector_history"):"";
                subsector_history = rs.getString("c_subsector_history") != null || !rs.getString("c_subsector_history").isEmpty() ? rs.getString("c_subsector_history"):"";
                target_group_history = rs.getString("c_target_group_history")!= null || !rs.getString("c_target_group_history").isEmpty() ? rs.getString("c_target_group_history"):"";
                methodology_history = rs.getString("c_methodology_history") != null || !rs.getString("c_methodology_history").isEmpty() ? rs.getString("c_methodology_history"):"";
                pro_cert_history = rs.getString("c_pro_cert_history") != null || !rs.getString("c_pro_cert_history").isEmpty() ? rs.getString("c_pro_cert_history"):"";
                pro_cert_file_history = rs.getString("c_pro_cert_file_history") != null || !rs.getString("c_pro_cert_file_history").isEmpty() ? rs.getString("c_pro_cert_file_history"):"";
                action_remarks_history  = rs.getString("c_action_remarks_history") != null || !rs.getString("c_action_remarks_history").isEmpty() ? rs.getString("c_action_remarks_history"):"";
                action_attachment_history  = rs.getString("c_action_attchment_history") != null || !rs.getString("c_action_attchment_history").isEmpty() ? rs.getString("c_action_attchment_history"):"";
            }

            LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","c_ = " + target_group_history);

            updateValue(
                id,
                industryBased_history,
                focus_area_history,
                industry_history,
                sector_history,
                subsector_history,
                target_group_history,
                methodology_history,
                pro_cert_history,
                pro_cert_file_history,
                action_remarks_history,
                action_attachment_history, con
            );

        }catch (Exception ex){
            LogUtil.error("HRDC - COURSE - Audit Trail Workflow Plugin ---->", ex, "Error Connecting to DB, Audit Trail not saved");
        }
        return null;
    }

    public void updateValue(
        String id,
        String industryBased_history,
        String focus_area_history,
        String industry_history,
        String sector_history,
        String subsector_history,
        String target_group_history,
        String methodology_history,
        String pro_cert_history,
        String pro_cert_file_history,
        String action_remarks_history,
        String action_attachment_history,
        Connection con
    ) {
        LogUtil.info("HRDC - COURSE - Audit Trail Workflow Plugin ---->","Updating Databse. c_target_group: " + target_group_history);
        try {
            // Assuming you have a PreparedStatement preparedStmt and a Connection connection

            String query = "UPDATE app_fd_course_register SET c_industryBased=?, c_focus_area=?, c_industry=?, c_sector=?, c_subsector=?, c_target_group=?, c_methodology=?, c_pro_cert=?, c_pro_cert_file=?, c_action_remarks=?, c_action_attachment=? WHERE id=?";

            PreparedStatement preparedStmt = con.prepareStatement(query);

            preparedStmt.setString(1, industryBased_history);
            preparedStmt.setString(2, focus_area_history);
            preparedStmt.setString(3, industry_history);
            preparedStmt.setString(4, sector_history);
            preparedStmt.setString(5, subsector_history);
            preparedStmt.setString(6, target_group_history);
            preparedStmt.setString(7, methodology_history);
            preparedStmt.setString(8, pro_cert_history);
            preparedStmt.setString(9, pro_cert_file_history);
            preparedStmt.setString(10, action_remarks_history);
            preparedStmt.setString(11, action_attachment_history);
            preparedStmt.setString(12, id);

            preparedStmt.executeUpdate();
            // Optionally, you can close the PreparedStatement and Connection here
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any potential errors here
        }
    }
}
