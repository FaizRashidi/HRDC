/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

/**
 *
 * @author user
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;


public class CR_Audit_Trail_cr extends DefaultApplicationPlugin{   
   
    @Override
    public String getName() {
        return "HRDC - Audit Trail -CR";
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getDescription() {
        return "HRDC - Audit Trail -CR";
    }

    @Override
    public String getLabel() {
        return "HRDC - Audit Trail -CR";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/cr_audit_trail.json", null, true);
    }
    
    @Override
    public Object execute(Map map) {
        
        String act_type = map.get("act_type").toString();
        String flow_type = map.get("flow_type").toString();
        
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        WorkflowAssignment workflowAssignment = (WorkflowAssignment) map.get("workflowAssignment");
        String reqId = appService.getOriginProcessId(workflowAssignment.getProcessId()); // Get record Id from process
        String reqId_1 = appService.getOriginProcessId(workflowAssignment.getProcessId()); // Get record Id from process
      
        //LogUtil.info("AUDIT TEST","reqId: "+reqId);
        
        String dateModified = "", name = "", status = "" ;
        
           LogUtil.info("HRDC-CR-AuditTrail","reqId:1 "+reqId);
        // LogUtil.info("AUDIT TEST","wan 1");
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        
        if(flow_type.equals("micas")){
            
            try {
                handleAuditMicas(reqId, act_type);
            } catch (SQLException ex) {
                Logger.getLogger(CR_Audit_Trail_cr.class.getName()).log(Level.SEVERE, null, ex);
            }
           }else if(flow_type.equals("nonmicas")){
            try {
                handleAuditNonMicas(reqId_1,act_type);
            } catch (SQLException ex) {
                Logger.getLogger(CR_Audit_Trail_cr.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
            
           String sql = "SELECT * FROM app_fd_creg_mcs where id = ?" ;



           String sql_1 = "select  * from  app_fd_creg WHERE id = ?" ;
                   
          LogUtil.info("HRDC-CR-AuditTrail","reqId:2 "+reqId);
         
        try { 
            
            con = ds.getConnection();  
            
            PreparedStatement stmt;    
            stmt = con.prepareStatement(sql);
            stmt.setString(1, reqId);      
            
            PreparedStatement stmt_1 = con.prepareStatement(sql_1);    
            stmt_1.setString(1, reqId);
            
            ResultSet rt = stmt_1.executeQuery();           
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){
                    

            } 
                 
            }catch (SQLException ex) {
                 ex.printStackTrace();
                }finally {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(CR_Audit_Trail_cr.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

        return null;
        
    }
       
    public void handleAuditMicas(String id, String act_type) throws SQLException{
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = ds.getConnection();
            String sql = "SELECT * FROM app_fd_creg_mcs where id = ?" ;
            stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            
            LogUtil.info("HRDC-CR-AuditTrail","reqId:3");
            
            ResultSet rs = stmt.executeQuery();
            
            
            
            String verify = "", auditStatus = "", audRemarks = "", dateModified = "", name = "" ,query = "";
            String submit = "";
            
            if(rs.next()){
                do{
                    verify = rs.getObject("c_micas_status") == null?"":rs.getString("c_micas_status");
                    audRemarks = rs.getObject("c_remarks") == null?"":rs.getString("c_remarks");
                    dateModified = rs.getString("dateModified");
                    name = rs.getString("modifiedByName");
                    query = rs.getObject("c_micas_status") == null?"":rs.getString("c_micas_status");
                    
                    
                   
                    
                }while(rs.next());
            }
            
            if(act_type.equals("submit")){
             
                 auditStatus = "Submitted by "+name;
                    
            }
           
            if(act_type.equals("verify")){
                
                 if(verify.equals("APPROVED")){
                     
                     auditStatus = " Approved by "+name;
                     
                 }else{
                     
                     auditStatus = " Rejected by "+name;
                 }
                    
            }
            
            if(act_type.equals("query")){
                
                 if(query.equals("QUERY")){
                     
                     auditStatus = " Query by "+name;
                 }
                
            }
            
            
        
                  
            
                LogUtil.info("HRDC-CR-AuditTrail","reqId:4");
                
           sql = "INSERT INTO app_fd_audit_trail_micas (id, dateCreated, dateModified, modifiedByName, c_micas_status, c_fk_mc) "
                  + "VALUES (UUID(), NOW(),NOW(),?, ?, ?)";
           
            stmt = con.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, auditStatus);
            stmt.setString(3, id);
            
               LogUtil.info("HRDC-CR-AuditTrail","reqId:5");
               
                  int i = stmt.executeUpdate();   


                    if(i>0){
                        LogUtil.info("HRDC-CR-AuditTrail","insert micas");
                    }else{
                        LogUtil.info("HRDC-CR-AuditTrail","Not insert micas");
                    }
                    
                    
                    
                    
            
        } catch (SQLException ex) {
            Logger.getLogger(CR_Audit_Trail_cr.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            con.close();
        }
        
        
    }
    
    public void handleAuditNonMicas(String id, String act_type) throws SQLException{
        
    DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
    Connection con = null;
    PreparedStatement stmt = null;
    try {
        con = ds.getConnection();
        String sql = "select  * from  app_fd_creg WHERE id = ?" ;
        stmt = con.prepareStatement(sql);
        stmt.setString(1, id);
        
        ResultSet rs = stmt.executeQuery();
        
            LogUtil.info("HRDC-CR-AuditTrail","reqId:6");
        
        String verify = "", auditStatus = "", audRemarks = "", dateModified = "", name = "" , query = "" , submit = "";
        
        if(rs.next()){
            do{
                verify = rs.getObject("c_course_status") == null?"":rs.getString("c_course_status");
                audRemarks = rs.getObject("c_remarks") == null?"":rs.getString("c_remarks");
                dateModified = rs.getString("dateModified");
                name = rs.getString("modifiedByName");
                query = rs.getObject("c_course_status") == null?"":rs.getString("c_course_status");
            }while(rs.next());
        }
        
        
        
        if(act_type.equals("submit")){
            
                auditStatus = "Submitted by "+name;
                
        }
        
        if(act_type.equals("verify")){
            
                if(verify.equals("APPROVED")){
                    
                    auditStatus = " Approved by "+name;
                    
                }else{
                    
                    auditStatus = " Rejected by "+name;
                }
                
        }
        
        if(act_type.equals("query")){
            
                if(query.equals("QUERY")){
                    
                    auditStatus = " Query by "+name;
                }
            
        }
        
        
            LogUtil.info("HRDC-CR-AuditTrail","reqId:7");
            
        String sql_1 = "INSERT INTO app_fd_audit_trail_non_mc (id, dateCreated, dateModified, modifiedByName, c_course_status, c_fk_cr) "
                + "VALUES (UUID(), NOW(),NOW(),?, ?, ?)";
            
        stmt = con.prepareStatement(sql_1);
        stmt.setString(1, name);
        stmt.setString(2, auditStatus);
        stmt.setString(3, id);
        
        LogUtil.info("HRDC-CR-AuditTrail","reqId:8");
        
                int i = stmt.executeUpdate();   


                if(i>0){
                    LogUtil.info("HRDC-CR-AuditTrail","insert Non-micas");
                }else{
                    LogUtil.info("HRDC-CR-AuditTrail","Not insert Non-micas");
                }
                
                
                
                
        
    } catch (SQLException ex) {
        Logger.getLogger(CR_Audit_Trail_cr.class.getName()).log(Level.SEVERE, null, ex);
    }finally{
        con.close();
    }
    
        LogUtil.info("HRDC-CR-AuditTrail","Success");
    }
    
}