package it.nextsw.olingo.interceptor.registry;


import it.nextsw.olingo.interceptor.OlingoInterceptorOperation;
import it.nextsw.olingo.interceptor.OlingoRequestInterceptor;
import it.nextsw.olingo.interceptor.bean.BinaryGrantExpansionValue;
import it.nextsw.olingo.interceptor.bean.OlingoQueryObject;
import it.nextsw.olingo.interceptor.exception.OlingoRequestNoInterceptorException;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.core.uri.UriInfoImpl;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import java.util.*;


/**
 * La classe si occupa di gestire gli handler di sicurezza di olingo richiamando quello appropriato a seconda dell'entità richiesta
 *
 * Created by f.longhitano on 30/06/2017.
 */
@Service
public class OlingoRequestInterceptorRegistry {

    private static final Logger logger = Logger.getLogger(OlingoRequestInterceptorRegistry.class.getSimpleName());
    protected static Integer DEFAULT_HANDLER_PRIORITY = 0;

    /** Il booleano gestisce il caso in cui nessun handler gestisca la richiesta.
     *  se true lancia un eccezione, se false non lancia nessun eccezione e fa processare la richiesta
     *
     *  Default false
     */
    @Value("${olingo.setting.no.handler.error: false}")
    private boolean noHandlerError;

    /**
     * Il booleano gestisce l'autorizzazione di default sugli expands
     *  se true le expansion sono tutte autorizzate salvo diversa impostazione degli handler,
     *  se false le expansions non sono autorizzate salvo diversa impostazione degli handler
     *
     *  Default true
     */
    @Value("${olingo.setting.default.expands.authorization: true}")
    private boolean defaultExpandsAuthorization;

    @Autowired(required = false)
    private List<OlingoRequestInterceptor> handlers;

    @PostConstruct
    public void init(){
        logger.info("handlers trovati: " + handlers);
        //ordina handlers per priorità
        if (CollectionUtils.isNotEmpty(handlers)){
            handlers.sort((o1, o2) -> {
//                logger.info("o1: " + o1);
//                logger.info("o2: " + o2);
                if (o1 == null)
                    return 1;
                if (o2 == null)
                    return -1;
                int priority1 = o1.getClass().getAnnotation(Priority.class) != null ? o1.getClass().getAnnotation(Priority.class).value() : DEFAULT_HANDLER_PRIORITY;
                int priority2 = o2.getClass().getAnnotation(Priority.class) != null ? o1.getClass().getAnnotation(Priority.class).value() : DEFAULT_HANDLER_PRIORITY;
                return priority1 >= priority2 ? -1 : 1;
            });
        }

    }


    // ============================ PRE Operation query edit =====================================================


//    public String executePreSecurityOperation(Authentication authentication, Object uriInfo, JPQLContext jpqlContext) {
//        Class targetClass = null;
//        try {
//            targetClass= parseTargetClass(uriInfo);
//            //    targetClass = ((JPAEdmMappingImpl) ((UriInfoImpl) resultsView).getTargetEntitySet().getEntityType().getMapping()).getJPAType();
//        } catch (Exception e) {
//            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
//        }
//        if(GetEntityUriInfo.class.isAssignableFrom(uriInfo.getClass()))
//            // query single
//            return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, jpqlContext, ((GetEntityUriInfo)uriInfo).getExpand());
//        else if(GetEntitySetUriInfo.class.isAssignableFrom(uriInfo.getClass()))
//            // query collections
//            return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, jpqlContext, ((GetEntitySetUriInfo)uriInfo).getExpand());
//        else if(DeleteUriInfo.class.isAssignableFrom(uriInfo.getClass()))
//            // delete
//            return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.DELETE, authentication, targetClass, jpqlContext, null);
//        else if(PutMergePatchUriInfo.class.isAssignableFrom(uriInfo.getClass()))
//            // update
//            return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.UPDATE, authentication, targetClass, jpqlContext, null);
//        else
//            throw new IllegalStateException("Object of class "+uriInfo.getClass()+" not managed");
//
//    }

