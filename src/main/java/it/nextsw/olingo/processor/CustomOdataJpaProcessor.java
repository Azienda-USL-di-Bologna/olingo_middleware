package it.nextsw.olingo.processor;


import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAProcessor;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Classe che stende {@link ODataJPAProcessor}
 * Permette di specificare nel costruttore quale {@link JPAProcessor} utilizzare
 *
 * Created by f.longhitano on 21/06/2017.
 */
@Component
@Scope("prototype")
public class CustomOdataJpaProcessor extends ODataJPAProcessor {

    @Autowired
    private ApplicationContext applicationContext;


    /**
     * Constructor
     *
     * @param oDataJPAContext non null OData JPA Context object
     */
    public CustomOdataJpaProcessor(ODataJPAContext oDataJPAContext, JPAProcessor jpaProcessor) {
        super(oDataJPAContext);
        this.jpaProcessor=jpaProcessor;
    }

    public ODataJPAContext getoDataJPAContext() {
        return oDataJPAContext;
    }

    public void setoDataJPAContext(ODataJPAContext oDataJPAContext) {
        this.oDataJPAContext = oDataJPAContext;
    }
}
