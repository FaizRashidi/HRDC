/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hrdcorp.ncs_dev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.UuidGenerator;

/**
 *
 * @author Fawad Khaliq <khaliq@opendynamics.com.my>
 */
public class EvntAuditTrailBinder extends WorkflowFormBinder {

    public EvntAuditTrailBinder getSuper() {
        return this;
    }

    Connection con = null;
    
    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {

        super.store(element, rows, formData);
        
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
                String action_review_status = row.getProperty("action_review_status");
                
                String insertSql = "INSERT INTO app_fd_evnt_auditTrail (dateCreated,dateModified,c_action_date,id,createdBy,createdByName,modifiedBy,modifiedByName,c_action_workflow,c_action_activity,c_parentId,c_action_name,c_action_department,c_action_query_subject,c_action_additional_email,c_action_query_reason,c_action_remarks,c_status,c_action_review_status)"
                        + "VALUES (NOW(),NOW(),NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                 
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
                stmtInsert.setString(16, action_review_status);
                
                //Execute SQL statement
                stmtInsert.executeUpdate();
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
        return rows;
    }
    
    // Method to format date as per the required database format
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public String getName() {
        return ("HRDC - EVNT - Audit Trail Binder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To update audit trail record to database");
    }

    @Override
    public String getLabel() {
        return ("HRDC - EVNT - Audit Trail Binder");
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