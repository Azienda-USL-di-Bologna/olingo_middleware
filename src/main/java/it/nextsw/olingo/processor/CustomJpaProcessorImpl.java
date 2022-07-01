package it.nextsw.olingo.processor;


import it.nextsw.olingo.querybuilder.CustomJPAQueryBuilder;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATombstoneEntityListener;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATransaction;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAFunction;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAMethodContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.core.ODataEntityParser;
import org.apache.olingo.odata2.jpa.processor.core.access.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe che estende {@link org.apache.olingo.odata2.jpa.processor.core.access.data.JPAProcessorImpl}
 * Utilizza {@link CustomJPAQueryBuilder} al posto del {@link JPAQueryBuilder} di default di olingo
 *
 * Fa l'Autowire sulle classi registrate come FunctionImport
 *
 * Created by f.longhitano on 21/06/2017.
 */
@Component
@Scope("prototype")
public class CustomJpaProcessorImpl extends JPAProcessorImpl {

    private static final String DELTATOKEN = "!deltatoken";

    @Autowired
    protected ApplicationContext applicationContext;

    protected EntityManager em;

    private ODataJPAContext oDataJPAContext;


    public CustomJpaProcessorImpl(ODataJPAContext oDataJPAContext) {

        super(oDataJPAContext);
        this.oDataJPAContext=oDataJPAContext;
        this.em=oDataJPAContext.getEntityManager();
    }

    @Override
    protected JPAQueryBuilder getJpaQueryBuilder(){
        CustomJPAQueryBuilder jpaQueryBuilder=applicationContext.getBean(CustomJPAQueryBuilder.class,oDataJPAContext);
        return jpaQueryBuilder;
    }

