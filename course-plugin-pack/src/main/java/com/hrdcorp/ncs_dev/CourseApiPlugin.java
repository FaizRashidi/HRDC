/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

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
            
            LogUtil.info("HRDC - Course Api Plugin  ---->","connetion ok");
            switch(method.toUpperCase()){
                
                case "GETCOURSEDETAILS":
                    getcoursedetails(request,response,con);
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
     
    private void  getcoursedetails (HttpServletRequest request, HttpServletResponse response ,Connection con ){
        
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
}


