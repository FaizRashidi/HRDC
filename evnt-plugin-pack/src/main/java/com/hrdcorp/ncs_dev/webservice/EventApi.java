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
import java.sql.Statement;
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
public class EventApi extends DefaultPlugin implements PluginWebSupport{
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
            
            // LogUtil.info("HRDC - Course Api Plugin  ---->","Connection ok");
            switch(method.toUpperCase()){
            
              
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
     

    private void getEmailTemplate (HttpServletRequest request, HttpServletResponse response ,Connection con ) throws SQLException{
        
        String sql = "SELECT et.id, CONCAT(et.id, ' - ', et.c_moduleType, ' - ', et.c_emailType) as template_name FROM app_fd_stp_evt_email_tmpl et";
        
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
        
        String sql = "SELECT c_columnID, c_columnName FROM app_fd_stp_evt_keyword WHERE c_group_as = ? ORDER BY c_columnID ASC";
        
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
        
        String sql = "SELECT c_keyword, c_link FROM app_fd_stp_evt_links";
        
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