    /** Per le funzioni odata che ritonano oggetti singoli
     *
     * @param authentication
     * @param uriInfo
     * @param jpqlContext
     */
    public String executePreSecurityOperation(Authentication authentication, GetEntityUriInfo uriInfo, JPQLContext jpqlContext) {
        Class targetClass = null;
        try {
            targetClass= parseTargetClass(uriInfo);
        //    targetClass = ((JPAEdmMappingImpl) ((UriInfoImpl) resultsView).getTargetEntitySet().getEntityType().getMapping()).getJPAType();
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, jpqlContext, uriInfo.getExpand());

    }

    /** Per le funzioni odata che ritonano collections
     *
     * @param authentication
     * @param resultsView
     * @param jpqlContext
     */
    public String executePreSecurityOperation(Authentication authentication, GetEntitySetUriInfo resultsView, JPQLContext jpqlContext) {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(resultsView);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, jpqlContext, resultsView.getExpand());
    }

    /** Per le funzioni odata di delete
     *
     * @param authentication
     * @param uriInfo
     * @param jpqlContext
     * @return
     */
    public String executePreSecurityOperation(Authentication authentication, DeleteUriInfo uriInfo, JPQLContext jpqlContext) {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(uriInfo);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        //return query;
        return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.DELETE, authentication, targetClass, jpqlContext, null);
    }

    /** Update
     *
     * @param authentication
     * @param uriInfo
     * @param jpqlContext
     * @return
     */
    public String executePreSecurityOperation(Authentication authentication, PutMergePatchUriInfo uriInfo, JPQLContext jpqlContext) {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(uriInfo);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        //return query;
        return this.internalExecutePreSecurityOperation(OlingoInterceptorOperation.UPDATE, authentication, targetClass, jpqlContext, null);
    }

    /**
     * Gestisce la chiamata degli handler per le operazioni di intercettazione query e grant expands.
     * Al massimo un solo handler eseguito
     *
     *
     * @param OlingoInterceptorOperationRequest Il tipo di operazione richiesta
     * @param authentication i dati di authenticazione
     * @param targetClass la classe per decidere quale handler richiamare
     * @param jpqlContext
     * @param expands la lista di expands richieste
     * @return
     * @throws OlingoRequestNoInterceptorException se Nessun handler viene eseguito e noHandlerError è true
     */
    protected String internalExecutePreSecurityOperation(OlingoInterceptorOperation OlingoInterceptorOperationRequest, Authentication authentication,
                                                         Class<?> targetClass, JPQLContext jpqlContext, List<ArrayList<NavigationPropertySegment>> expands) throws OlingoRequestNoInterceptorException {
        OlingoQueryObject olingoQueryObject=new OlingoQueryObject(jpqlContext);
        if (CollectionUtils.isEmpty(handlers)) {
            logger.info("No OlingoRequestInterceptor declared");
        }
        if (authentication != null) {
            Map<Class, List<BinaryGrantExpansionValue>> requestExpandsAuthorization = this.createMapExpansion(expands);
            boolean executedHandler=false;
            boolean executedHandlerExpansion= MapUtils.isNotEmpty(requestExpandsAuthorization) ? false : true;
            if (CollectionUtils.isNotEmpty(handlers)){
                for (OlingoRequestInterceptor handler : handlers) {
                    //chiama handler per query
                    if (checkClassMatch(handler.getReferenceEntity(), targetClass)
                            && !executedHandler
                        //&& checkAuthenticationMatch(handler.getRoles(), authentication.getAuthorities())
                            ) {
                        //this.matchHandlerExpansAuthorization(requestExpandsAuthorization, handler.getGrantExpandsAuthorization());
                        logger.debug("Execute handler " + handler.getClass());
                        callPreSecurityOperationHandlerMethod(OlingoInterceptorOperationRequest, handler, olingoQueryObject);
                        executedHandler = true;
                    }
                    //chiama handler per expansion
                    if (requestExpandsAuthorization.containsKey(handler.getReferenceEntity())) {
                        handler.onGrantExpandsAuthorization(requestExpandsAuthorization.get(handler.getReferenceEntity()));
                        executedHandlerExpansion = true;
                    }
                }
            }
            try {
                this.finalizateExpans(requestExpandsAuthorization,expands);
            } catch (EdmException e) {
                logger.error("Exception on finalizateExpans ",e);
            }

            //controllo se sono stati eseguiti l'handler
            if(noHandlerError && (!executedHandler || !executedHandlerExpansion) )
                throw new OlingoRequestNoInterceptorException("Nessun hadler dichiarato per richiesta su class "+targetClass+", executeHandler "+executedHandler+", executeHandlerExpansion "+executedHandlerExpansion);

        } else
            throw new IllegalStateException("Authentication is null ");
        return olingoQueryObject.toJpqlString();
    }


    protected void callPreSecurityOperationHandlerMethod(OlingoInterceptorOperation OlingoInterceptorOperationRequest, OlingoRequestInterceptor handler, OlingoQueryObject olingoQueryObject ){
        switch (OlingoInterceptorOperationRequest){
            case QUERY:
                 handler.onQueryEntityQueryEdit(olingoQueryObject);
                 break;
            case UPDATE:
                 handler.onUpdateEntityQueryEdit(olingoQueryObject);
                 break;
            case DELETE:
                 handler.onDeleteEntityQueryEdit(olingoQueryObject);
                 break;
            default:
                throw new IllegalStateException("No OlingoInterceptorOperationRequest valid value "+OlingoInterceptorOperationRequest);
        }
    }



    // =============================== POST OPERATION =====================================================

