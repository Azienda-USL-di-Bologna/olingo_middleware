/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.nextsw.olingo.interceptor.queryextension;



import it.nextsw.olingo.interceptor.registry.OlingoRequestInterceptorRegistry;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAQueryExtensionEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;


/**
 * todo aggiunti if su olingoRequestInterceptorRegistry in attesa di decidere come gestire la questione degli aggiornamenti con link meta
 *
 * http://localhost:10005/odata.svc/Utentes(12604)
 *
 *
 *    {
 *      "idRuolo": {
 *          "__metadata": {
 *          "id": "http://localhost:10005/odata.svc/Ruolos(0)",
 *          "uri": "http://localhost:10005/odata.svc/Ruolos(0)",
 *          "type": "Model.Ruolo"
 *          }
 *      }
 *  }
 */
@Component
@Scope("prototype")
public class BaseOdataJpaQueryExtension extends ODataJPAQueryExtensionEntityListener {

    private final static Logger logger = Logger.getLogger(BaseOdataJpaQueryExtension.class.getName());


    @Autowired
    protected OlingoRequestInterceptorRegistry olingoRequestInterceptorRegistry;


    /**
     * Query single
     *
     * @param uriInfo
     * @param em
     * @return
     */
    @Override
    public Query getQuery(GetEntityUriInfo uriInfo, EntityManager em) {
        Query query = null;
        JPQLContextType contextType = null;
        if (uriInfo.getNavigationSegments().size() > 0) {
            contextType = JPQLContextType.JOIN_SINGLE;
        } else {
            contextType = JPQLContextType.SELECT_SINGLE;
        }
        JPQLContext jpqlContext;
        try {
            jpqlContext = JPQLContext.createBuilder(contextType, uriInfo).build();
            String queryString = JPQLStatement.createBuilder(jpqlContext).build().toString();
            if(olingoRequestInterceptorRegistry!=null)
                queryString = olingoRequestInterceptorRegistry.executePreSecurityOperation(getLoggedUtenteAuthentication(), uriInfo, jpqlContext);
            logger.info("query create: " + queryString);

            query = em.createQuery(queryString);

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();


        } catch (Exception e) {
            logger.error("Error on getQuery",e);
            throw new IllegalStateException("Error on getQuery",e);
        }
        return query;
    }

    /**
     * Query collection
     *
     * @param resultsView
     * @param em
     * @return
     * @throws ODataJPARuntimeException
     */
    @Override
    public String getQueryString(GetEntitySetUriInfo resultsView, EntityManager em) throws ODataJPARuntimeException {
        String queryString = null;
        try {

            JPQLContextType contextType = null;
            if (!resultsView.getStartEntitySet().getName().equals(resultsView.getTargetEntitySet().getName())) {
                contextType = JPQLContextType.JOIN;
            } else {
                contextType = JPQLContextType.SELECT;
            }
            JPQLContext.JPQLContextBuilder jpqlContextBuilder = JPQLContext.createBuilder(contextType, resultsView);
            JPQLContext jpqlContext = jpqlContextBuilder.build();

            JPQLStatement.JPQLStatementBuilder jpqlStatementBuilder = JPQLStatement.createBuilder(jpqlContext);
            JPQLStatement jpqlStatement = jpqlStatementBuilder.build();

            queryString = jpqlStatement.toString();
            logger.info("query create: " + queryString);

            if(olingoRequestInterceptorRegistry!=null)
                queryString = olingoRequestInterceptorRegistry.executePreSecurityOperation(getLoggedUtenteAuthentication(), resultsView, jpqlContext);

            //query = em.createQuery(queryString);


        } catch (Exception e) {
            logger.error("Error on getQuery",e);
            throw new IllegalStateException("Error on getQuery",e);
        }

        return queryString;
    }

    /**
     * Update
     *
     * @param uriInfo
     * @param em
     * @return
     * @throws ODataJPARuntimeException
     */
    @Override
    public Query getQuery(PutMergePatchUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
        try {
            JPQLContextType contextType = null;
            if (uriInfo.getNavigationSegments().size() > 0) {
                contextType = JPQLContextType.JOIN_SINGLE;
            } else {
                contextType = JPQLContextType.SELECT_SINGLE;
            }
            JPQLContext jpqlContext = JPQLContext.createBuilder(contextType, uriInfo).build();

            //JPQLContext jpqlContext = buildJPQLContext(contextType, uriParserResultView);
            JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext).build();

            String queryString = jpqlStatement.toString();
            logger.info("Update query " + queryString);

            if(olingoRequestInterceptorRegistry!=null)
                queryString = olingoRequestInterceptorRegistry.executePreSecurityOperation(getLoggedUtenteAuthentication(), uriInfo, jpqlContext);

            return em.createQuery(queryString);
        } catch (Exception e) {
            logger.error("Errore getQuery update", e);
            throw new IllegalStateException("Error on getQuery",e);
        }

    }

    /**
     * delete
     *
     * @param uriInfo
     * @param em
     * @return
     * @throws ODataJPARuntimeException
     */
    @Override
    public Query getQuery(DeleteUriInfo uriInfo, EntityManager em) throws ODataJPARuntimeException {
        try {
            JPQLContextType contextType;

            if (uriInfo.getNavigationSegments().size() > 0) {
                contextType = JPQLContextType.JOIN_SINGLE;
            } else {
                contextType = JPQLContextType.SELECT_SINGLE;
            }
            JPQLContext jpqlContext = JPQLContext.createBuilder(contextType, uriInfo).build();
            JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext).build();
            String queryString = jpqlStatement.toString();
            logger.info("Delete query " + queryString);

            if(olingoRequestInterceptorRegistry!=null)
                queryString = olingoRequestInterceptorRegistry.executePreSecurityOperation(getLoggedUtenteAuthentication(), uriInfo, jpqlContext);

            return em.createQuery(queryString);
        } catch (Exception e) {
            logger.error("Errore getQuery delete ", e);
            throw new IllegalStateException("Error on getQuery",e);
        }

    }

    protected Authentication getLoggedUtenteAuthentication() {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    return SecurityContextHolder.getContext().getAuthentication();
                }
            }
        }
        return null;
    }




}