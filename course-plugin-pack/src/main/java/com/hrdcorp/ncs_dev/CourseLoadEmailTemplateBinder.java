package com.hrdcorp.ncs_dev;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

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

import com.hrdcorp.ncs_dev.util.EmailTemplate;

public class CourseLoadEmailTemplateBinder  extends WorkflowFormBinder {

    @Override
    public String getName() {
        return ("HRDC - COURSE - Load Email Template Binder");
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
        return ("HRDC - COURSE - Load Email Template Binder");
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
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {

        FormRow row = rows.get(0);

        String id = formData.getPrimaryKeyValue();
        String template_content = row.getProperty("c_template_content");
        String template_subject = row.getProperty("c_template_subject");
        String app_type = row.getProperty("c_moduleType");

        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

        try(Connection con = ds.getConnection();){

            String tableName = "";

            LogUtil.info("HRDC - COURSE - Load Email Template Binder ----->", "appType: " +app_type);
            LogUtil.info("HRDC - COURSE - Load Email Template Binder ----->", "id: " +id);
            LogUtil.info("HRDC - COURSE - Load Email Template Binder ----->", "subject: "+template_subject);
            LogUtil.info("HRDC - COURSE - Load Email Template Binder ----->", "content: "+template_content);

            if(app_type.equals("Course Registrer") || app_type.equals("Course Registration") || app_type.equals("Course Amendment")){
                tableName = "app_fd_course_register";
            }else if(app_type.equals("Class Creation") || app_type.equals("Class Amendment") || app_type.equals("Class Cancellation")){
                tableName = "app_fd_course_class";
            }else if(app_type.equals("License Training Material")){
                tableName = "app_fd_course_ltm";
            }

            String sql = "UPDATE ? SET c_action_query_subject =?, c_action_query_reason=? WHERE id=?";

            PreparedStatement updateSql = con.prepareStatement(sql);

            updateSql.setString(1, tableName);
            updateSql.setString(2, template_subject);
            updateSql.setString(3, template_content);
            updateSql.setString(4, id);

            updateSql.executeUpdate();

        }catch(Exception ex){
            LogUtil.error("HRDC - COURSE - Load Email Template Binder ----->", ex, "Error Updating template content");

        }

        return rows;
    }
}
