package org.ow2.chameleon.fuchsia.core.it.components;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core [IntegrationTests]
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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.it.services.SimulateBindingInterface;

@Component(name = "SimpleDiscoveryFactory")
@Provides(specifications = {SimulateBindingInterface.class, DiscoveryService.class})
public class SimpleDiscovery extends AbstractDiscoveryComponent implements SimulateBindingInterface {

    public SimpleDiscovery(BundleContext bundleContext) {
        super(bundleContext);
    }

    public void bind(ImportDeclaration id) {
        super.registerImportDeclaration(id);
    }

    public void unbind(ImportDeclaration id) {
        super.unregisterImportDeclaration(id);
    }

    @Validate
    @Override
    protected void start() {
        super.start();
    }

    @Invalidate
    @Override
    protected void stop() {
        super.stop();
    }

    public String getName() {
        return "name";
    }

}
