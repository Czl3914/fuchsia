/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.importer.knx;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;

import static org.apache.felix.ipojo.configuration.Instance.instance;

//@Configuration
public class KNXImporterConfig {

    Instance fileBasedDiscoveryImport = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport")
            .named("FileBasedDiscoveryImport");

    Instance knxImporter = instance()
            .of(KNXDeviceLightImporter.class.getName())
            .with("target").setto("(discovery.knx.device.addr=*)");


    Instance knxLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(discovery.knx.device.addr=*)")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=knxImporter)");

}