    @Override
    protected void additionalOperationOnJPAMethodContext(JPAMethodContext jpaMethodContext){
        AutowireCapableBeanFactory autowireCapableBeanFactory=applicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBeanProperties(jpaMethodContext.getEnclosingObject(),AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,false);
    }




//    /**
//     * Process utilizzato per le chiamate odata con filtri
//     *
//     * @param uriParserResultView
//     * @return
//     * @throws ODataJPAModelException
//     * @throws ODataJPARuntimeException
//     */
//    @Override
//    public List<Object> process(final GetEntitySetUriInfo uriParserResultView)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//
//        List<Object> result = null;
//        if (uriParserResultView.getFunctionImport() != null) {
//            return (List<Object>) process((GetFunctionImportUriInfo) uriParserResultView);
//        }
//
//        InlineCount inlineCount = uriParserResultView.getInlineCount();
//        Integer top = uriParserResultView.getTop() == null ? 1 : uriParserResultView.getTop().intValue();
//        boolean hasNoAllPages = inlineCount == null ? true : !inlineCount.equals(InlineCount.ALLPAGES);
//        if (top.intValue() == 0 && hasNoAllPages) {
//            return new ArrayList<Object>();
//        }
//
//        try {
//            JPAEdmMapping mapping = (JPAEdmMapping) uriParserResultView.getTargetEntitySet().getEntityType().getMapping();
//            CustomJPAQueryBuilder queryBuilder = getCustomJpaQueryBuilder(oDataJPAContext);
//            //istanzio il mio JPAQueryInfo usando la reflection
//            JPAQueryInfo queryInfo = new JPAQueryInfo(queryBuilder.build(uriParserResultView));
//
//            Query query = queryInfo.getQuery();
//            ODataJPATombstoneEntityListener listener =
//                    queryBuilder.getODataJPATombstoneEntityListener((UriInfo) uriParserResultView);
//            Map<String, String> customQueryOptions = uriParserResultView.getCustomQueryOptions();
//            String deltaToken = null;
//            if (customQueryOptions != null) {
//                deltaToken = uriParserResultView.getCustomQueryOptions().get(DELTATOKEN);
//            }
//            if (deltaToken != null) {
//                ODataJPATombstoneContext.setDeltaToken(deltaToken);
//            }
//            if (listener != null && (!queryInfo.isTombstoneQuery() && listener.isTombstoneSupported())) {
//                query.getResultList();
//                List<Object> deltaResult =
//                        (List<Object>) ODataJPATombstoneContext.getDeltaResult(((EdmMapping) mapping).getInternalName());
//                result = handlePaging(deltaResult, uriParserResultView);
//            } else {
//                result = handlePaging(query, uriParserResultView);
//            }
//            if (listener != null && listener.isTombstoneSupported()) {
//                ODataJPATombstoneContext.setDeltaToken(listener.generateDeltaToken((List<Object>) result, query));
//            }
//            return result == null ? new ArrayList<Object>() : result;
//        } catch (EdmException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        } catch (InstantiationException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        } catch (IllegalAccessException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        }
//    }
//
//    /** Classe privata copiata dalla classe JPAProcessorImpl in quanto privata
//     *
//     * @param result
//     * @param uriParserResultView
//     * @return
//     */
//    private List<Object> handlePaging(final List<Object> result, final GetEntitySetUriInfo uriParserResultView) {
//        if (result == null) {
//            return null;
//        }
//        JPAPage.JPAPageBuilder pageBuilder = new JPAPage.JPAPageBuilder();
//        pageBuilder.pageSize(oDataJPAContext.getPageSize())
//                .entities(result)
//                .skipToken(uriParserResultView.getSkipToken());
//
//        // $top/$skip with $inlinecount case handled in response builder to avoid multiple DB call
//        if (uriParserResultView.getSkip() != null && uriParserResultView.getInlineCount() == null) {
//            pageBuilder.skip(uriParserResultView.getSkip().intValue());
//        }
//
//        if (uriParserResultView.getTop() != null && uriParserResultView.getInlineCount() == null) {
//            pageBuilder.top(uriParserResultView.getTop().intValue());
//        }
//
//        JPAPage page = pageBuilder.build();
//        oDataJPAContext.setPaging(page);
//
//        return page.getPagedEntities();
//    }
//
//    /** Classe privata copiata dalla classe JPAProcessorImpl in quanto privata
//     *
//     * @param query
//     * @param uriParserResultView
//     * @return
//     */
//    @Override
//    protected List<Object> handlePaging(final Query query, final GetEntitySetUriInfo uriParserResultView) {
//
//        JPAPage.JPAPageBuilder pageBuilder = new JPAPage.JPAPageBuilder();
//        pageBuilder.pageSize(oDataJPAContext.getPageSize())
//                .query(query)
//                .skipToken(uriParserResultView.getSkipToken());
//
//        // $top/$skip with $inlinecount case handled in response builder to avoid multiple DB call
////        if (uriParserResultView.getSkip() != null && uriParserResultView.getInlineCount() == null) {
////            pageBuilder.skip(uriParserResultView.getSkip().intValue());
////        }
////
////        if (uriParserResultView.getTop() != null && uriParserResultView.getInlineCount() == null) {
////            pageBuilder.top(uriParserResultView.getTop().intValue());
////        }
//
//
//        if (uriParserResultView.getSkip() != null ) {
//            pageBuilder.skip(uriParserResultView.getSkip().intValue());
//        } else
//            pageBuilder.skip(0);
//
//        if (uriParserResultView.getTop() != null) {
//            pageBuilder.top(uriParserResultView.getTop().intValue());
//        }
//
//        JPAPage page = pageBuilder.build();
//        oDataJPAContext.setPaging(page);
//
//        return page.getPagedEntities();
//
//    }




//
//    /**Process chiamato durante le choiamate semplici odata ex Utentes(12012)
//     *
//     * @param uriParserResultView
//     * @param <T>
//     * @return
//     * @throws ODataJPAModelException
//     * @throws ODataJPARuntimeException
//     */
//    @Override
//    public <T> Object process(GetEntityUriInfo uriParserResultView)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//        CustomJPAQueryBuilder jpaQueryBuilder=getCustomJpaQueryBuilder(oDataJPAContext);
//        return readEntity(jpaQueryBuilder.build(uriParserResultView));
//    }
//
//    private Object readEntity(final Query query) throws ODataJPARuntimeException {
//        Object selectedObject = null;
//        @SuppressWarnings("rawtypes")
//        final List resultList = query.getResultList();
//        if (!resultList.isEmpty()) {
//            selectedObject = resultList.get(0);
//        }
//        return selectedObject;
//    }
//
//    /**Override process per autowiring custom edm
//     *
//     * @param uriParserResultView
//     * @return
//     * @throws ODataJPAModelException
//     * @throws ODataJPARuntimeException
//     */
//    @Override
//    public List<Object> process(final GetFunctionImportUriInfo uriParserResultView)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//
//        JPAMethodContext jpaMethodContext = JPAMethodContext.createBuilder(JPQLContextType.FUNCTION, uriParserResultView).build();
//        //todo il true e di verifica
////        if (true || jpaMethodContext.getEnclosingObject().getClass().isAssignableFrom(WithODataJPAContext.class))
////            ((WithODataJPAContext)jpaMethodContext.getEnclosingObject()).setoDataJPAContext(oDataJPAContext);
//        AutowireCapableBeanFactory autowireCapableBeanFactory=applicationContext.getAutowireCapableBeanFactory();
//        autowireCapableBeanFactory.autowireBeanProperties(jpaMethodContext.getEnclosingObject(),AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,false);
//
//        List<Object> resultObj = null;
//
//        try {
//
//            JPAFunction jpaFunction = jpaMethodContext.getJPAFunctionList()
//                    .get(0);
//            Method method = jpaFunction.getFunction();
//            Object[] args = jpaFunction.getArguments();
//            if (uriParserResultView.getFunctionImport().getReturnType()
//                    .getMultiplicity().equals(EdmMultiplicity.MANY)) {
//
//                resultObj = (List<Object>) method.invoke(jpaMethodContext.getEnclosingObject(), args);
//            } else {
//                resultObj = new ArrayList<Object>();
//                Object result = method.invoke(
//                        jpaMethodContext.getEnclosingObject(), args);
//                resultObj.add(result);
//            }
//
//        } catch (EdmException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getMessage()), e);
//        } catch (IllegalAccessException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getMessage()), e);
//        } catch (IllegalArgumentException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getMessage()), e);
//        } catch (InvocationTargetException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getTargetException().getMessage()), e.getTargetException());
//        }
//
//        return resultObj;
//    }
//
//    // ============================= UPDATE ====================================
//
//
//    @Override
//    public Object process(final PutMergePatchUriInfo updateView,
//                          final InputStream content, final String requestContentType)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//        return processUpdate(updateView, content, null, requestContentType);
//    }
//
//    /** Sovrascrittura del metodo per dare un custom
//     *
//     *
//     * @param updateView
//     * @param content
//     * @param properties
//     * @param requestContentType
//     * @param <T>
//     * @return
//     * @throws ODataJPAModelException
//     * @throws ODataJPARuntimeException
//     */
//    private <T> Object processUpdate(PutMergePatchUriInfo updateView,
//                                     final InputStream content, final Map<String, Object> properties, final String requestContentType)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//        Object jpaEntity = null;
//        try {
//
//            boolean isLocalTransaction = setTransaction();
//
//            jpaEntity = readEntity(getCustomJpaQueryBuilder(oDataJPAContext).build(updateView));
//
//            if (jpaEntity == null) {
//                throw ODataJPARuntimeException
//                        .throwException(ODataJPARuntimeException.RESOURCE_NOT_FOUND, null);
//            }
//
//            final EdmEntitySet oDataEntitySet = updateView.getTargetEntitySet();
//            final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();
//            final JPAEntity virtualJPAEntity = new JPAEntity(oDataEntityType, oDataEntitySet, oDataJPAContext);
//            virtualJPAEntity.setJPAEntity(jpaEntity);
//            if (content != null) {
//                final ODataEntityParser oDataEntityParser = new ODataEntityParser(oDataJPAContext);
//                ODataEntry oDataEntry;
//                oDataEntry = oDataEntityParser.parseEntry(oDataEntitySet, content, requestContentType, false);
//                virtualJPAEntity.update(oDataEntry);
//            } else if (properties != null) {
//                virtualJPAEntity.update(properties);
//            } else {
//                return null;
//            }
//            em.flush();
//            if (isLocalTransaction) {
//                oDataJPAContext.getODataJPATransaction().commit();
//            }
//
//        } catch (ODataBadRequestException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        } catch (EdmException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        }
//        return jpaEntity;
//    }
//
//
//
//
//// =========================== CREATE =========================================
//
//    @Override
//    public Object process(final PostUriInfo createView, final InputStream content,
//                          final String requestedContentType) throws ODataJPAModelException,
//            ODataJPARuntimeException {
//        return processCreate(createView, content, null, requestedContentType);
//    }
//
//
//    private Object processCreate(final PostUriInfo createView, final InputStream content,
//                                 final Map<String, Object> properties,
//                                 final String requestedContentType) throws ODataJPAModelException,
//            ODataJPARuntimeException {
//        try {
//            final EdmEntitySet oDataEntitySet = createView.getTargetEntitySet();
//            final EdmEntityType oDataEntityType = oDataEntitySet.getEntityType();
//            final JPAEntity virtualJPAEntity = new JPAEntity(oDataEntityType, oDataEntitySet, oDataJPAContext);
//            Object jpaEntity = null;
//
//            if (content != null) {
//                final ODataEntityParser oDataEntityParser = new ODataEntityParser(oDataJPAContext);
//                final ODataEntry oDataEntry =
//                        oDataEntityParser.parseEntry(oDataEntitySet, content, requestedContentType, false);
//                virtualJPAEntity.create(oDataEntry);
//            } else if (properties != null) {
//                virtualJPAEntity.create(properties);
//            } else {
//                return null;
//            }
//
//            boolean isLocalTransaction = setTransaction();
//            jpaEntity = virtualJPAEntity.getJPAEntity();
//
//            em.persist(jpaEntity);
//            if (em.contains(jpaEntity)) {
//                if (isLocalTransaction) {
//                    oDataJPAContext.getODataJPATransaction().commit();
//                }
//                return jpaEntity;
//            }
//            return jpaEntity;
//        } catch (ODataBadRequestException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        } catch (EdmException e) {
//            throw ODataJPARuntimeException.throwException(
//                    ODataJPARuntimeException.ERROR_JPQL_QUERY_CREATE, e);
//        }
//        // return null;
//    }
//
//// =============================== DELETE ==========================================================
//
//    @Override
//    public Object process(DeleteUriInfo uriParserResultView, final String contentType)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//        if (uriParserResultView instanceof DeleteUriInfo) {
//            if (((UriInfo) uriParserResultView).isLinks()) {
//                return deleteLink(uriParserResultView);
//            }
//        }
//
//        boolean isLocalTransaction = setTransaction();
//        Object selectedObject = readEntity(getCustomJpaQueryBuilder(oDataJPAContext).build(uriParserResultView));
//        if (selectedObject != null) {
//            removeEntity(selectedObject);
//            //em.remove(selectedObject);
//            //em.flush();
//        }
//        if (isLocalTransaction) {
//            commitTransaction();
//            //oDataJPAContext.getODataJPATransaction().commit();
//        }
//        return selectedObject;
//    }
//
//    // todo da capire a cosa serve e cosa sono i link
//    protected Object deleteLink(final DeleteUriInfo uriParserResultView) throws ODataJPARuntimeException {
//        JPALink link = new JPALink(oDataJPAContext);
//        link.delete(uriParserResultView);
//        link.save();
//        return link.getTargetJPAEntity();
//    }


