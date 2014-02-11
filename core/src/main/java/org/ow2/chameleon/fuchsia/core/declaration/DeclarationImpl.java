package org.ow2.chameleon.fuchsia.core.declaration;

import org.apache.felix.ipojo.Factory;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link DeclarationImpl} is the reference implementation of the {@link Declaration} interface of Fuchsia
 *
 * @author jnascimento
 * @author Morgan Martinet
 */
class DeclarationImpl implements Declaration, ImportDeclaration, ExportDeclaration {

    // the lock used to synchronized serviceReferencesBound and serviceReferencesHandled
    private final Object lock;

    // List of importerServices bind to this Declaration
    private final List<ServiceReference> serviceReferencesBound;

    // List of importerServices which are currently doing something with this Declaration
    // i.e. the importerService has created a proxy from the declaration
    private final List<ServiceReference> serviceReferencesHandled;

    // The metadata of the Declaration
    private final Map<String, Object> metadata;

    // The extra-metadata of the Declaration. Must be empty in the DeclarationImpl because the discovery
    // mustn't give any extraMetadata, all the information from the discovery must be in the metadata
    // the extraMetadata are provided in the DeclarationDecorator
    private final Map<String, Object> extraMetadata;

    DeclarationImpl(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Can't create a Declaration without metadata.");
        }
        this.metadata = Collections.unmodifiableMap(new HashMap<String, Object>(metadata));
        this.extraMetadata = Collections.unmodifiableMap(new HashMap<String, Object>());
        this.serviceReferencesBound = new ArrayList<ServiceReference>();
        this.serviceReferencesHandled = new ArrayList<ServiceReference>();
        this.lock = new Object();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getExtraMetadata() {
        return extraMetadata;
    }

    public Status getStatus() {
        synchronized (lock) {
            return Status.from(serviceReferencesBound, serviceReferencesHandled);
        }
    }

    public void bind(ServiceReference serviceReference) {
        synchronized (lock) {
            serviceReferencesBound.add(serviceReference);
        }
    }

    public void unbind(ServiceReference serviceReference) {
        synchronized (lock) {
            if (!serviceReferencesHandled.contains(serviceReference)) {
                serviceReferencesBound.remove(serviceReference);
            } else {
                String name = (String) serviceReference.getProperty(Factory.INSTANCE_NAME_PROPERTY);
                throw new IllegalStateException(name + " want to unbound a declaration that it is still handling.");
            }
        }
    }

    public void handle(ServiceReference serviceReference) {
        synchronized (lock) {
            if (serviceReferencesBound.contains(serviceReference)) {
                serviceReferencesHandled.add(serviceReference);
            } else {
                String name = (String) serviceReference.getProperty(Factory.INSTANCE_NAME_PROPERTY);
                throw new IllegalStateException(name + " cannot handle a declaration that it's not bound to it");
            }
        }
    }

    public void unhandle(ServiceReference serviceReference) {
        synchronized (lock) {
            serviceReferencesHandled.remove(serviceReference);
        }
    }

    public String toString() {
        //TODO Implement this for debug purposes
        return super.toString();
    }
}
