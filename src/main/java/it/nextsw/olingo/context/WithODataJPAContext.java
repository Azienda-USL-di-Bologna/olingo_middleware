package it.nextsw.olingo.context;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;

/**
 * Created by user on 23/06/2017.
 */
public interface WithODataJPAContext {
    public ODataJPAContext getoDataJPAContext();

    public void setoDataJPAContext(ODataJPAContext oDataJPAContext);

}
