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
        //registrationList.add(context.registerService(MyPlugin.class.getName(), new MyPlugin(), null));
        registrationList.add(context.registerService(EvntAuditTrailBinder.class.getName(), new EvntAuditTrailBinder(), null));
        registrationList.add(context.registerService(EvntAuditAndEmailPotentialClientBinder.class.getName(), new EvntAuditAndEmailPotentialClientBinder(), null));
        registrationList.add(context.registerService(EvntAutoRejectAuditWorkflowPlugin.class.getName(), new EvntAutoRejectAuditWorkflowPlugin(), null));
        registrationList.add(context.registerService(EvntGenerateQrCodeWorkflowPlugin.class.getName(), new EvntGenerateQrCodeWorkflowPlugin(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}