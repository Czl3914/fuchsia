package org.ow2.chameleon.fuchsia.discovery.filebased;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery FileBased
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.discovery.filebased.monitor.DirectoryMonitor;
import org.ow2.chameleon.fuchsia.discovery.filebased.monitor.ExporterDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * This component instantiate a directory monitor (initially pointed to a directory in chameleon called "load/export") that reads all file placed there (as property files)
 * and publishes an {@link ExportDeclaration}
 *
 * @author jeremy.savonet@gmail.com
 * @author botelho (at) imag.fr
 * @author morgan.martinet@imag.fr
 */

@Component
@Provides(specifications = {DiscoveryService.class, ExporterDeployer.class})
public class FileBasedDiscoveryExport extends AbstractFileBasedDiscovery<ExportDeclaration> implements DiscoveryService, ExporterDeployer {

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedDiscoveryExport.class);

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_MONITORED_DIR_KEY, value = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_EXPORT_PROPERTY_KEY_MONITORED_DIR_VALUE)
    private String monitoredExportDirectory;

    @ServiceProperty(name = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_KEY, value = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_VALUE)
    private Long pollingTime;

    private DirectoryMonitor dm;

    public FileBasedDiscoveryExport(BundleContext bundleContext) {
        super(bundleContext, ExportDeclaration.class);
    }

    @Validate
    public void start() {
        super.start(this.monitoredExportDirectory, this.pollingTime);
        LOG.info("Filebased Export discovery up and running.");
    }

    @Invalidate
    public void stop() {
        super.stop();
        LOG.info("Filebased Export discovery stopped.");
    }

    public String getName() {
        return name;
    }



}
