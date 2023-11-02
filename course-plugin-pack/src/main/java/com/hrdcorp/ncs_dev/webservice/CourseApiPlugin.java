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

/**
 *
 * @author courts
 */
public class CourseApiPlugin extends DefaultPlugin implements PluginWebSupport{
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
                
                case "GETCOURSEDETAILS":
                    getcoursedetails(request,response,con);
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
     
    private void getcoursedetails (HttpServletRequest request, HttpServletResponse response ,Connection con ){
        
        String sql = "Select * from app_fd_course_register" ;
        
        try (PrintWriter out = response.getWriter()) {
            //LogUtil.info("HRDC - UI/UX - Get User Last Login ---->","Getting timestamp");
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONObject data = new JSONObject();
            if (rs.next()) {
                data.put("id", rs.getString("id"));
                data.put("course_type", rs.getString("c_cr_type"));
                data.put("dateCreated", rs.getString("dateCreated"));
                //LogUtil.info("HRDC - UI/UX - Get User Last Login ---->","Timestamep: "+ rs.getString("timestamp"));
            }

            
            try{
                con.close();
            }catch(Exception ex){
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }finally{
               out.print(data); 
            }

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Something went wrong:");
            
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


