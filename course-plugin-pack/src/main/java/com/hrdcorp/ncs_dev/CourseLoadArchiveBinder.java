package com.hrdcorp.ncs_dev;

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


//This is used to load data to course form. Will check if id exist in archive table. If so, select from archive table and populate form. If not select from jwdb table and populate
//by default using super.load() function.
// Example used in: https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/form/builder/cr_nonmicas_sub1 (Setting > Load Data From)
            //  and https://ncs-dev.hrdcorp.gov.my/jw/web/console/app/course_registration_module/1/form/builder/cr_view (Setting > Load Data From)
            
public class CourseLoadArchiveBinder  extends WorkflowFormBinder {

    @Override
    public String getName() {
        return ("HRDC - COURSE - Load Archive Course");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To load archive course from archive table");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Load Archive Course");
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

            UuidGenerator uuid = UuidGenerator.getInstance();
            String pId = uuid.getUuid();

            String c_title = "";
            String c_skill_area = "";
            String c_scheme_name = "";
            String c_course_objective = "";
            String c_course_outcome = "";
            String c_cr_type = "";
            String type_of_learning = "";
            String traning_skill ="";
            String type_of_programme ="";
            String min_qualification = "";
            String status = "";
            String review_status = "";
            String methodology = "";
            String online_hours = "";
            String hours_achieved = "";
            String f2f_hours = "";
            String e_learning_type = "";
            String target_group = "";

            String practical_centric_indicator = "";

            String query = "SELECT * FROM app_fd_course_register WHERE id=?";

            String sql = "SELECT * FROM archive.course_registration WHERE TRAINING_PROGRAMME_MASTER_ID = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            boolean exists = false;
            
            if (resultSet.next()) {
                query = "SELECT * FROM archive.course_registration WHERE TRAINING_PROGRAMME_MASTER_ID=?";
                exists = true;
            }

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery(); 

            if(rs.next()) {

                if (exists){
                    c_title = rs.getString("COURSE_TITLE");
                    c_skill_area = rs.getString("SKILL_AREA");
                    c_scheme_name = rs.getString("SCHEME_NAME");
                    c_course_objective = rs.getString("OBJECTIVE");
                    //c_course_outcome = rs.getString("COURSE_OUTLINE");
                    c_cr_type = rs.getString("CRAS_PROGRAM_ID");
                    type_of_learning = rs.getString("TRNG_TYPE");
                    traning_skill = rs.getString("TRNG_SKILL");
                    type_of_programme = rs.getString("IS_TECHNICAL");
                    min_qualification = rs.getString("TRAINING_MINIMUM_REQUISITE");
                    methodology = rs.getString("METHODOLOGY");
                    online_hours = rs.getString("ONLINE_HOURS");
                    hours_achieved = rs.getString("HOURS_ACHIEVED");
                    f2f_hours = rs.getString("FACT_TO_FACE_HOURS");
                    e_learning_type = rs.getString("METHODOLOGY");
                    target_group = rs.getString("TARGET_GRP");

                    status = rs.getString("TRAINING_PROGRAMME_STATUS");
                    review_status = rs.getString("APPLICATION_STATUS");

                    practical_centric_indicator = rs.getString("IS_PERLA");

                    row.setProperty("id", id);
                    row.setProperty("cr_type", c_cr_type != null ? "Industry-Specific":"Normal");
                    row.setProperty("course_name", c_title.trim());
                    row.setProperty("dummy_name", c_title.trim());
                    row.setProperty("schema", c_scheme_name.trim());
                    row.setProperty("skill_area", c_skill_area.trim());
                    row.setProperty("course_objectives", c_course_objective.trim());
                    //row.setProperty("course_outcome", c_course_outcome.trim());
                    row.setProperty("type_of_learning", type_of_learning.trim());
                    row.setProperty("traning_skill", traning_skill.trim());
                    row.setProperty("type_of_programme", type_of_programme.trim() == "Y" ? "Technical":"Non Technical");
                    row.setProperty("min_qualification", min_qualification.trim());
                    row.setProperty("action_review_status", review_status.trim() == "Approve"? "Rejected":"Approved");
                    row.setProperty("review_status", status.trim() == "Approved"? "Rejected":"Approved");
                    row.setProperty("status", review_status.trim() == "Approve"? "Rejected":"Approved");
                    row.setProperty("c_hours_achieved", hours_achieved != null ? f2f_hours: "");
                    row.setProperty("c_f2f_hours", f2f_hours != null ? f2f_hours:"");
                    row.setProperty("c_online_hours", online_hours != null ? online_hours:"");
                    row.setProperty("c_e_learning_type", e_learning_type != null ? e_learning_type.trim():"");          
                    
                    row.setProperty("target_group", target_group != null? target_group.trim():"");    
                    row.setProperty("methodology", methodology.trim());     

                    row.setProperty("practical_centric_indicator", practical_centric_indicator.trim() == "N"? "Non-Practical Centric":"Practical Centric");     

                    rows.add(row);

                }else {
                    return super.load(element, id, formData);
                }
            }

            
        }catch(Exception ex){
            LogUtil.error("HRDC - COURSE - Load Archive Data Binder ----->", ex, "Error loading template");
        }
        return rows;
    }
}
