package com.hrdcorp.ncs_dev;

import com.hrdcorp.ncs_dev.binder.EventPreviewEmailTemplateBinder;
import com.hrdcorp.ncs_dev.binder.EventSaveEmailTemplateBinder;
import com.hrdcorp.ncs_dev.binder.EvntAuditAndEmailPotentialClientBinder;
import com.hrdcorp.ncs_dev.binder.EvntAuditTrailBinder;
import com.hrdcorp.ncs_dev.default_plugin.EvntAutoRejectAuditWorkflowPlugin;
import com.hrdcorp.ncs_dev.default_plugin.EvntBlastEmailMapper;
import com.hrdcorp.ncs_dev.default_plugin.EvntGenerateQrCodeWorkflowPlugin;
import com.hrdcorp.ncs_dev.webservice.EventApi;
import com.hrdcorp.ncs_dev.default_plugin.EvntEmailTemplateMapper;
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
        registrationList.add(context.registerService(EventSaveEmailTemplateBinder.class.getName(), new EventSaveEmailTemplateBinder(), null));
        registrationList.add(context.registerService(EvntEmailTemplateMapper.class.getName(), new EvntEmailTemplateMapper(), null));
        registrationList.add(context.registerService(EventApi.class.getName(), new EventApi(), null));  
        registrationList.add(context.registerService(EvntBlastEmailMapper.class.getName(), new EvntBlastEmailMapper(), null));
        registrationList.add(context.registerService(EventPreviewEmailTemplateBinder.class.getName(), new EventPreviewEmailTemplateBinder(), null));



    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}