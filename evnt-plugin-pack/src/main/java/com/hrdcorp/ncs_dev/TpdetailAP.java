package com.tms.hrdc.eventscreation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.springframework.beans.BeansException;


 
public class TpdetailAP extends WorkflowFormBinder {

    @Override
    public String getName() {
        return "HRDC Events TP_Details_AP";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "HRDC Events TP_Details_AP";
    }

    @Override
    public String getLabel() {
        return "HRDC Events TP_Details_AP";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public Object getProperty(String property) {
        return super.getProperty(property);
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
    FormRowSet rows = new FormRowSet();
    
  
//     
//     String name = "";
//     String email = "";
     String  tp_name_of_tp = "";
     String  tp_myCoid = "";
     String  tp_email = "";
     String  tp_tel_no = "";
//     String  tp_mob_phone_no = "";                
     String  tp_address= "";
     String  tp_postcode= "";
     String  tp_city= "";
     String   tp_state= "";
    
             
//     String state = "";
//        LogUtil.info("0","takde");
        
     if (primaryKey != null && !primaryKey.isEmpty()) {
        Connection con = null;
        
        FormRow row = new FormRow();
        
//        LogUtil.info("1","ada");
        try {
            // retrieve connection from the default datasource
            DataSource ds = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");
            con = ds.getConnection();
          
            // execute SQL query
            if(!con.isClosed()) {
                PreparedStatement stmt = con.prepareStatement("select \n" +
            "c_training_organization_name, c_tp_new_myCoID,c_comp_email,c_comp_tel_no,c_tp_address,c_tp_postcode,c_co_city,c_tp_state from app_fd_trm_tpr where  c_training_organization_name = ? ");
                stmt.setObject(1, primaryKey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                
                    tp_name_of_tp = (rs.getObject("c_training_organization_name") != null)?rs.getObject("c_training_organization_name").toString():"";
                    tp_myCoid = (rs.getObject("c_tp_new_myCoID") != null)?rs.getObject("c_tp_new_myCoID").toString():"";
                    tp_email = (rs.getObject("c_comp_email") != null)?rs.getObject("c_comp_email").toString():"";
                    tp_tel_no = (rs.getObject("c_comp_tel_no") != null)?rs.getObject("c_comp_tel_no").toString():"";
//                    tp_mob_phone_no = (rs.getObject("c_comp_tel_no") != null)?rs.getObject("c_comp_tel_no").toString():"";
                    tp_address = (rs.getObject("c_tp_address") != null)?rs.getObject("c_tp_address").toString():"";
                    tp_postcode = (rs.getObject("c_tp_postcode") != null)?rs.getObject("c_tp_postcode").toString():"";
                    tp_city = (rs.getObject("c_co_city") != null)?rs.getObject("c_co_city").toString():"";
                    tp_state = (rs.getObject("c_tp_state") != null)?rs.getObject("c_tp_state").toString():"";
                   
            
//                   LogUtil.info("2","beanshell in Process "+name+" "+email);
                     
                    // rows.add(row);
                    // break;
                }
            }
        } catch(SQLException e) {
        } catch (BeansException e) {
        } finally {
            //always close the connection after used
            try {
                if(con != null) {
                    con.close();
                }
            } catch(SQLException e) {/* ignored */}
        }
//        row.setProperty("state",state);
//        row.setProperty("co_name",name);
//        row.setProperty("tmp_usr_email",email);
        row.setProperty("training_organization_name", tp_name_of_tp);
        row.setProperty("tp_new_myCoID",tp_myCoid);
        row.setProperty("comp_email",tp_email);
        row.setProperty("comp_tel_no",tp_tel_no);
//        row.setProperty("comp_tel_no",tp_mob_phone_no);
        row.setProperty("tp_address",tp_address);
        row.setProperty("tp_postcode",tp_postcode);
        row.setProperty("co_city",tp_city);
        row.setProperty("tp_state",tp_state);
        rows.add(row);
        
        
    
    return rows;
    
    
}
       
   return null;
    }
}
       
    



   
//call load method with injected variable
//return load(element, primaryKey, formData);
