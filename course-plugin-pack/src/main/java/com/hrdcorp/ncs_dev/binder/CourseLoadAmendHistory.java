package com.hrdcorp.ncs_dev.binder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.joget.apps.app.service.AppService;
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

import com.hrdcorp.ncs_dev.util.EmailTemplate;


//This is used to load/copy data to a history/new field in course form before sending amendment. The reason is because we save this data temporarily. When amend process is cancelled, this data will need to be inserted back to original field/column
// Used in: https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/form/builder/cr_nonmicas_sub1_rd (section9 > Load Data From)
            
public class CourseLoadAmendHistory  extends WorkflowFormBinder {

    @Override
    public String getName() {
        return ("HRDC - COURSE - Load Amend History");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To load previous data before amend (to be saved as history. So that when amend rejected, we can retrieve the old record)");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Load Amend History");
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
    
    @Override
    public FormRowSet load(Element element, String id, FormData formData) {

        FormRowSet rows = new FormRowSet();
        FormRow row = new FormRow();
        
        if(StringUtils.isBlank(id)){
            return rows;
        }

        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

        try(Connection con = ds.getConnection();){

            String industryBased_history = "";
            String focus_area_history = "";
            String industry_history = "";
            String sector_history = "";
            String subsector_history = "";
            String target_group_history = "";
            String methodology_history ="";
            String pro_cert_history ="";
            String pro_cert_file_history = "";
            String action_remarks_history = "";
            String action_attachment_history = "";

            String query = "SELECT * FROM app_fd_course_register WHERE id = ?";

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery(); 

            if(rs.next()) {
                
                industryBased_history = rs.getString("c_industryBased");
                focus_area_history = rs.getString("c_focus_area");
                industry_history = rs.getString("c_industry");
                sector_history = rs.getString("c_sector");
                subsector_history = rs.getString("c_subsector");
                target_group_history = rs.getString("c_target_group");
                methodology_history = rs.getString("c_methodology");
                pro_cert_history = rs.getString("c_pro_cert");
                pro_cert_file_history = rs.getString("c_pro_cert_file");
                action_remarks_history = rs.getString("c_action_remarks");
                action_attachment_history = rs.getString("c_action_attachment");

                row.setProperty("industryBased_history", industryBased_history!= null? industryBased_history : "");
                row.setProperty("focus_area_history",  focus_area_history!= null? focus_area_history : "");
                row.setProperty("industry_history",  industry_history!= null? industry_history : "");
                row.setProperty("sector_history",  sector_history!= null? sector_history : "");
                row.setProperty("subsector_history",  subsector_history!= null? subsector_history : "");
                row.setProperty("target_group_history",  target_group_history!= null? target_group_history : "");
                row.setProperty("methodology_history",  methodology_history!= null? methodology_history : "");
                row.setProperty("pro_cert_history",  pro_cert_history!= null? pro_cert_history : "");
                row.setProperty("pro_cert_file_history",  pro_cert_file_history!= null? pro_cert_file_history : "");
                row.setProperty("action_remarks_history",  action_remarks_history!= null? action_remarks_history : "");
                row.setProperty("action_attachment_history",  action_attachment_history!= null? action_attachment_history : "");


                rows.add(row);
            }

            
        }catch(Exception ex){
            LogUtil.error("HRDC - COURSE - Load Amend History ----->", ex, "Error loading template");
        }
        return rows;
    }
}
