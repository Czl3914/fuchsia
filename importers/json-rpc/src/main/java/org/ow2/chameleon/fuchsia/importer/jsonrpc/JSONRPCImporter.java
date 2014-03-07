package org.ow2.chameleon.fuchsia.importer.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static java.lang.String.valueOf;
import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;

/**
 * Provides an {@link ImporterService} allowing to access a
 * remote endpoint through jsonrpc thanks to the jsonrpc4j implementation.
 * <p/>
 * A valid {@link ImportDeclaration} for this ImporterService contains has metadata :
 * - ID : a unique String which is the id of the JSON-RPC service
 * - URL : a String containing the URL of the JSON-RPC service to import into OSGi.
 * - SERVICE_CLASS : a String containing the name of the class to use to build the proxy
 * <p/>
 * TODO : Improves the client management, only one client should be created for a given uri.
 */
@Component
@Provides(specifications = {ImporterService.class})
public class JSONRPCImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(JSONRPCImporter.class);

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = TARGET_FILTER_PROPERTY, value = "(&(" + PROTOCOL_NAME + "=jsonrpc)(scope=generic))")
    private String filter;

    @Requires(filter = "(factory.name=org.ow2.chameleon.fuchsia.importer.jsonrpc.DefaultJSONRPCProxy)", optional = false)
    Factory defaultProxyFactory;

    private final BundleContext context;

    private ServiceReference serviceReference;

    /**
     * Map which contains the clients and theirs Client.
     */
    private final Map<String, JsonRpcHttpClient> clients = new HashMap<String, JsonRpcHttpClient>();
    private final Map<String, ServiceRegistration> registrations = new HashMap<String, ServiceRegistration>();
    private final Map<String, ComponentInstance> componentInstances = new HashMap<String, ComponentInstance>();


    public JSONRPCImporter(BundleContext pContext) {
        context = pContext;
    }

    @Override
    public void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        final JsonRpcHttpClient client;
        final String url, id, klassName;

        // Get the URL
        url = valueOf(importDeclaration.getMetadata().get(URL));
        // Get an id
        id = (String) importDeclaration.getMetadata().get(ID);

        try {
            client = new JsonRpcHttpClient(new java.net.URL(url));
        } catch (MalformedURLException e) {
            LOG.error("Error during connection to " + url, e);
            throw new BinderException("Error during connection to " + url, e);
        }
        clients.put(id, client);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("name", "JsonRPCProxy-" + id);
        props.put("metadata", importDeclaration.getMetadata());

        //Try to load the class
        klassName = (String) importDeclaration.getMetadata().get(SERVICE_CLASS);
        if (klassName != null) {
            createProxyFromKlass(klassName, client, props, importDeclaration);
        } else {
            createDefaultProxy(client, props, importDeclaration);
        }
        LOG.debug("JsonRPC Proxy successfully created and registered in OSGi " + url + "");
    }

    private void createProxyFromKlass(String klassName, JsonRpcHttpClient client, Dictionary<String, Object> props, ImportDeclaration importDeclaration) throws BinderException {
        final Object proxy;
        final Class<?> klass;
        // Use given klass
        try {
            klass = FuchsiaUtils.loadClass(context, klassName);
        } catch (ClassNotFoundException e) {
            throw new BinderException(
                    "Cannot create a proxy for the ImportDeclaration : " + importDeclaration
                            + " unable to find a bundle which export the service class.", e);
        }

        // create the proxy !
        proxy = ProxyUtil.createClientProxy(klass.getClassLoader(), klass, client);
        ServiceRegistration sReg = context.registerService(klassName, proxy, props);

        importDeclaration.handle(serviceReference);

        String id = (String) importDeclaration.getMetadata().get(ID);
        // Add the registration to the registration list
        registrations.put(id, sReg);
    }

    private void createDefaultProxy(JsonRpcHttpClient client, Dictionary<String, Object> props, ImportDeclaration importDeclaration) throws BinderException {
        // Use a default/generic proxy
        final ComponentInstance componentInstance;
        props.put("client", client);
        try {
            componentInstance = defaultProxyFactory.createComponentInstance(props);
        } catch (Exception e) {
            LOG.error("Error during creation of defaultProxy", e);
            throw new BinderException("Error during creation of defaultProxy", e);
        }
        importDeclaration.handle(serviceReference);

        String id = (String) importDeclaration.getMetadata().get(ID);
        componentInstances.put(id, componentInstance);
    }


    @Override
    public void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        String id = (String) importDeclaration.getMetadata().get(ID);
        if (clients.containsKey(id)) {

            String klassName = (String) importDeclaration.getMetadata().get(SERVICE_CLASS);
            if (klassName != null) {
                // Unregister the proxy from OSGi
                ServiceRegistration sReg = registrations.remove(id);
                if (sReg != null) {
                    sReg.unregister();
                    importDeclaration.unhandle(serviceReference);
                } else {
                    throw new IllegalStateException("The serviceRegistration of the given object is null." +
                            "It could not be unregister.");
                }
            } else {
                ComponentInstance componentInstance = componentInstances.remove(id);
                if (componentInstance != null) {
                    componentInstance.dispose();
                    importDeclaration.unhandle(serviceReference);
                }
            }
            // Remove the client
            clients.remove(id);
        } else {
            throw new BinderException("The given object has not been created through this factory");
        }
    }

    public String getName() {
        return name;
    }


    /*------------------------------------------*
     *  Component LifeCycle method              *
     *------------------------------------------*/

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.chameleon.rose.AbstractImporterComponent#start()
     */
    @Override
    @Validate
    protected void start() {
        super.start();
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
     */
    @Override
    @Invalidate
    protected void stop() {

        super.stop();

        for(Map.Entry<String, JsonRpcHttpClient> entry:clients.entrySet()){
            clients.remove(entry.getKey());
        }

        for(Map.Entry<String, ServiceRegistration> entry:registrations.entrySet()){
            registrations.remove(entry.getKey());
            entry.getValue().unregister();
        }

        for(Map.Entry<String, ComponentInstance> entry:componentInstances.entrySet()){
            componentInstances.remove(entry.getKey());
            entry.getValue().stop();
        }

    }
}
