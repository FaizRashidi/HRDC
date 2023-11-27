/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            
                case "GETVIEWFORMCOURSE":
                    getviewformcourse(request, response, con);
                    break;
                case "GETVIEWFORMCLASS":
                    getviewformclass(request, response, con);
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
     


    private void getviewformcourse(HttpServletRequest request, HttpServletResponse response, Connection con) throws SQLException {
       
        String c_course_id = request.getParameter("c_course_id") != null ? request.getParameter("c_course_id"): "";
        String sql = "SELECT id FROM app_fd_course_register WHERE c_course_id = ?";

        try (PrintWriter out = response.getWriter()) {
            
            String id = "";
            String baseUrl = WorkflowUtil.getHttpServletRequest().getScheme() + "://" +
                             WorkflowUtil.getHttpServletRequest().getServerName() +
                             "/jw/web/userview/course_registration_module/course/_/";

            PreparedStatement stmt =con.prepareStatement(sql);
            stmt.setString(1, c_course_id);
            ResultSet rs = stmt.executeQuery();
            JSONObject obj = new JSONObject();
            

            if (rs.next()) {
                
                String c_id = rs.getString("id");
                String formid = "course_view"; 
                String link = baseUrl + formid + "?id="+ c_id ;
                // LogUtil.info("HRDC - Link  ---->", "Link: " + link);
                obj.put("url", link);
                obj.put("c_course_id", c_course_id);
            }
            
    
            // Setting response content type
            response.setContentType("application/json");
    
            // Sending the JSON response
            out.print(obj.toString());
    
        } catch (Exception e) {
            // Handling exceptions and logging errors
            LogUtil.error("HRDC - Error", e, "Something went wrong:");
        }
    }
    
      private void getviewformclass(HttpServletRequest request, HttpServletResponse response, Connection con) throws SQLException {
       
        String c_class_id = request.getParameter("c_class_id") != null ? request.getParameter("c_class_id"): "";
        String sql = "SELECT id FROM app_fd_course_class WHERE c_class_id = ?";

        try (PrintWriter out = response.getWriter()) {
            
            String id = "";
            String baseUrl = WorkflowUtil.getHttpServletRequest().getScheme() + "://" +
                             WorkflowUtil.getHttpServletRequest().getServerName() +
                             "/jw/web/userview/course_registration_module/course/_/";

            PreparedStatement stmt =con.prepareStatement(sql);
            stmt.setString(1, c_class_id);
            ResultSet rs = stmt.executeQuery();
            JSONObject obj = new JSONObject();
            

            if (rs.next()) {
                
                String c_id = rs.getString("id");
                String formid = "class_view"; 
                String link = baseUrl + formid + "?id="+ c_id ;
                // LogUtil.info("HRDC - Link  ---->", "Link: " + link);
                obj.put("url", link);
                obj.put("c_class_id", c_class_id);
            }
            
    
            // Setting response content type
            response.setContentType("application/json");
    
            // Sending the JSON response
            out.print(obj.toString());
    
        } catch (Exception e) {
            // Handling exceptions and logging errors
            LogUtil.error("HRDC - Error", e, "Something went wrong:");
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


