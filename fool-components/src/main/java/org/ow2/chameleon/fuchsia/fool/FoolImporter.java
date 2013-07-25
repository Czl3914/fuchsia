package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.util.BadImportRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component(name = "Fuchsia-FoolImporter-Factory")
@Provides(specifications = ImporterService.class)
@Instantiate(name = "Fuchsia-FoolImporter")
public class FoolImporter extends AbstractImporterComponent {
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ServiceProperty(name = "target", value = "(fool-number=1)")
    private String filter;

    @ServiceProperty(name = "instance.name")
    private String name;

    @Override
    @Validate
    protected void stop() {
        super.stop();
    }

    @Override
    @Invalidate
    protected void start() {
        super.start();
    }

    @Override
    protected void createProxy(ImportDeclaration importDeclaration) {
        logger.debug("FoolImporter create a proxy for " + importDeclaration);
    }

    @Override
    protected void destroyProxy(ImportDeclaration importDeclaration) {
        logger.debug("FoolImporter destroy a proxy for " + importDeclaration);
    }

    public List<String> getConfigPrefix() {
        List<String> l = new ArrayList<String>();
        l.add("fool");
        return l;
    }

    public String getName() {
        return name;
    }
}
