package it.nextsw.olingo.querybuilder;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryBuilder;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;


/**
 * Classe che estende {@link JPAQueryBuilder}
 * Questa classe permette di avere gli entity listener istanziati da spring
 *
 * Created by f.longhitano on 28/06/2017.
 */
@Component
@Scope("prototype")
public class CustomJPAQueryBuilder extends JPAQueryBuilder {


    @Autowired
    private ApplicationContext applicationContext;
    private EntityManager em = null;


    public CustomJPAQueryBuilder(ODataJPAContext odataJPAContext) {

        super(odataJPAContext);
        em=odataJPAContext.getEntityManager();
    }

    /**
     * Listener di entita instanziate da spring
     *
     * @param uriParserResultView
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws EdmException
     */
    @Override
    public ODataJPATombstoneEntityListener getODataJPATombstoneEntityListener(UriInfo uriParserResultView)
            throws InstantiationException, IllegalAccessException, EdmException {
        JPAEdmMapping mapping = (JPAEdmMapping) uriParserResultView.getTargetEntitySet().getEntityType().getMapping();
        ODataJPATombstoneEntityListener listener = null;
        if (mapping.getODataJPATombstoneEntityListener() != null) {
           // listener = (ODataJPATombstoneEntityListener) mapping.getODataJPATombstoneEntityListener().newInstance();
            listener= applicationContext.getBean(mapping.getODataJPATombstoneEntityListener());
        }
        return listener;
    }

    // todo in verifica poi si spostera sulla libreria olingo
    @Override
    public void createQueriesForEntitySet(JPAQueryInfo queryInfo, String queryString){
        Query query=em.createQuery(queryString);
        queryInfo.setQuery(query);
        //calculate count query
        String countQueryString="select count(1) FROM "+queryString.split("(?i)FROM")[1];
        //rimuovo la orderBy
        countQueryString=countQueryString.split("(?i)ORDER BY")[0];
        Query countQuery=em.createQuery(countQueryString);
        queryInfo.setCountQuery(countQuery);
    }






}
