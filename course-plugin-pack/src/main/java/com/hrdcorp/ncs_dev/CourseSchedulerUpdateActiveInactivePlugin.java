/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hrdcorp.ncs_dev;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.commons.util.LogUtil;



/**
 *
 * @author farih
 */
public class CourseSchedulerUpdateActiveInactivePlugin extends DefaultApplicationPlugin {
    @Override
    public String getName() {
        return ("HRDC - COURSE - Scheduler Update Active/Inactive Status");
    }

    @Override
    public String getVersion() {
        return ("1.0.0");
    }

    @Override
    public String getDescription() {
        return ("To update active/inactive status based on scheduler date");
    }

    @Override
    public String getLabel() {
        return ("HRDC - COURSE - Scheduler Update Active/Inactive Status");
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/cr_audit_trail.json", null, true);
    }
    
    
    @Override
    public Object execute(Map props) {
        LogUtil.info("HRDC COURSE Scheduler Update Active/Inactive Status ---->","Start scheduler");
        
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
                     
        try {
            // Establish a database connection
            Connection con = ds.getConnection();
            try {
                LogUtil.info("HRDC COURSE Scheduler Update Active/Inactive Status ---->","Try Updating");
                updateInactiveDataAfterYear(con);
                LogUtil.info("HRDC COURSE Scheduler Update Active/Inactive Status ---->","Successfully Updating");
            }catch(SQLException e){
                LogUtil.info("HRDC COURSE Scheduler Update Active/Inactive Status ---->","Fail Updating");
               Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
         
    public void updateInactiveDataAfterYear(Connection con) throws SQLException {
      Statement stmt = null;
        
      stmt = con.createStatement();
            
      stmt.execute("UPDATE app_fd_course_register AS cr\n" +
                    "SET cr.c_active_inactive_status = 'Inactive'\n" +
                    "WHERE\n" +
                    "	cr.c_status = 'Approved'\n" +
                    "   AND\n" +
                    "	cr.c_active_inactive_status = 'Active'\n" +
                    "   AND\n" +
                    "	cr.c_date_approved IS NOT NULL  \n" +
                    "	AND\n" +
                    "	cr.c_date_approved < DATE_SUB(NOW(), INTERVAL 1 YEAR)\n" +
                    "    AND NOT EXISTS (\n" +
                    "        SELECT 1\n" +
                    "        FROM app_fd_stp_dummy_grant AS dg\n" +
                    "        WHERE\n" +
                    "            FIND_IN_SET(cr.id, REPLACE(dg.c_course_title, ';', ','))\n" +
                    "	) \n" +
                    "    AND cr.id IS NOT NULL LIMIT 1000;"
                    );
      stmt.close();
      con.close();
    }
}