//
//    public Object executePostSecurityOperation(Authentication authentication, Object uriInfo, Object resultObject, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
//        Class targetClass = null;
//        try {
//            targetClass= parseTargetClass(uriInfo);
//            //    targetClass = ((JPAEdmMappingImpl) ((UriInfoImpl) resultsView).getTargetEntitySet().getEntityType().getMapping()).getJPAType();
//        } catch (Exception e) {
//            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
//        }
//        if(uriInfo.getClass().isAssignableFrom(GetEntityUriInfo.class))
//            // query single
//            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, resultObject, oDataJPAContext);
//        else if(uriInfo.getClass().isAssignableFrom(GetEntitySetUriInfo.class))
//            // query collections
//            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, resultObject, oDataJPAContext);
//        else if(uriInfo.getClass().isAssignableFrom(DeleteUriInfo.class))
//            // delete
//            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.DELETE, authentication, targetClass, resultObject, oDataJPAContext);
//        else if(uriInfo.getClass().isAssignableFrom(PutMergePatchUriInfo.class))
//            // update
//            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.UPDATE, authentication, targetClass, resultObject, oDataJPAContext);
//        else if(uriInfo.getClass().isAssignableFrom(PostUriInfo.class))
//            // create
//            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.CREATE, authentication, targetClass, resultObject, oDataJPAContext);
//        else
//            throw new IllegalStateException("Object of class "+uriInfo.getClass()+" not managed");
//
//    }


    public Object executePostSecurityOperation(Authentication authentication, GetEntitySetUriInfo resultsView, Object resultObject, ODataJPAContext oDataJPAContext) {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(resultsView);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        try {
            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, resultObject, oDataJPAContext);
        } catch (OlingoRequestRollbackException e) {
            throw new IllegalStateException("unthrownable exception ",e);
        }
    }


    public Object executePostSecurityOperation(Authentication authentication, GetEntityUriInfo uriInfo, Object resultObject, ODataJPAContext oDataJPAContext) {
        Class targetClass = null;
        try {
            targetClass= parseTargetClass(uriInfo);
            //    targetClass = ((JPAEdmMappingImpl) ((UriInfoImpl) resultsView).getTargetEntitySet().getEntityType().getMapping()).getJPAType();
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        try {
            return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.QUERY, authentication, targetClass, resultObject, oDataJPAContext);
        } catch (OlingoRequestRollbackException e) {
            throw new IllegalStateException("unthrownable exception ",e);
        }

    }

    public Object executePostSecurityOperation(Authentication authentication, PostUriInfo uriParserResultView, Object resultObject, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(uriParserResultView);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        //return query;
        return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.CREATE, authentication, targetClass, resultObject, oDataJPAContext);
    }

    public Object executePostSecurityOperation(Authentication authentication, PutMergePatchUriInfo uriInfo, Object resultObject, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(uriInfo);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        //return query;
        return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.UPDATE, authentication, targetClass, resultObject, oDataJPAContext);
    }

    public Object executePostSecurityOperation(Authentication authentication, DeleteUriInfo uriInfo, Object resultObject, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
        Class targetClass = null;
        try {
            targetClass = parseTargetClass(uriInfo);
        } catch (Exception e) {
            throw new IllegalStateException("Illegal state on OlingoSecurityHandlerRegistry", e);
        }
        //return query;
        return this.internalExecutePostSecurityOperation(OlingoInterceptorOperation.DELETE, authentication, targetClass, resultObject, oDataJPAContext);
    }


    /**
     * Gestisce la chiamata degli handler per le operazioni di intercettazione post query sugli oggetti (o collection).
     * Al massimo un solo handler eseguito
     *
     * @param OlingoInterceptorOperationRequest
     * @param authentication
     * @param targetClass
     * @param resultObject
     * @return
     * @throws OlingoRequestRollbackException
     */
    protected Object internalExecutePostSecurityOperation(OlingoInterceptorOperation OlingoInterceptorOperationRequest, Authentication authentication, Class<?> targetClass, Object resultObject, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
        if (CollectionUtils.isEmpty(handlers)) {
            logger.info("No OlingoRequestInterceptor declared");
            return resultObject;
        }
        if (authentication != null) {
            boolean executedHandler=false;
            if(CollectionUtils.isNotEmpty(handlers)) {
                for (OlingoRequestInterceptor handler : handlers) {
                    if (checkClassMatch(handler.getReferenceEntity(), targetClass)
                            && !executedHandler
                        // && checkAuthenticationMatch(handler.getRoles(), authentication.getAuthorities())
                            ) {
                        logger.debug("Execute handler " + handler.getClass());
                        resultObject = callPostSecurityOperationHandlerMethod(OlingoInterceptorOperationRequest, handler, resultObject, oDataJPAContext);
                    }
                }
            }
            return resultObject;

        } else
            throw new IllegalStateException("Authentication is null ");
    }

    protected Object callPostSecurityOperationHandlerMethod(OlingoInterceptorOperation OlingoInterceptorOperationRequest, OlingoRequestInterceptor handler, Object result, ODataJPAContext oDataJPAContext ) throws OlingoRequestRollbackException {
        switch (OlingoInterceptorOperationRequest){
            case QUERY:
                return handler.onQueryEntityPostProcess(result, oDataJPAContext);
            case UPDATE:
                return handler.onUpdateEntityPostProcess(result, oDataJPAContext);
            case CREATE:
                return handler.onCreateEntityPostProcess(result, oDataJPAContext);
            case DELETE:
                handler.onDeleteEntityPostProcess(result, oDataJPAContext);
                return result;
            default:
                throw new IllegalStateException("No OlingoInterceptorOperationRequest valid value "+OlingoInterceptorOperationRequest);
        }
    }

    /** Il metodo si occupa di risalire alla classe jpa su cui si sta lavorando
     *
     * @param uriInfo
     * @return
     * @throws EdmException
     */
    protected Class<?> parseTargetClass(Object uriInfo) throws EdmException {
        return ((JPAEdmMappingImpl) ((UriInfoImpl) uriInfo).getTargetEntitySet().getEntityType().getMapping()).getJPAType();
    }


    protected boolean checkAuthenticationMatch(Collection<? extends GrantedAuthority> handlerDeclaredAuthorities, Collection<? extends GrantedAuthority> userDeclaredAuthorities) {
        for (GrantedAuthority handlerDeclaredAuthority : handlerDeclaredAuthorities)
            for (GrantedAuthority userDeclaredAuthority : userDeclaredAuthorities)
                if (handlerDeclaredAuthority.getAuthority().equals(userDeclaredAuthority.getAuthority()))
                    return true;
        return false;
    }

