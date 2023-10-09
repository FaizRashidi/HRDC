/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;

/**
 *
 * @author farih
 */
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
    public Object execute(Map props) {
       
        wfAssignment = (WorkflowAssignment) props.get("workflowAssignment");     
        pm =  (PluginManager)props.get("pluginManager");
        wm = (WorkflowManager) pm.getBean("workflowManager");
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        
        String id = appService.getOriginProcessId(wfAssignment.getProcessId());
        String status = wm.getProcessVariable(wfAssignment.getProcessId(), "status");
        String processId = wfAssignment.getProcessId();
        
        LogUtil.info("HRDC COURSE Audit Trail Workflow Plugin ---->","Record ID = " + id);
        LogUtil.info("HRDC COURSE Audit Trail Workflow Plugin ---->","Process ID = " + processId);
        LogUtil.info("HRDC COURSE Audit Trail Workflow Plugin ---->","Status = " + status);
        
        

        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
                        
        try {
            String sql = null;
            
            if (processId.endsWith("course_register") || processId.endsWith("course_amendment")){
                sql = "SELECT * FROM app_fd_course_register where id = ?;" ;
            }else if(processId.endsWith("class_creation") || processId.endsWith("class_amendment") || processId.endsWith("class_cancellation")){
                sql = "SELECT * FROM app_fd_course_class where id = ?;" ;
            }else if(processId.endsWith("license_training_material")){
                sql = "SELECT * FROM app_fd_course_ltm where id = ?;" ;
            }
            con = ds.getConnection();
                       
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            
            ResultSet rs = stmt.executeQuery();          
            
                       
            if(rs.next()){
                    
                UuidGenerator uuid = UuidGenerator.getInstance();
                String pId = uuid.getUuid();
                String parentId = id;

                String activityName = rs.getString("c_action_activity");
                String workflowName = rs.getString("c_action_workflow");

                String username = rs.getString("c_action_username");
                String name = rs.getString("c_action_name");
                String department = rs.getString("c_action_department");

                String querySubject = rs.getString("c_action_query_subject")!= null ?  rs.getString("c_action_query_subject") : "";
                String queryAddEmail = rs.getString("c_action_additional_email")!= null ? rs.getString("c_action_additional_email") : "";
                String queryReason = rs.getString("c_action_query_reason")!= null ? rs.getString("c_action_query_reason") : "";
                String remarks = rs.getString("c_action_remarks");
                String action_status = rs.getString("c_status");
                String action_attachment = rs.getString("c_action_attachment");
                String action_review_status = rs.getString("c_action_review_status");
                

                String insertSql = "INSERT INTO app_fd_course_audit (dateCreated,dateModified,c_action_date,id,createdBy,createdByName,modifiedBy,modifiedByName,c_action_workflow,c_action_activity,c_parentId,c_action_name,c_action_department,c_action_query_subject,c_action_additional_email,c_action_query_reason,c_action_remarks,c_status,c_action_attachment,c_action_review_status)"
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
                stmtInsert.executeUpdate();
            }
        }catch (Exception ex){
            LogUtil.error("HRDC COURSE Audit Trail Workflow Plugin ---->", ex, "Error Connecting to DB, Audit Trail not saved");
        }finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error("HRDC COURSE Audit Trail Workflow Plugin ---->", ex, "Error closing the jdbc connection");
            }
        } 
        
        
        
        return null;
    }
}
