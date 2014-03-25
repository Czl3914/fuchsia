package org.ow2.chameleon.fuchsia.core.component.test;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AbstractImporterComponentTest {
    private static final int IMPORT_MAX = 10; //Number max of Import to be tested within a single test.

    //Mock object
    @Mock
    LogService logservice;

    @Mock
    ImportationLinker importationLinker;

    //Tested Object
    private TestedClass testedClass;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
        testedClass = new TestedClass();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testImportDeclaration() throws BinderException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.addDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that useImportDeclaration has been called

        testedClass.removeDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyImportDeclaration has been called
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateImportDeclaration() throws BinderException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.addDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that useImportDeclaration has been called

        testedClass.addDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that the importer handle correctly the duplication
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveImportDeclarationNotAdded() throws BinderException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.removeDeclaration(idec);
    }

    @Test
    public void testGetImportDeclaration() throws BinderException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.addDeclaration(idec);

        Set<ImportDeclaration> importDeclarations = testedClass.getImportDeclarations();
        assertThat(importDeclarations).containsExactly(idec);
    }


    @Test
    public void testMultiplesImportDeclaration() throws BinderException {
        Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        for (int i = 0; i < IMPORT_MAX; i++) {
            ImportDeclaration idec = mock(ImportDeclaration.class);
            testedClass.addDeclaration(idec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that useImportDeclaration has been called

            decs.add(idec);
        }

        for (ImportDeclaration idec : decs) {
            testedClass.removeDeclaration(idec);
        }

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyImportDeclaration has been called
    }


    @Test
    public void testStop() throws BinderException {
        Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        for (int i = 0; i < IMPORT_MAX; i++) {
            ImportDeclaration idec = mock(ImportDeclaration.class);
            testedClass.addDeclaration(idec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that useImportDeclaration has been called

            decs.add(idec);
        }

        testedClass.stop();

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyImportDeclaration has been called
    }

    @Test
    public void testToString() throws BinderException {
        String ts = testedClass.toString();
        assertThat(ts).isEqualTo("name");

    }
    public class TestedClass extends AbstractImporterComponent {

        private final Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        @Override
        protected void useImportDeclaration(ImportDeclaration importDeclaration) {
            decs.add(importDeclaration);
        }

        @Override
        protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
            decs.remove(importDeclaration);
        }

        public int nbProxies() {
            return decs.size();
        }


        @Override
        protected void stop() {
            super.stop();
        }

        @Override
        protected void start() {
            super.start();
        }

        public String getName() {
            return "name";
        }

    }

}
