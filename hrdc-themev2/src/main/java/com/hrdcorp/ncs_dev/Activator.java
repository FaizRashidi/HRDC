package com.hrdcorp.ncs_dev;

import com.hrdcorp.ncs_dev.theme.HrdcThemeDX8;
import com.hrdcorp.ncs_dev.webservice.HrdcThemeWebService;
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
        registrationList.add(context.registerService(HrdcThemeDX8.class.getName(), new HrdcThemeDX8(), null));
        registrationList.add(context.registerService(HrdcThemeWebService.class.getName(), new HrdcThemeWebService(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}