    /**
     * Metodo per cancellare l'entity sull'entity manager
     *
     * @param entity
     */
    public void removeEntity(Object entity){
        em.remove(entity);
    }

    /**
     * Metodo da chiamare per persistere l'entità sull'entity manager
     *
     * @param entity
     */
    public void persistEntity(Object entity){
        em.persist(entity);
    }

    /**
     * Metodo da richiamare per fare flush e committare la transazione nel caso sia aperta
     */
    public void commitTransaction(){
        em.flush();
        ODataJPATransaction transaction = oDataJPAContext.getODataJPATransaction();
        if (transaction.isActive()) {
            oDataJPAContext.getODataJPATransaction().commit();
        }
    }

    /**
     * Se la transazione è aperta fa il rollback
     */
    public void rollbackTransaction(){
        ODataJPATransaction transaction = oDataJPAContext.getODataJPATransaction();
        if(transaction.isActive())
            transaction.rollback();
    }

    public boolean setTransaction() {
        ODataJPATransaction transaction = oDataJPAContext.getODataJPATransaction();
        if (!transaction.isActive()) {
            transaction.begin();
            return true;
        }
        return false;
    }

//    @Override
//    public List<Object> process(final GetFunctionImportUriInfo uriParserResultView)
//            throws ODataJPAModelException, ODataJPARuntimeException {
//
//        JPAMethodContext jpaMethodContext = JPAMethodContext.createBuilder(JPQLContextType.FUNCTION, uriParserResultView).build();
//
//        List<Object> resultObj = null;
//
//        try {
//
//            JPAFunction jpaFunction = jpaMethodContext.getJPAFunctionList()
//                    .get(0);
//            Method method = jpaFunction.getFunction();
//            Object[] args = jpaFunction.getArguments();
//
//            if (uriParserResultView.getFunctionImport().getReturnType()
//                    .getMultiplicity().equals(EdmMultiplicity.MANY)) {
//
//                resultObj = (List<Object>) method.invoke(jpaMethodContext.getEnclosingObject(), args);
//            } else {
//                resultObj = new ArrayList<Object>();
//                Object result = method.invoke(
//                        jpaMethodContext.getEnclosingObject(), args);
//                resultObj.add(result);
//            }
//
//        } catch (EdmException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getMessage()), e);
//        } catch (IllegalAccessException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getMessage()), e);
//        } catch (IllegalArgumentException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getMessage()), e);
//        } catch (InvocationTargetException e) {
//            throw ODataJPARuntimeException
//                    .throwException(ODataJPARuntimeException.GENERAL
//                            .addContent(e.getTargetException().getMessage()), e.getTargetException());
//        }
//
//        return resultObj;
//    }


}

