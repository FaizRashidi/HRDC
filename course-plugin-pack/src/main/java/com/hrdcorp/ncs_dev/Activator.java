package com.hrdcorp.ncs_dev;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(CourseAuditTrailBinder.class.getName(), new CourseAuditTrailBinder(), null));
        registrationList.add(context.registerService(CourseAuditTrailWorkflowPlugin.class.getName(), new CourseAuditTrailWorkflowPlugin(), null));
        registrationList.add(context.registerService(CourseSchedulerUpdateActiveInactivePlugin.class.getName(), new CourseSchedulerUpdateActiveInactivePlugin(), null));
        registrationList.add(context.registerService(CourseSchedulerNotifyCourseExpirePlugini.class.getName(), new CourseSchedulerNotifyCourseExpirePlugini(), null));
        registrationList.add(context.registerService(CourseApiPlugin.class.getName(), new CourseApiPlugin(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}