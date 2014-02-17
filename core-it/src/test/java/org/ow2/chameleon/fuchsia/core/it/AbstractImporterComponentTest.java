package org.ow2.chameleon.fuchsia.core.it;

import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.it.components.SimpleImporter;
import org.ow2.chameleon.fuchsia.testing.CommonTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


public class AbstractImporterComponentTest extends CommonTest {

    private ComponentInstance testedCI;

    private SimpleImporter simpleImporter;
    private SimpleImporter spySimpleImporter;

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "org.ow2.chameleon.fuchsia.core.it.components"
        );
    }

    @Before
    public void setUp() {
        testedCI = ipojoHelper.createComponentInstance("SimpleImporterFactory", "SimpleImporterInstance");
        assertThat(testedCI).isNotNull();
        simpleImporter = (SimpleImporter) ipojoHelper.getServiceObjectByName(ImporterService.class ,"SimpleImporterInstance");
        assertThat(simpleImporter).isNotNull();
        assertThat(simpleImporter.getName()).isEqualTo("simpleImporter");
        assertThat(simpleImporter).isInstanceOf(SimpleImporter.class);

        spySimpleImporter = spy(simpleImporter);
    }

    @After
    public void tearDown() {
        simpleImporter = null;
        testedCI.dispose();
    }

    @Override
    public boolean deployTestBundle() {
        return true;
    }

    @Override
    public boolean quiet() {
        return true;
    }

    @Test
    public void testInstanceIsHere() {
        assertThat(testedCI).isNotNull();
        assertThat(ipojoHelper.isInstanceValid(testedCI)).isTrue();
        assertThat(simpleImporter).isNotNull();
        assertThat(simpleImporter).isInstanceOf(ImporterService.class).isInstanceOf(AbstractImporterComponent.class);
    }

    @Test
    public void testImportDeclarationAddAndRemove() throws BinderException {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        spySimpleImporter.addImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).containsOnly(iDec);
        assertThat(simpleImporter.nbProxies()).isEqualTo(1);

        spySimpleImporter.removeImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).isEmpty();
        assertThat(simpleImporter.nbProxies()).isEqualTo(0);
    }


    @Test
    public void testImportDeclarationAddAndStopServiceImporter() throws BinderException {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        spySimpleImporter.addImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).containsOnly(iDec);
        assertThat(simpleImporter.nbProxies()).isEqualTo(1);

        spySimpleImporter.stop();
        assertThat(simpleImporter.nbProxies()).isEqualTo(0);
        assertThat(simpleImporter.getImportDeclarations()).isEmpty();
    }


    class anyDeclaration extends ArgumentMatcher<ImportDeclaration> {
        public boolean matches(Object obj) {
            return true; //(obj instanceof ImportDeclaration);
        }
    }

}