//    protected boolean checkOperationMatch(Collection<OlingoInterceptorOperation> handlerDeclaredOperations, OlingoInterceptorOperation OlingoInterceptorOperationRequest) {
//        for (OlingoInterceptorOperation handlerDeclaredOperation : handlerDeclaredOperations)
//            if (handlerDeclaredOperation.equals(OlingoInterceptorOperationRequest))
//                    return true;
//        return false;
//    }

    protected boolean checkClassMatch(Class<?> handlerDeclaredClass, Class<?> userDeclaredClass) {
        if (handlerDeclaredClass.equals(userDeclaredClass))
            return true;
        return false;
    }

    /**
     * Matcha per ogni expand richiesta le granted che espone ogni handler e fa l'or fra i due booleani,
     * PER ORA NON PIÙ USATA
     *
     * @param requestExpandsAuthorization
     * @param grantExpandsAuthorization
     */
    protected void matchHandlerExpansAuthorization(Map<String, Boolean> requestExpandsAuthorization, Map<String, Boolean> grantExpandsAuthorization) {
        for (Map.Entry entry : grantExpandsAuthorization.entrySet()) {
            if (requestExpandsAuthorization.containsKey(entry.getKey()))
                requestExpandsAuthorization.put((String) entry.getKey(), requestExpandsAuthorization.get(entry.getKey()) || grantExpandsAuthorization.get(entry.getKey()));
        }
    }

    /**
     * Crea una mappa class-bean da sottoporre agli handler per l'autorizzazione.
     * La mappa ha come chiave la classe dell'oggetto dell'expans e come valore una lista di object contenente il nome dell'expansions richieste
     * e un booleano per concedere il grant o meno
     *
     * @param expands
     * @return
     */
    protected Map<Class, List<BinaryGrantExpansionValue>> createMapExpansion(List<ArrayList<NavigationPropertySegment>> expands) {
        Map<Class, List<BinaryGrantExpansionValue>> map = new HashMap();
        if (CollectionUtils.isNotEmpty(expands))
            for (List<NavigationPropertySegment> expand : expands) {
                if (CollectionUtils.isNotEmpty(expand))
                    for (NavigationPropertySegment navigationPropertySegment : expand) {
                        try {
                            Class expansionClass=((JPAEdmMappingImpl)navigationPropertySegment.getTargetEntitySet().getEntityType().getMapping()).getJPAType();
                            List<BinaryGrantExpansionValue> binaryGrantExpansionValues=null;
                            if(map.containsKey(expansionClass)) {
                                binaryGrantExpansionValues = map.get(expansionClass);
                                //map.put(navigationPropertySegment.getNavigationProperty().getName(), DEFAULT_EXPANDS_AUTHORIZATION);
                            } else
                                binaryGrantExpansionValues = new ArrayList<>();

                            binaryGrantExpansionValues.add(new BinaryGrantExpansionValue(navigationPropertySegment.getNavigationProperty().getName(), defaultExpandsAuthorization));
                            map.put(expansionClass,binaryGrantExpansionValues);
                        } catch (EdmException e) {
                            logger.error("Error handler registry ", e);
                        }
                    }
            }

        return map;
    }

    /** Dopo le autorizzazioni concesse o non concesse dagli handlers filtra la richiesta di expands
     *
     * @param requestExpandsAuthorization
     * @param expands
     */
    protected void finalizateExpans(Map<Class, List<BinaryGrantExpansionValue>> requestExpandsAuthorization, List<ArrayList<NavigationPropertySegment>> expands) throws EdmException {
        if (CollectionUtils.isNotEmpty(expands)) {
            List<List<NavigationPropertySegment>> toRemoves = new ArrayList<>();
            for (List<NavigationPropertySegment> expand : expands) {
                if (CollectionUtils.isNotEmpty(expand))
                    for (NavigationPropertySegment navigationPropertySegment : expand) {
                        Class expansionClass = ((JPAEdmMappingImpl) navigationPropertySegment.getTargetEntitySet().getEntityType().getMapping()).getJPAType();
                        List<BinaryGrantExpansionValue> values = requestExpandsAuthorization.get(expansionClass);
                        for (BinaryGrantExpansionValue value : values) {
                            try {

                                if (value.getExpansionName().equals(navigationPropertySegment.getNavigationProperty().getName()) && !value.getExpansionGrant())
                                    toRemoves.add(expand);


                                // se non sono autorizzato
//                                if (!requestExpandsAuthorization.get(navigationPropertySegment.getNavigationProperty().getName()))
//                                    toRemoves.add(expand);
                            } catch (EdmException e) {
                                logger.error("Error handler registry ", e);
                            }
                        }
                    }

            }
            for(List<NavigationPropertySegment> toRemove: toRemoves)
                expands.remove(toRemove);
        }


    }


}
