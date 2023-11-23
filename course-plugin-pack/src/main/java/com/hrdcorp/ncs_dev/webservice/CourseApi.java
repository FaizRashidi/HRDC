/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;

/**
 *
 * @author courts
 */
public class CourseApi extends DefaultPlugin implements PluginWebSupport{
    @Override
    public Object execute(Map arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "HRDC Course Api";
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return "1.0";
    }
    
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        
        try(Connection con = ds.getConnection()){
            
            String method = request.getParameter("method") != null ? request.getParameter("method"): "";
            
            LogUtil.info("HRDC - Course Api Plugin  ---->","Connection ok");
            switch(method.toUpperCase()){
                
                case "GETCOURSEDETAILSFORTM":
                    getcoursedetailsfortm(request,response,con);
                    break;
                case "GETCOURSEDETAILSFORGRANT":
                    getcoursedetailsforgrant(request,response,con);
                    break;
                case "GETCLASSDETAILS":
                    getclassdetails(request, response, con);
                    break;
                case "GETPROMOCODE":
                    getpromocode(request, response, con);
                    break;
                case "GETCOURSEAPPROVED":
                    getcourseapproved(request, response, con);
                    break;
                case "GETCLASSAPPROVED":
                    getclassapproved(request, response, con);
                    break;
                case "GETVIEWFORMCOURSE&CLASS":
                    getviewformcourseandclass(request, response, con);
                    break;
                case "GETDETAILSTRAINER":
                    getdetailstrainer(request, response, con);
                    break;
                case "GETTEMPLATE":
                    getEmailTemplate(request, response, con);
                    break;
                case "GETKEYWORDNAME":
                    getKeywordName(request, response, con);
                    break;
                case "GETLINKSNAME":
                    getLinksName(request, response, con);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Parameter method with invalid option " + method);
                    LogUtil.info(this.getClass().getName(), "Parameter method with invalid option");
                    try{
                        con.close();
                    }catch(Exception ex){
                        LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
                    }
                    break;
            }
            
        } catch (Exception ex) {
            LogUtil.error(this.getClass().getName(), ex, "Something went wrong: ");
        }
    }
     
    private void getcoursedetailsfortm(HttpServletRequest request, HttpServletResponse response, Connection con) {

        LocalDate startDate = LocalDate.parse(request.getParameter("startDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(request.getParameter("endDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        String sql = "SELECT * FROM app_fd_course_register WHERE c_date_approved BETWEEN ? AND ? AND c_active_inactive_status = 'Active'";
    
    //    LogUtil.info("HRDC - Course Api Plugin  ---->","DATA KELUAR LAH HAWAU");
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            int Count = 0;
            
            // LogUtil.info("HRDC - Course Api Plugin  ---->","DATA KELUAR LAH HAWAU");
            while (rs.next()) {
                JSONObject data = new JSONObject();
                String id = rs.getString("id");
                String course_id = rs.getString("c_course_id");
                String course_name = rs.getString("c_course_name");
                String course_type = rs.getString("c_cr_type");
                String dateCreated = rs.getString("dateCreated");
                String dateApproved = rs.getString("c_date_approved");
                String activeInactiveStatus = rs.getString("c_active_inactive_status");
                String status = rs.getString("c_status");
    
                data.put("id", id);
                data.put("course_id", course_id);
                data.put("course_name", course_name);
                data.put("course_type", course_type);
                data.put("dateCreated", dateCreated);
                data.put("dateApproved", dateApproved);
                data.put("activeInactiveStatus", activeInactiveStatus);
                data.put("status", status);
                array.put(data);
                // LogUtil.info("HRDC - Course Api Plugin  ---->","DATA KELUAR LAH HAWAU");
                Count = Count +1;
            }
            JSONObject count = new JSONObject();
            count.put("Count", Count);
            array.put(count);
    
            out.print(array);
    
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
    
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }
        }
    }
     
    private void getcoursedetailsforgrant (HttpServletRequest request, HttpServletResponse response ,Connection con ){
        
            
        String sql = "SELECT * FROM app_fd_course_register";
        
        
        try (PrintWriter out = response.getWriter()) {
            //LogUtil.info("HRDC - normal-page1 - Get User Last Login ---->","page1");
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject data = new JSONObject();
             
                String id = rs.getString("id");
                String course_id = rs.getString("c_course_id");
                String course_name = rs.getString("c_course_name");
                String course_type = rs.getString("c_cr_type");
                String dateCreated = rs.getString("dateCreated");
                String dateApproved = rs.getString("c_date_approved");
                String schema = rs.getString("c_schema");
                String skill_area = rs.getString("c_skill_area");
                String traning_skill = rs.getString("c_traning_skill");
                String training_category = rs.getString("c_training_category");
                String focus_area = rs.getString("c_focus_area");
                String training_schedule_normal = rs.getString("c_training_schedule_normal");
                String min_qualification = rs.getString("c_min_qualification");
                String target_group = rs.getString("c_target_group");
                String methodology = rs.getString("c_methodology");
                String pro_cert = rs.getString("c_pro_cert");
                String industry = rs.getString("c_industry");
                String sector = rs.getString("c_sector");
                String subsector = rs.getString("c_subsector");
                String practical_centric_indicator = rs.getString("c_practical_centric_indicator");
                String course_objectives = rs.getString("c_course_objectives");
                String course_outcome = rs.getString("c_course_outcome");
                String type_of_learning = rs.getString("c_type_of_learning");
                String type_of_programme = rs.getString("c_type_of_programme");
                String course_overview = rs.getString("c_course_overview");
                String spCourseOverview = rs.getString("c_spCourseOverview");
                String tht = rs.getString("c_tht");
                String thp = rs.getString("c_thp");
                String attachment = rs.getString("c_attachment");
                String personal_name = rs.getString("c_personal_name");
                String personal_nationality = rs.getString("c_personal_nationality");
                String personal_ic_pass = rs.getString("c_personal_ic_pass");
                String personal_phoneNo = rs.getString("c_personal_phoneNo");
                String director_email = rs.getString("c_director_email");
                String officer_name = rs.getString("c_officer_name");
                String officer_nationality = rs.getString("c_officer_nationality");
                String officer_icpass = rs.getString("c_officer_icpass");
                String designation_op = rs.getString("c_designation_op");
                String officer_phoneNo = rs.getString("c_officer_phoneNo");
                String officer_email = rs.getString("c_officer_email");
                String activeInactiveStatus = rs.getString("c_active_inactive_status");
                String status = rs.getString("c_status");


                data.put("id", id);
                data.put("course_id", course_id);
                data.put("course_type",course_type);
                data.put("dateCreated", dateCreated);
                data.put("dateApproved", dateApproved);
                data.put("course_name", course_name);
                data.put("schema",schema);
                data.put("skill_area",skill_area);
                data.put("traning_skill", traning_skill);
                data.put("training_category",training_category);
                data.put("focus_area", focus_area);
                data.put("training_schedule_normal", training_schedule_normal);
                data.put("min_qualification ", min_qualification);
                data.put("target_group",target_group);
                data.put("methodology",methodology);
                data.put("pro_cert",pro_cert);
                data.put("industry", industry);
                data.put("sector ", sector);
                data.put("subsector", subsector);
                data.put("practical_centric_indicator ", practical_centric_indicator);
                data.put("course_objectives", course_objectives);
                data.put("course_outcome", course_outcome);
                data.put("type_of_learning", type_of_learning);
                data.put("type_of_programme", type_of_programme);
                data.put("course_overview", course_overview);

                // //LogUtil.info("HRDC - normal-page2 - Get User Last Login ---->","page2");
                data.put("spCourseOverview",spCourseOverview);
                data.put("tht", tht);
                data.put("thp", thp);
                data.put("attachment", attachment);


                // //LogUtil.info("HRDC - normal-page3 - Get User Last Login ---->","page3");
                data.put("personal_name ", personal_name);
                data.put("personal_nationality", personal_nationality);
                data.put("personal_ic_pass", personal_ic_pass);
                data.put("personal_phoneNo", personal_phoneNo);
                data.put("director_email", director_email);
                data.put("officer_name", officer_name);
                data.put("officer_nationality", officer_nationality);
                data.put("officer_icpass", officer_icpass);
                data.put("designation_op", designation_op);
                data.put("officer_phoneNo", officer_phoneNo);
                data.put("officer_email",officer_email);
                data.put("activeInactiveStatus",activeInactiveStatus);
                data.put("status",status);
                array.put(data);
            }

            
            try{
                con.close();
            }catch(Exception ex){
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }finally{
               out.print(array); 
            }

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }
    }

    private void getclassdetails(HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
         
        // String sql = "SELECT * FROM app_fd_course_class ";

       
        
        
        // try (PrintWriter out = response.getWriter()) {
        //     //LogUtil.info("HRDC - normal-page1 - Get User Last Login ---->","page1");
        //     PreparedStatement stmt = con.prepareStatement(sql);
        //     ResultSet rs = stmt.executeQuery();
        //     JSONArray array = new JSONArray();
        //     while (rs.next()) {
        //         JSONObject data = new JSONObject();
             
        //         String id = rs.getString("id");
        //         String class_id = rs.getString("c_class_id");
        //         String course_name = rs.getString("c_course_name");
        //         String date = rs.getString("c_date");
        //         String venue= rs.getString("c_venue");
        //         String e_venue = rs.getString("c_e_venue");
        //         String training_type = rs.getString("c_training_type");
        //         String course_fee = rs.getString("c_course_fee");
        //         String pax = rs.getString("c_pax");
        //         String trainer_reg = rs.getString("c_trainer_reg");
        //         String check_upfront = rs.getString("c_check_upfront");
        //         String upfront_fee = rs.getString("c_upfront_fee");
        //         String description = rs.getString("c_description");
        //         String target_group = rs.getString("c_target_group");
        //         String methodology = rs.getString("c_methodology");
        //         String pro_cert = rs.getString("c_pro_cert");
        //         String industry = rs.getString("c_industry");
        //         String sector = rs.getString("c_sector");
        //         String subsector = rs.getString("c_subsector");
        //         String practical_centric_indicator = rs.getString("c_practical_centric_indicator");
        //         String course_objectives = rs.getString("c_course_objectives");
        //         String course_outcome = rs.getString("c_course_outcome");
        //         String type_of_learning = rs.getString("c_type_of_learning");
        //         String type_of_programme = rs.getString("c_type_of_programme");
        //         String course_overview = rs.getString("c_course_overview");
        //         String spCourseOverview = rs.getString("c_spCourseOverview");
        //         String tht = rs.getString("c_tht");
        //         String thp = rs.getString("c_thp");
        //         String attachment = rs.getString("c_attachment");
        //         String personal_name = rs.getString("c_personal_name");
        //         String personal_nationality = rs.getString("c_personal_nationality");
        //         String personal_ic_pass = rs.getString("c_personal_ic_pass");
        //         String personal_phoneNo = rs.getString("c_personal_phoneNo");
        //         String director_email = rs.getString("c_director_email");
        //         String officer_name = rs.getString("c_officer_name");
        //         String officer_nationality = rs.getString("c_officer_nationality");
        //         String officer_icpass = rs.getString("c_officer_icpass");
        //         String designation_op = rs.getString("c_designation_op");
        //         String officer_phoneNo = rs.getString("c_officer_phoneNo");
        //         String officer_email = rs.getString("c_officer_email");
        //         String activeInactiveStatus = rs.getString("c_active_inactive_status");
        //         String status = rs.getString("c_status");


                
        //     data.put("id",id);
        //     data.put("class_id",class_id);
        //     data.put("course_name",course_name);
        //     data.put("date",date);
        //     data.put("venue",venue);
        //     data.put("training_type",training_type);
        //     data.put("course_fee",course_fee);
        //     data.put("e_venue", e_venue);
        //     data.put("pax", pax);
        //     data.put("trainer_reg",trainer_reg);
        //     data.put("check_upfront",check_upfront);
        //     data.put("upfront_fee", upfront_fee);
        //     data.put("description", description);

             //LogUtil.info("HRDC - section-promocode - Get promocode ---->","promocode");
        //     data.put("promoCode", );
        //     data.put("type", type);
        //     data.put("percentage", percentage);
        //     data.put("amount_rm", amount_rm);

        //      //LogUtil.info("HRDC - section-training location - Get training location ---->","training location");
        //     data.put("state",state);
        //     data.put("city", city);
        //     data.put("posscode", posscode);

        //      //LogUtil.info("HRDC - section-training category - Get training category ---->","training category");
        //     data.put("trainer_name",trainer_name);
        //     data.put("trainer_ic", trainer_ic);
        //     data.put("companyName", companyName);
        //     data.put("origin", origin);
        //     data.put("trainer_profile", trainer_profile);

        //       //LogUtil.info("HRDC - section-training details - Get training details ---->","training details");
        //     data.put("exam_ass_fee", exam_ass_fee);
        //     data.put("exam_ass_file", exam_ass_file);
        //     //LogUtil.info("HRDC - section-remote online training hours2 - Get remote online training hours2 ---->","remote online training hours2");
        //     data.put("hybrid_remote_hours", hybrid_remote_hours);
        //     data.put("hybrid_f2f_hours", hybrid_f2f_hours);
        //      //LogUtil.info("HRDC - section-ltm - Get ltm ---->","ltm");
        //     data.put("amount_approved",amount_approved);
        //     data.put("ltm_id", ltm_id);
        //     data.put("description", description);
        //     data.put("file", file );

        //       //LogUtil.info("HRDC - section-training duration - Get training duration ---->","training duration");
        //     data.put("programme_commencement_start_date", programme_commencement_start_date);
        //     data.put("programme_commencement_end_date", programme_commencement_end_date);
        //     data.put("programme_total_hours_per_training", programme_total_hours_per_training);
        //     data.put("programme_half_days_no_hours", programme_half_days_no_hours);
        //     data.put("programme_full_days_no ", programme_full_days_no);
        //     data.put("programme_half_days_no", programme_half_days_no);
        //     data.put("programme_less_than_half_days_no", programme_less_than_half_days_no);
        //     data.put("programme_total_hours_per_trainee", programme_total_hours_per_trainee);
        //     data.put("from_class", from_class);
        //     data.put("to_class", to_class);
        //     data.put("status_publish", status_publish);

        //     }
            
        //     try{
        //         con.close();
        //     }catch(Exception ex){
        //         LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
        //     }finally{
        //        out.print(array); 
        //     }

        // } catch (Exception e) {
        //     LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        // }

    }
    
    private void getpromocode (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{

        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement("SELECT G.id, G.c_promoCode, G.c_type, G.c_amount_rm, G.c_percentage " +
                                                         "FROM app_fd_promo_code_sub G " +
                                                         "LEFT JOIN app_fd_course_class C ON G.id = C.id");
            ResultSet rs = stmt.executeQuery();
        
            JSONArray jsonArray = new JSONArray();
        
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", rs.getString("id"));
                jsonObject.put("c_promoCode", rs.getString("c_promoCode"));
                jsonObject.put("c_type", rs.getString("c_type"));
                jsonObject.put("c_amount_rm", rs.getString("c_amount_rm"));
                jsonObject.put("c_percentage", rs.getString("c_percentage"));
        
                jsonArray.put(jsonObject);
            }
        
            out.print(jsonArray.toString());
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }
        }
    }

    private void getcourseapproved(HttpServletRequest request, HttpServletResponse response, Connection con) throws SQLException {
        String urlParam = "TP01";
        String sql = "SELECT * FROM app_fd_course_register where c_active_inactive_status ='active' AND createdBy = ?";
    
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, urlParam);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String id = rs.getString("id");
                String course_name = rs.getString("c_course_name");
                String active_inactive_status = rs.getString("c_active_inactive_status");
                String createdBy = rs.getString("createdBy");
                obj.put("id", id);
                obj.put("course_name", course_name);
                obj.put("active_inactive_status", active_inactive_status);
                obj.put("createdBy", createdBy);
                array.put(obj);
            }
    
            out.print(array);
    
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
    
        } finally {
            con.close();
        }
    }

    private void  getclassapproved (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        try (PrintWriter out = response.getWriter()) {
            String sql = "SELECT * FROM app_fd_course_class where c_status ='Published'";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        
            JSONArray jsonArray = new JSONArray();
        
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", rs.getString("id"));
                jsonObject.put("course_name", rs.getString("c_course_name"));
            
                // ... add other columns
        
                jsonArray.put(jsonObject);
            }
        
            out.print(jsonArray.toString());
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }
        }
    }

    private void getviewformcourseandclass(HttpServletRequest request, HttpServletResponse response, Connection con) throws SQLException {
        // String sql = "SELECT c_keyword, c_link FROM app_fd_stp_links";
        try (PrintWriter out = response.getWriter()) {
            // Using try-with-resources to ensure the statement is closed
            
            String baseUrl = WorkflowUtil.getHttpServletRequest().getScheme() + "://" +
                             WorkflowUtil.getHttpServletRequest().getServerName() +
                             "/jw/web/userview/course_registration_module/course/_/";
            
            JSONObject obj = new JSONObject();
            
            String formid = "123"; 
            String link = baseUrl + formid;
            LogUtil.info("HRDC - Link  ---->", "Link: " + link);
            obj.put("url", link);
            
            response.setContentType("application/json");
            
            out.print(obj.toString());
            
        } catch (Exception e) {
            LogUtil.error("HRDC - Error", e, "Something went wrong:");
        } // This was missing in the provided cod
    }
    
    

  

    private void getdetailstrainer (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT " +
        "    a.c_trainer_name, " +
        "    a.c_trainer_ic, " +
        "    a.c_companyName, " +
        "    a.c_origin, " +
        "    a.c_trainer_profile, " +
        "    b.c_course_id, " +
        "    c.c_grant_id " +
        "FROM " +
        "    app_fd_course_register a " +
        "LEFT JOIN " +
        "    app_fd_course_trainer b ON a.id = b.id " +
        "LEFT JOIN " +
        "    app_fd_grant_table c ON b.c_course_id = c.c_course_id";

        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String id = rs.getString("id");
                String trainer_name = rs.getString("c_trainer_name");
                String trainer_ic = rs.getString("c_trainer_ic");
                String companyName = rs.getString("c_companyName");
                String origin = rs.getString("c_origin");
                  String trainer_profile = rs.getString("c_trainer_profile");

                obj.put("id", id);
                obj.put("trainer_name", trainer_name);
                obj.put("trainer_ic", trainer_ic);
                obj.put("companyName", companyName);
                obj.put("origin", origin);
                obj.put("trainer_profile", trainer_profile);
                array.put(obj);
            }

            out.print(array);
            

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }


    private void getEmailTemplate (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT et.id, CONCAT(et.id, ' - ', et.c_moduleType, ' - ', et.c_emailType) as template_name FROM app_fd_stp_email_template et";
        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String id = rs.getString("id");
                String template_name = rs.getString("template_name");
                obj.put("value", id);
                obj.put("label", template_name);
                array.put(obj);
            }

            out.print(array);
            

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }

    private void getKeywordName (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{

        String group = request.getParameter("group") != null ? request.getParameter("group"): "";
        
        String sql = "SELECT c_columnID, c_columnName FROM app_fd_stp_keyword WHERE c_group_as = ? ORDER BY c_columnID ASC";
        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1,group);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String column_id = rs.getString("c_columnID");
                String column_name = rs.getString("c_columnName");
                obj.put("pholder", column_id);
                obj.put("descr", column_name);
                array.put(obj);
            }

            out.print(array);
            

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }

    private void getLinksName (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT c_keyword, c_link FROM app_fd_stp_links";
        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String keyword = rs.getString("c_keyword");
                String link = rs.getString("c_link");
                obj.put("pholder", keyword);
                obj.put("descr", link);
                array.put(obj);
            }

            out.print(array);
            

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }

}


