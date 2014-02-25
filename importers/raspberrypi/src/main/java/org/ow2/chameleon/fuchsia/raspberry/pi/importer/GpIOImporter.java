package org.ow2.chameleon.fuchsia.raspberry.pi.importer;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.raspberry.pi.internal.GPIOPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component(name = "GpIOImporter")
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class GpIOImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(GpIOImporter.class);

    private ServiceReference serviceReference;

    private Map<String, InstanceManager> gpioPin = new HashMap<String, InstanceManager>();

    @Requires(filter = "(factory.name=org.ow2.chameleon.fuchsia.raspberry.pi.device.GPIOOutputPinFactory)")
    private Factory lightFactory;

    @ServiceProperty(name = "target", value = "(&(importer.gpio.pin=*)(importer.gpio.name=*)(id=*))")
    private String filter;

    public GpIOImporter() {
        super();
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Validate
    public void validate() {
        super.start();
        LOG.info("GOIO importer is up and running");
    }

    @Invalidate
    public void invalidate() {
        super.stop();
        LOG.info("GOIO importer was shutdown");
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        GPIOPojo pojo = GPIOPojo.create(importDeclaration.getMetadata());

        LOG.info("importing id:" + pojo.getId());

        Dictionary<String, Object> light = new Hashtable<String, Object>();

        light.put("pin", pojo.getPin());

        try {
            importDeclaration.handle(serviceReference);
            InstanceManager im = (InstanceManager) lightFactory.createComponentInstance(light);

            gpioPin.put(pojo.getId(), im);


        } catch (UnacceptableConfiguration e) {
            LOG.error("Invalid configuration", e);
            importDeclaration.unhandle(serviceReference);
        } catch (MissingHandlerException e) {
            LOG.error("Missing handle", e);
            importDeclaration.unhandle(serviceReference);
        } catch (ConfigurationException e) {
            LOG.error("Configuration exception", e);
            importDeclaration.unhandle(serviceReference);
        }
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        GPIOPojo pojo = GPIOPojo.create(importDeclaration.getMetadata());

        InstanceManager im = gpioPin.get(pojo.getId());
        importDeclaration.unhandle(serviceReference);
        if (im != null) {
            im.dispose();
        }
    }


    public String getName() {
        return this.getClass().getSimpleName();
    }

}
