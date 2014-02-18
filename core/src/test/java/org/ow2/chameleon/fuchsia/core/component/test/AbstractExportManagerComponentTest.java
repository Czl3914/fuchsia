package org.ow2.chameleon.fuchsia.core.component.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractExportManagerComponent;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AbstractExportManagerComponentTest {


    @Mock
    BundleContext bundleContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
        when(bundleContext.registerService(any(String[].class), any(Declaration.class), any(Dictionary.class)))
                .thenReturn(mock(ServiceRegistration.class));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInstantiation(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();
    }

    @Test
    public void testRegisterExportDeclaration(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        verify(bundleContext,times(1)).registerService(any(String[].class), eq(id), any(Dictionary.class));

        assertThat(testedClass.getExportDeclarations()).containsExactly(id);
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterTwoTimesExportDeclaration(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.addIdec(id);
    }

    @Test
    public void testUnregisterExportDeclaration(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        ServiceRegistration mockSR = mock(ServiceRegistration.class);

        testedClass.addIdec(id);
        testedClass.removeIdec(id);

        assertThat(testedClass.getExportDeclarations()).isEmpty();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnregisterTwoTimesExportDeclaration(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.removeIdec(id);
        testedClass.removeIdec(id);
    }

    @Test
    public void testStop(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.stop();

        assertThat(testedClass.getExportDeclarations()).isEmpty();
    }

    @Test
    public void testToString(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        String string = testedClass.toString();
        assertThat(string).isEqualTo("name");
    }



    public class TestedClass extends AbstractExportManagerComponent{

        protected TestedClass(BundleContext bundleContext) {
            super(bundleContext);
        }

        @Override
        public Logger getLogger() {
            return LoggerFactory.getLogger(this.getClass());
        }

        public String getName() {
            return "name";
        }

        @Override
        protected void start() {
            super.start();
        }

        @Override
        protected void stop() {
            super.stop();
        }

        public void addIdec(ExportDeclaration exportDeclaration){
            registerExportDeclaration(exportDeclaration);
        }

        public void removeIdec(ExportDeclaration exportDeclaration){
            unregisterExportDeclaration(exportDeclaration);
        }
    }
}
