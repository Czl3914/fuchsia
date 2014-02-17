package org.ow2.chameleon.fuchsia.exporter.jaxws.internal;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

public class CxfExporterPojo {

    private String clazz;
    private String webcontext;
    private String filter;

    private CxfExporterPojo(){

    }

    public static CxfExporterPojo create(ExportDeclaration exportDeclaration){
        CxfExporterPojo pojo=new CxfExporterPojo();

        pojo.clazz=(String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_TYPE);
        pojo.webcontext=(String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_WEB_CONTEXT);

        Object filterObject=exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_FILTER);

        if(filterObject!=null){
            pojo.filter = filterObject.toString();
        }


        return pojo;
    }

    public String getClazz() {
        return clazz;
    }

    public String getWebcontext() {
        return webcontext;
    }

    public String getFilter() {

        return filter;

    }
}
