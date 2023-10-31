package com.hrdcorp.ncs_dev;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
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

public class CourseSaveEmailTemplateBinder extends WorkflowFormBinder {

    @Override
    public String getName() {
        return ("HRDC - COURSE - Save Email Template Binder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To save user email template from ajax subform");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Save Email Template Binder");
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

            LogUtil.info("HRDC - COURSE - Save Email Template Binder ---->","rawId = " + id);

            String[] rawValue = id.split("_"); //0-templateid, 1-parent id


            if(StringUtils.isBlank(id) || StringUtils.isBlank(id)){
                return rows;
            }

            UuidGenerator uuid = UuidGenerator.getInstance();
            String pId = uuid.getUuid();

            
            String templateId = rawValue[0]==null?"":rawValue[0];
            String appType = rawValue[0]==null?"":rawValue[1];
            String parentId = rawValue[1]==null?"":rawValue[2];

            // Map<String, String> template = EmailTemplate.getTemplate(templateId, con);

            // String subject = template.get("c_template_subject");
            // String msg = template.get("c_template_content");

            String c_template_subject = "";
            String c_template_content = "";
            String newAppType = "";
            String newSubject = "";
            String newContent = "";

            String query = "SELECT c_template_subject, c_template_content FROM app_fd_stp_template WHERE id=?";

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, templateId);
            ResultSet rs = stmt.executeQuery(); 

            if(rs.next()) {
                c_template_subject = rs.getString("c_template_subject");
                c_template_content = rs.getString("c_template_content");
            }

            if(appType.equals("Course Register") || appType.equals("Course Registration") || appType.equals("Course Amendment")){
                newAppType = "course_register";
            }else if(appType.equals("Class Creation") || appType.equals("Class Amendment") || appType.equals("Class Cancellation")){
                newAppType = "class_amendment";
            }else if(appType.equals("License Training Material")){
                newAppType = "license_training_material";
            }
            LogUtil.info("HRDC - COURSE - Save Email Template Binder ---->","appType: "+appType);
            LogUtil.info("HRDC - COURSE - Save Email Template Binder ---->","newAppType: "+newAppType);

            newSubject = EmailTemplate.buildContent(newAppType, parentId, c_template_subject, con);
            newContent = EmailTemplate.buildContent(newAppType, parentId, c_template_content, con);

            row.setProperty("id", pId);
            row.setProperty("parentId", parentId);
            row.setProperty("moduleType", appType);
            row.setProperty("template_subject", newSubject);
            row.setProperty("additional_email", "");
            row.setProperty("template_content", newContent);

            rows.add(row);

            
        }catch(Exception ex){
            LogUtil.error("HRDC - COURSE - Save Email Template Binder ----->", ex, "Error loading template");
        }
        return rows;
    }
    
    
}
