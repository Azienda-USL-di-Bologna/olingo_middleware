package it.nextsw.olingo.context;

import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAContextImpl;

/**
 * Classe che estende {@link ODataJPAContextImpl} e permette di customizzare e aggiungere eventuali campi
 * Created by f.longhitano on 22/06/2017.
 */
public class CustomOdataJpaContextImpl extends ODataJPAContextImpl {


    public CustomOdataJpaContextImpl() {
    }

    /**
     * Costruttore alternativo che permette di passare il contesto al OdataJPAContextImpl, che non è implementato
     * @param oDataContext
     */
    public CustomOdataJpaContextImpl(ODataContext oDataContext) {
        setODataContext(oDataContext);
    }

}
