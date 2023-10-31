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
 * @author farih
 */
public class HrdcThemeWebService  extends DefaultPlugin implements PluginWebSupport{
    
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
        return "HRDC Theme Webservice";
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
    
    String BASE_URL = "";
    
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
        BASE_URL = request.getScheme()+"://"+request.getServerName();

        DataSource dataSource = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        
        try (Connection con = dataSource.getConnection()) {
            
            String method = request.getParameter("method") != null ? request.getParameter("method") : "";

            if (method.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Parameter method is missing or empty");
                LogUtil.info(this.getClass().getName(), "Parameter method is missing or empty");
                return;
            }

            switch (method.toUpperCase()) {
                case "GETLASTLOGIN":
                    lastLogin(request, response, con);
                    break;

                case "GETAPPICON":
                    appIcon(request, response, con);
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
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong, Please try again or contact the administrator");
            } catch (IOException ex1) {
                LogUtil.error(this.getClass().getName(), ex1, "Something went wrong: ");
            }
            LogUtil.error(this.getClass().getName(), ex, "Something went wrong: ");
        }
    }
     
    private void lastLogin(HttpServletRequest request, HttpServletResponse response, Connection con) {

        String username = request.getParameter("username") != null ? request.getParameter("username") : "";

        String sql = "SELECT id, message, timestamp " +
                    "FROM wf_audit_trail " +
                    "WHERE message LIKE 'Authentication for user "+username+"%: true' " +
                    "ORDER BY timestamp DESC " +
                    "LIMIT 1 OFFSET 1;";

        try (PrintWriter out = response.getWriter()) {
            //LogUtil.info("HRDC - UI/UX - Get User Last Login ---->","Getting timestamp");
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONObject data = new JSONObject();
            if (rs.next()) {
                data.put("id", rs.getString("id"));
                data.put("timestamp", rs.getString("timestamp"));
                data.put("message", rs.getString("message"));
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
            
        }finally{
            try{
                con.close();
            }catch(Exception ex){
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }
        }
    }
    
    private void appIcon(HttpServletRequest request, HttpServletResponse response, Connection con) {

        String sql = "SELECT login.id, login.c_parentId, login.c_image, theme.c_duration FROM app_fd_stp_loginCarousel login LEFT JOIN app_fd_stp_theme theme ON login.c_parentId = theme.id";

        try (PrintWriter out = response.getWriter()) {
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                String id = rs.getString("id");
                String image = rs.getString("c_image").replaceAll(" ", "%20");
                String duration = rs.getString("c_duration");
                obj.put("id", id);
                obj.put("parent_id", rs.getString("c_parentId"));
                obj.put("image", image);
                obj.put("url", BASE_URL + "/jw/web/client/app/appcenter/1/form/download/login_carousel/"+id+"/"+image+".?__a_=appcenter&__u_=home");
                obj.put("duration", duration);
                array.put(obj);
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
        }finally{
            try{
                con.close();
            }catch(Exception ex){
                LogUtil.error(this.getClass().getName(), ex, "Cannot close connection");
            }
        }
    }     
}

