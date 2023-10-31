package com.hrdcorp.ncs_dev.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.UuidGenerator;

public class AuditTrail {
    
    public static void addAuditTrail(String plugin_name, String update_type, String processId , String id, Connection con) {  
        
        try {
            String sql = null;
            
            if (processId.endsWith("course_register") || processId.endsWith("course_amendment") || processId.endsWith("course_amendment_officer")){
                sql = "SELECT * FROM app_fd_course_register where id = ?;" ;
            }else if(processId.endsWith("class_creation") || processId.endsWith("class_amendment") || processId.endsWith("class_cancellation")){
                sql = "SELECT * FROM app_fd_course_class where id = ?;" ;
            }else if(processId.endsWith("license_training_material")){
                sql = "SELECT * FROM app_fd_course_ltm where id = ?;" ;
            }else{
                LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Process id not recognized. Cannot update to Audit trail");
                return;
            }

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, id);
            
            ResultSet rs = stmt.executeQuery();          
            
                       
            if(rs.next()){
                LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Record exist, updatng Audit trail for selected record:"+ id);

                UuidGenerator uuid = UuidGenerator.getInstance();
                String pId = uuid.getUuid();
                String parentId = id;

                String activityName = rs.getString("c_action_activity");
                String workflowName = rs.getString("c_action_workflow");

                String username, name, department, remarks, action_status, action_attachment, action_review_status;

                if (update_type == "autoreject"){
                    LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Adding Auto Reject Audit Trail");

                    username = "System";
                    name = "System";
                    department = rs.getString("c_action_department");    

                    remarks = "Rejected by system";
                    action_status = "Rejected";
                    action_attachment = rs.getString("c_action_attachment");
                    action_review_status = "Rejected";
                }else{
                    LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Adding Audit Trail");

                    username = rs.getString("c_action_username");
                    name = rs.getString("c_action_name");
                    department = rs.getString("c_action_department");

                    remarks = rs.getString("c_action_remarks");
                    action_status = rs.getString("c_status");
                    action_attachment = rs.getString("c_action_attachment");
                    action_review_status = rs.getString("c_action_review_status");
                }
                
                
                String insertSql = "INSERT INTO app_fd_course_audit (dateCreated,dateModified,c_action_date,id,createdBy,createdByName,modifiedBy,modifiedByName,c_action_workflow,c_action_activity,c_parentId,c_action_name,c_action_department,c_action_remarks,c_status,c_action_attachment,c_action_review_status)"
                        + "VALUES (NOW(),NOW(),NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

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
                stmtInsert.setString(11, remarks);
                stmtInsert.setString(12, action_status);
                stmtInsert.setString(13, action_attachment);
                stmtInsert.setString(14, action_review_status);

                LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","audit Trail Executing");
                stmtInsert.executeUpdate();

                try{
                    String workflow_path = null;
                    if (workflowName.endsWith("Course Registration") || workflowName.endsWith("Course Amendment")){
                        workflow_path = "course_register";
                    }else if(workflowName.endsWith("Class Creation") || workflowName.endsWith("Class Amendment") || workflowName.endsWith("Class Cancellation")){
                        workflow_path = "course_class";
                    }else if(workflowName.endsWith("License Training Material")){
                        workflow_path = "course_ltm";
                    }
                    
                    String path = SetupManager.getBaseDirectory() + "app_formuploads/"+workflow_path+"/" + parentId+"/";
                    String newPath = SetupManager.getBaseDirectory() + "app_formuploads/course_audit/" +pId+"/";
                    
                    LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Checking if path exist: " + path);                    
                    
                    Path sourcePath = Paths.get(path, action_attachment);
                    Path destinationPath = Paths.get(newPath, action_attachment);
                    
                    
                    if (Files.exists(sourcePath)) {
                        LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Source path exist:" + path);      
                        if (!Files.exists(destinationPath.getParent())) {
                            LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Destination path not exist, Creating Folder"+ destinationPath);
                            
                            Files.createDirectories(destinationPath.getParent());
                        }
                                                
                        try {
                            LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Copying File");
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Copy Successful");
                        } catch (IOException e) {
                            e.printStackTrace();
                            LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Error copy");
                        }
                    }else{
                        LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Source path doesn't exist:" + path); 
                        if (!Files.exists(destinationPath.getParent())) {
                            LogUtil.info("HRDC - COURSE - "+plugin_name+" ---->","Path not exist, Creating Folder"+ destinationPath);
                            
                            Files.createDirectories(destinationPath.getParent());
                        }
                    }
                    
                    
                }catch (Exception ex){
                    LogUtil.error("HRDC - COURSE - "+plugin_name+" ---->", ex, "Error copying file to Audit trail from/table");
                }
            }
        }catch (Exception ex){
            LogUtil.error("HRDC - COURSE - "+plugin_name+" ---->", ex, "Error Adding Audit trail, Audit Trail not saved");
        }
    }

}
