package org.ow2.chameleon.fuchsia.discovery.filebased;

import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationRegistrationManager;
import org.ow2.chameleon.fuchsia.core.declaration.*;
import org.ow2.chameleon.fuchsia.discovery.filebased.monitor.Deployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

abstract class AbstractFileBasedDiscovery<D extends Declaration> implements Deployer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFileBasedDiscovery.class);

    private final Map<String, D> declarationsFiles;
    private final DeclarationRegistrationManager<D> declarationRegistrationManager;
    private final Class<D> klass;

    private final BundleContext bundleContext;

    public AbstractFileBasedDiscovery(BundleContext bundleContext, Class<D> klass) {
        this.bundleContext = bundleContext;
        this.klass = klass;
        declarationsFiles = new HashMap<String, D>();
        declarationRegistrationManager  = new DeclarationRegistrationManager<D>(bundleContext, klass);
    }

    public boolean accept(File file) {
        return !file.exists() || (!file.isHidden() && file.isFile());
    }

    private Properties parseFile(File file) throws Exception {
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            properties.load(is);
        } catch (Exception e) {
            throw new Exception(String.format("Error reading declaration file %s", file.getAbsoluteFile()), e);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        if (!properties.containsKey(Constants.ID)) {
            throw new Exception(String.format("File %s is not a correct declaration, needs to contains an id property", file.getAbsoluteFile()));
        }
        return properties;
    }

    public void onFileCreate(File file) {
        LOG.info("New file detected : {}", file.getAbsolutePath());
        try {
            Properties properties = parseFile(file);
            Map<String, Object> metadata = new HashMap<String, Object>();
            for (Map.Entry<Object, Object> element : properties.entrySet()) {
                Object replacedObject = metadata.put(element.getKey().toString(), element.getValue());
                if (replacedObject != null) {
                    LOG.warn("ExportDeclaration: replacing metadata key {}, that contained the value {} by the new value {}", new Object[]{element.getKey(), replacedObject, element.getValue()});
                }
            }
            D declaration = createAndRegisterDeclaration(metadata);
            declarationsFiles.put(file.getAbsolutePath(), declaration);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    // FIXME : this have to be rechecked, this is an pessimist approach
    public void onFileChange(File file) {
        LOG.info("File updated : {}", file.getAbsolutePath());
        onFileDelete(file);
        onFileCreate(file);
    }

    public void onFileDelete(File file) {
        LOG.info("File removed : {}", file.getAbsolutePath());
        D declaration = declarationsFiles.get(file.getAbsolutePath());

        if (declaration == null) {
            return;
        }

        if (declarationsFiles.remove(file.getAbsolutePath()) == null) {
            LOG.error("Failed to unregister export declaration file mapping ({}),  it did not existed before.", file.getAbsolutePath());
        } else {
            LOG.info("import declaration file mapping removed.");
        }

        try {
            unregisterDeclaration(declaration);
        } catch (IllegalStateException e) {
            LOG.error("Failed to unregister export declaration file " + declaration.getMetadata() + ",  it did not existed before.", e);
        }
    }

    public void open(Collection<File> files) {
        for (File file : files) {
            onFileChange(file);
        }
    }

    public void close() {
        // nothing to do ?
    }

    private D createAndRegisterDeclaration(Map<String, Object> metadata) {
        D declaration;
        if(klass.equals(ImportDeclaration.class)){
            declaration = (D) ImportDeclarationBuilder.fromMetadata(metadata).build();
        }else if(klass.equals(ExportDeclaration.class)){
            declaration = (D) ExportDeclarationBuilder.fromMetadata(metadata).build();
        }else{
            throw new IllegalStateException("");
        }
        declarationRegistrationManager.registerDeclaration(declaration);
        return declaration;
    }

    private void unregisterDeclaration(D declaration) {
        declarationRegistrationManager.unregisterDeclaration(declaration);
    }


    BundleContext getBundleContext() {
        return bundleContext;
    }


    void start(){

    }


    void stop(){
        declarationsFiles.clear();
        declarationRegistrationManager.unregisterAll();
    }
}
