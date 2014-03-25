package org.ow2.chameleon.fuchsia.core.component.manager;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
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

import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class DeclarationBindManager<T extends Declaration> {
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DeclarationBindManager.class);

    private final Set<T> declarations;
    private final DeclarationBinder<T> declarationBinder;

    public DeclarationBindManager(DeclarationBinder<T> declarationBinder) {
        declarations = new HashSet<T>();
        this.declarationBinder = declarationBinder;
    }

    /**
     * @param declaration The {@link org.ow2.chameleon.fuchsia.core.declaration.Declaration} of the service to be imported.
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    public void addDeclaration(final T declaration) throws BinderException {
        synchronized (declarations) {
            if (declarations.contains(declaration)) {
                // Already register
                throw new IllegalStateException("Duplicate Declaration : " +
                        "this Declaration has already been treated.");
            }
            // First registration, give it to the implementation class and keep it in memory
            declarationBinder.useDeclaration(declaration);
            declarations.add(declaration);
        }
    }

    /**
     * @param declaration The {@link Declaration} of the service to stop to be imported.
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    public void removeDeclaration(final T declaration) throws BinderException {
        synchronized (declarations) {
            if (!declarations.contains(declaration)) {
                throw new IllegalStateException("The given Declaration has never been added"
                        + "or have already been removed.");
            }
            declarations.remove(declaration);
        }
        declarationBinder.denyDeclaration(declaration);
    }

    public Set<T> getDeclarations() {
        synchronized (declarations) {
            return new HashSet<T>(declarations);
        }
    }

    public void unbindAll() {
        synchronized (declarations) {
            // deny all the Declarations
            for (T declaration : declarations) {
                try {
                    declarationBinder.denyDeclaration(declaration);
                } catch (BinderException e) {
                    LOG.error("An exception has been thrown while denying the declaration "
                            + declaration
                            + "Stopping in progress.", e);
                }
            }
            // Clear the map
            declarations.clear();
        }
    }
}
