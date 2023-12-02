package com.hrdcorp.ncs_dev.binder;

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

// This is used to load email template to a text rich field in a form of https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/form/builder/course_user_email (course_user_email).
// When selecting email template from (mailTemplate) form: https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/form/builder/cr_review_approver,
// this will pass template id to course_user_email form. Which this plugin is respondsible to load the template, and populate the template.

// The reason its called EventSaveEmailTemplateBinder instead of EventLoadEmailTemplateBinder, is because the main purpose of loading and populate data for the template is to save the email to form course_user_email with table name of app_fd_course_user_email

public class EventPreviewEmailTemplateBinder extends WorkflowFormBinder {

    @Override
    public String getName() {
        return ("HRDC - EVENT - Preview Email Template Binder");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To preview email template from ajax subform");
    }

    @Override
    public String getLabel() {
        return ("HRDC - EVENT - Preview Email Template Binder");
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

            String[] rawValue = id.split("_"); //0-templateid, 1-parent id

            // LogUtil.info("HRDC - EVENT - Preview Email Template Binder ---->","rawValue Length = " + rawValue.length);

            if(StringUtils.isBlank(id) || StringUtils.isBlank(id)){
                return rows;
            }

            if(rawValue.length < 2){
                return rows;
            }
            String templateId = rawValue[0]==null?"":rawValue[0];
            String appType = rawValue[1]==null?"":rawValue[1];

            String c_template_subject = "";
            String c_template_content = "";
            String query = "SELECT c_template_subject, c_template_content FROM app_fd_stp_evt_template WHERE id=?";

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, templateId);
            ResultSet rs = stmt.executeQuery(); 

            if(rs.next()) {
                c_template_subject = rs.getString("c_template_subject");
                c_template_content = rs.getString("c_template_content");
            }
            row.setProperty("template_subject", c_template_subject);
            row.setProperty("template_content", c_template_content);
            row.setProperty("moduleType", appType);

            rows.add(row);

            
        }catch(Exception ex){
            LogUtil.error("HRDC - EVENT - Preview Email Template Binder ----->", ex, "Error loading template");
        }
        return rows;
    }
    
    
}
