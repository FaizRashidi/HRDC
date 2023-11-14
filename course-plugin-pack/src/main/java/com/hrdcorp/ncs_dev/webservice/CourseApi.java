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
    
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            int Count = 0;
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
                data.put("id", rs.getString("id"));
                data.put("course_type", rs.getString("c_cr_type"));
                data.put("dateCreated", rs.getString("dateCreated"));
                data.put("id",rs.getString("id"));
                data.put("course_name",rs.getString("c_course_name"));
                data.put("schema",rs.getString("c_schema"));
                data.put("skill_area",rs.getString("c_skill_area"));
                data.put("traning_skill", rs.getString("c_traning_skill"));
                data.put("training_category", rs.getString("c_training_category"));
                data.put("focus_area", rs.getString("c_focus_area"));
                data.put("training_schedule_normal", rs.getString("c_training_schedule_normal"));
                data.put("min_qualification ", rs.getString("c_min_qualification"));
                data.put("target_group",rs.getString("c_target_group"));
                data.put("methodology", rs.getString("c_methodology"));
                data.put("pro_cert", rs.getString("c_pro_cert"));
                data.put("industry",rs.getString("c_industry"));
                data.put("sector ", rs.getString("c_sector"));
                data.put("subsector", rs.getString("c_subsector"));
                data.put("practical_centric_indicator ",rs.getString("c_practical_centric_indicator"));
                data.put("course_objectives", rs.getString("c_course_objectives"));
                data.put("course_outcome",rs.getString("c_course_outcome"));
                data.put("type_of_learning", rs.getString("c_type_of_learning"));
                data.put("type_of_programme", rs.getString("c_type_of_programme"));
                data.put("course_overview", rs.getString("c_course_overview"));

                //LogUtil.info("HRDC - normal-page2 - Get User Last Login ---->","page2");
                data.put("spCourseOverview",rs.getString("c_spCourseOverview"));
                data.put("tht",rs.getString("c_tht"));
                data.put("thp",rs.getString("c_thp"));
                // data.put("thtp",rs.getString("c_thtp"));
                data.put("attachment",rs.getString("c_attachment"));

                //LogUtil.info("HRDC - normal-page3 - Get User Last Login ---->","page3");
                data.put("personal_name ",rs.getString("c_personal_name"));
                data.put("personal_nationality",rs.getString("c_personal_nationality"));
                data.put("personal_ic_pass",rs.getString("c_personal_ic_pass"));
                data.put("personal_phoneNo",rs.getString("c_personal_phoneNo"));
                data.put("director_email",rs.getString("c_director_email"));
                data.put("officer_name",rs.getString("c_officer_name"));
                data.put("officer_nationality",rs.getString("c_officer_nationality"));
                data.put("officer_icpass",rs.getString("c_officer_icpass"));
                data.put("designation_op",rs.getString("c_designation_op"));
                data.put("officer_phoneNo",rs.getString("c_officer_phoneNo"));
                data.put("officer_email",rs.getString("c_officer_email"));
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

        // PreparedStatement stmt = con.prepareStatement(sql);
        // ResultSet rs = stmt.executeQuery();
        // JSONObject data = new JSONObject();
        // if (rs.next()) {
            
        //     data.put("id", rs.getString(""));
        //     data.put("", rs.getString("c_"));
        //     data.put("", rs.getString(""));
        //     data.put("",rs.getString(""));
        //     data.put("",rs.getString(""));
        //     data.put("",rs.getString (""));
        //     data.put("",rs.getString (""));
        //     data.put("", rs.getString (""));
        //     data.put("", rs.getString(""));
        //     data.put("", rs.getString(""));
        //     data.put("", rs.getString(""));
        //     data.put(" ", rs.getString(""));
        //     data.put("",rs.getString (""));
        //     data.put("", rs.getString(""));
        //     data.put("", rs.getString(""));
        //     data.put("",rs.getString(""));
        //     data.put("", rs.getString(""));
        //     data.put("", rs.getString(""));
        //     data.put(" ",rs.getString (""));
        //     data.put("", rs.getString(""));
        //     data.put("",rs.getString (""));
        //     data.put("", rs.getString(""));
        //     data.put("", rs.getString(""));
        //     data.put("", rs.getString(""));
        // }

        // try{
        //     con.close();
        // }catch(Exception ex){
        //     LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
        // }finally{
        //     // out.print(data); 
        // }

    }
    
    private void getpromocode (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{


        String sql = "SELECT G.id, G.c_promoCode, G.c_type, G.c_amount_rm, G.c_percentage " +
                         "FROM app_fd_promo_code_sub G " +
                         "LEFT JOIN app_fd_course_class C ON G.id = C.id";
        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
    
            while (rs.next()) {
                JSONObject data = new JSONObject();
                String id = rs.getString("id");
                String promoCode = rs.getString("c_promoCode");
                String amount_rm = rs.getString("c_amount_rm");
                String type = rs.getString("c_type");
                String percentage = rs.getString("c_percentage");
    
                data.put("id", id);
                data.put("promoCode", promoCode);
                data.put("amount_rm", amount_rm);
                data.put("type", type);
                data.put("percentage", percentage);
            }

            out.print(array);
        

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }

    private void getcourseapproved (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT * FROM app_fd_course_register where c_status ='Approved'";
        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String id = rs.getString("id");
                String course_name = rs.getString("c_course_name");
                obj.put("id", id);
                obj.put("course_name", course_name);
                array.put(obj);
            }

            out.print(array);
            

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }

    private void  getclassapproved (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT * FROM app_fd_course_class where c_status ='Approved'";
        
        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String id = rs.getString("id");
                String course_name = rs.getString("c_course_name");
                obj.put("id", id);
                obj.put("course_name", course_name);
                array.put(obj);
            }

            out.print(array);
            

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
        }finally{
            con.close();
        }
    }

    private void getviewformcourseandclass(HttpServletRequest request, HttpServletResponse response, Connection con) throws SQLException {
        String urlCourseView = "https://ncs-dev.hrdcorp.gov.my/jw/web/userview/course_registration_module/course/_/list_approved_courses_officer?_mode=edit&id=948fc416-8357-4dff-bec3-598a74267c46";
        String urlClassView = "https://ncs-dev.hrdcorp.gov.my/jw/web/userview/course_registration_module/course/_/list_published_classes_officer?_mode=edit&id=4e650eae-430f-4ccd-b155-dc513d7759d2";
    
        try {
            // First URL
            processUrl(urlCourseView);
    
            // Second URL
            processUrl(urlClassView);
    
        } catch (IOException e) {
            e.printStackTrace(); // Handle this exception more appropriately in a production environment
        }
    }
    
    private static void processUrl(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    
            // Set request method (GET by default)
            connection.setRequestMethod("GET");
    
            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code for " + urlString + ": " + responseCode);
    
            // Read the response content
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder content = new StringBuilder();
    
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
    
                reader.close();
    
                // Print the content
                System.out.println("Response Content for " + urlString + ":\n" + content.toString());
            } else {
                System.out.println("HTTP request failed for " + urlString + ": " + responseCode);
            }
    
            // Close the connection
            connection.disconnect();
        } catch (IOException e) {
            // Handle this exception more appropriately in a production environment
            e.printStackTrace();
        }
    }

    private void getdetailstrainer (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT c_trainer_name,c_trainer_ic,c_companyName,c_origin,c_trainer_profile FROM app_fd_course_class aLEFT JOIN app_fd_course_trainer b ON a.id = b.id;";
        
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


