package org.ow2.chameleon.fuchsia.tools.shell;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.felix.ipojo.Factory.*;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;


@Component(immediate = true)
@Instantiate
@Provides(specifications = FuchsiaGogoCommand.class)
/**
 * {@link FuchsiaGogoCommand} is basic shell command set
 * Gogo {@link http://felix.apache.org/site/apache-felix-gogo.html} is used as base for this command
 *
 * @author jander nascimento (botelho at imag.fr)
 */
public class FuchsiaGogoCommand {

    private static final Logger LOG = LoggerFactory.getLogger(FuchsiaGogoCommand.class);

    private final BundleContext context;

    @ServiceProperty(name = "osgi.command.scope", value = "fuchsia")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"declarations", "declaration", "linker", "discovery", "importer", "exporter", "sendmessage"};

    @Requires
    private EventAdmin eventAdmin;

    public FuchsiaGogoCommand(BundleContext context) {
        this.context = context;
    }

    // ---------------- DECLARATION

    @Descriptor("Gets info about the declarations available")
    public void declarations(@Descriptor("declarations [--type import|export]") String... parameters) {
        String type = getArgumentValue("--type", parameters);
        try {
            if (type == null || type.equals("import")) {
                List<ServiceReference> allServiceRef = getAllServiceRefs(ImportDeclaration.class);
                displayDeclarationList(allServiceRef);
            }

            if (type == null || type.equals("export")) {
                List<ServiceReference> allServiceRef = getAllServiceRefs(ExportDeclaration.class);
                displayDeclarationList(allServiceRef);
            }
        } catch (Exception e) {
            LOG.error("failed to execute command", e);
            System.out.println("failed to execute the command");
        }
    }

    private String getIdentifier(Declaration declaration) {
        String type = null;
        if (declaration instanceof ImportDeclaration) {
            type = "import";
        } else if (declaration instanceof ExportDeclaration) {
            type = "export";
        }

        String id = (String) declaration.getMetadata().get(ID);

        return id;
    }

    private void displayDeclarationList(List<ServiceReference> references) {
        for (ServiceReference reference : references) {
            Declaration declaration = (Declaration) context.getService(reference);
            String state;
            if (declaration.getStatus().isBound()) {
                state = " BOUND ";
            } else {
                state = "UNBOUND";
            }
            String identifier = getIdentifier(declaration);
            System.out.printf("[%s]\t%s\n", state, identifier);
        }
    }


    @Descriptor("Gets info about the declaration")
    public void declaration(@Descriptor("declaration [-f LDAP_FILTER] [DECLARATION_ID]") String... parameters) {

        Filter filter = null;

        try {

            String explicitFilterArgument=getArgumentValue("-f",parameters);

            if(explicitFilterArgument==null){

                String idFilterArgument=getArgumentValue(null,parameters);

                if(idFilterArgument==null){
                    filter=null;
                }else {
                    filter=FrameworkUtil.createFilter(String.format("(id=%s)",idFilterArgument));
                }

            } else  {
                filter=FrameworkUtil.createFilter(getArgumentValue(null,parameters));
            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Failed to create the appropriate filter.", e);
            return;
        }

        Map<ServiceReference, Declaration> declarations = null;

        String type=getArgumentValue("-t",parameters);

        if (type!=null && type.equals("import")) {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ImportDeclaration.class));
        } else if (type!=null && type.equals("export")) {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ExportDeclaration.class));
        } else {
            declarations = new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(Declaration.class));
            declarations.putAll(new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ImportDeclaration.class)));
            declarations.putAll(new HashMap<ServiceReference, Declaration>(getAllServiceRefsAndServices(ExportDeclaration.class)));
        }

        if (declarations.isEmpty()) {
            System.err.println("No declarations found.");
            return;
        }

        for (Map.Entry<ServiceReference, Declaration> declaration : declarations.entrySet()) {

            if (filter==null || filter.matches(declaration.getValue().getMetadata())) {

                displayDeclaration(getIdentifier(declaration.getValue()), declaration.getKey(), declaration.getValue());

            }

        }

    }

    private void displayDeclaration(String identifier, ServiceReference reference, Declaration declaration) {

        StringBuilder sg = new StringBuilder();
        sg.append("Declaration Metadata : \n");
        for (Map.Entry<String, Object> entry : declaration.getMetadata().entrySet()) {
            sg.append(String.format("\t%s = %s\n", entry.getKey(), entry.getValue()));
        }
        sg.append("Declaration ExtraMetadata : \n");
        for (Map.Entry<String, Object> entry : declaration.getExtraMetadata().entrySet()) {
            sg.append(String.format("\t%s = %s\n", entry.getKey(), entry.getValue()));
        }

        System.out.printf("Service Properties\n");
        for (String propertyKey : reference.getPropertyKeys()) {
            sg.append(String.format("\t%s = %s\n", propertyKey, reference.getProperty(propertyKey)));
        }
        if (reference.getPropertyKeys().length == 0) {
            sg.append("\tEMPTY\n");
        }

        sg.append("Declaration binded to " + declaration.getStatus().getServiceReferencesBounded().size() + " services.\n");
        sg.append("Declaration handled by " + declaration.getStatus().getServiceReferencesHandled().size() + " services.\n");

        System.out.println(sg.toString());

    }


    // ---------------- LINKER

    @Descriptor("Gets the importation/exportation linker available")
    public void linker(@Descriptor("linker [-(import|export)] [ID name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] exportationLinkerRef = context.getAllServiceReferences(ExportationLinker.class.getName(), filter);
            ServiceReference[] importationLinkerRef = context.getAllServiceReferences(ImportationLinker.class.getName(), filter);
            if (exportationLinkerRef != null || importationLinkerRef != null) {
                if (exportationLinkerRef != null)
                    for (ServiceReference reference : exportationLinkerRef) {
                        displayServiceInfo("Exportation Linker", reference);
                        displayServiceProperties("Exportation Linker", reference, "\t\t");
                    }
                if (importationLinkerRef != null)
                    for (ServiceReference reference : importationLinkerRef) {
                        displayServiceInfo("Importation Linker", reference);
                        displayServiceProperties("Importation Linker", reference, "\t\t");
                    }
            } else {
                System.out.println("No linkers available.");
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("invalid LDAP filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- DISCOVERY

    @Descriptor("Gets the discovery available in the platform")
    public void discovery(@Descriptor("discovery [discovery name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] discoveryRef = context.getAllServiceReferences(DiscoveryService.class.getName(), filter);
            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {
                    displayServiceInfo("Discovery", reference);
                    displayServiceProperties("Discovery", reference, "\t\t");
                }
            } else {
                System.out.println("No discovery available.");
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("invalid ldap filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- IMPORTER

    @Descriptor("Gets the importer available in the platform")
    public void importer(@Descriptor("importer [importer name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] discoveryRef = context.getAllServiceReferences(ImporterService.class.getName(), filter);
            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {
                    displayServiceInfo("Importer", reference);
                    ImporterService is = (ImporterService) context.getService(reference);
                    System.out.println(String.format("\t*importer name = %s", is.getName()));
                    displayServiceProperties("Importer", reference, "\t\t");
                }
            } else {
                System.out.println("No importers available.");
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("invalid ldap filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- EXPORTER

    @Descriptor("Gets the exporter available in the platform")
    public void exporter(@Descriptor("exporter [exporter name]") String... parameters) {
        String filter = null;
        try {
            ServiceReference[] discoveryRef = context.getAllServiceReferences(ExporterService.class.getName(), filter);
            if (discoveryRef != null) {
                for (ServiceReference reference : discoveryRef) {
                    displayServiceInfo("Exporter", reference);
                    ExporterService es = (ExporterService) context.getService(reference);
                    System.out.println(String.format("\t*exporter name = %s", es.getName()));
                    displayServiceProperties("Exporter", reference, "\t\t");
                }
            } else {
                System.out.println("No exporter available.");
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("invalid ldap filter syntax", e);
            System.out.println("failed to execute the command");
        }
    }

    // ---------------- SEND MESSAGE

    @Descriptor("Send event admin messages")
    public void sendmessage(@Descriptor("sendmessage BUS [KEY=VALUE ]*") String... parameters) {
        assert parameters[0] != null;
        String bus = parameters[0];
        Dictionary eventAdminPayload = new Hashtable();
        for (String m : parameters) {
            if (m.contains("=")) {
                StringTokenizer st = new StringTokenizer(m, "=");
                assert st.countTokens() == 2;
                String key = st.nextToken();
                String value = st.nextToken();
                eventAdminPayload.put(key, value);
            }
        }
        Event eventAdminMessage = new Event(bus, eventAdminPayload);
        System.out.println(String.format("Sending message to the bus %s with the arguments %s", bus, eventAdminPayload));
        System.out.println("Event admin message sent");
        eventAdmin.sendEvent(eventAdminMessage);
    }


    // ---------------- UTILS SERVICES

    private <T> List<ServiceReference> getAllServiceRefs(Class<T> klass) {
        ServiceReference[] importDeclarationsRef;
        try {
            importDeclarationsRef = context.getAllServiceReferences(klass.getName(), null);
        } catch (InvalidSyntaxException e) {
            LOG.error("Failed to retrieved services " + klass.getName(), e);
            return new ArrayList<ServiceReference>();
        }
        if (importDeclarationsRef != null) {
            return Arrays.asList(importDeclarationsRef);
        }
        return new ArrayList<ServiceReference>();
    }

    private <T> List<T> getAllServices(Class<T> klass) {
        List<T> services = new ArrayList<T>();
        for (ServiceReference sr : getAllServiceRefs(klass)) {
            services.add((T) context.getService(sr));
        }
        return services;
    }

    private <T> Map<ServiceReference, T> getAllServiceRefsAndServices(Class<T> klass) {
        Map<ServiceReference, T> services = new HashMap<ServiceReference, T>();
        for (ServiceReference sr : getAllServiceRefs(klass)) {
            services.put(sr, (T) context.getService(sr));
        }

        return services;
    }

    // ---------------- UTILS DISPLAY

    private static void displayServiceProperties(String prolog, ServiceReference reference, String prefixTabulation) {
        System.out.printf("%s Service Properties\n", prolog);
        for (String propertyKey : reference.getPropertyKeys()) {
            System.out.println(String.format(prefixTabulation + "%s\t\t = %s", propertyKey, reference.getProperty(propertyKey)));
        }
        if (reference.getPropertyKeys().length == 0) {
            System.out.println(prefixTabulation + "EMPTY");
        }
    }

    private static void displayServiceInfo(String prolog, ServiceReference reference) {
        System.out.println(String.format("%s [%s] provided by bundle %s (%s)", prolog, reference.getProperty(INSTANCE_NAME_PROPERTY), reference.getBundle().getSymbolicName(), reference.getBundle().getBundleId()));
    }

    // ---------------- UTILS GOGO ARGUMENTS

    private static String getArgumentValue(String option, String... params) {
        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {

            /**
             * In case of a Null option, returns the last parameter
             */
            if(option==null){
                return params[params.length-1];
            }

            if (i < (params.length - 1) && params[i].equals(option)) {
                found = true;
                value = params[i + 1];
                break;
            }
        }

        if (found) {
            return value;
        }
        return null;
    }

}


