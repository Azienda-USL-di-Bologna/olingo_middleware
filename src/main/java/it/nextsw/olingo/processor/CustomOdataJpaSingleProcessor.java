package it.nextsw.olingo.processor;


import it.nextsw.olingo.edmextension.EdmMediaStream;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import it.nextsw.olingo.interceptor.registry.OlingoRequestInterceptorRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.info.*;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPADefaultProcessor;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by f.longhitano on 21/06/2017.
 */
@Component
@Scope("prototype")
public class CustomOdataJpaSingleProcessor extends ODataJPADefaultProcessor {

    private static final Logger logger = Logger.getLogger(CustomOdataJpaSingleProcessor.class);


    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private OlingoRequestInterceptorRegistry olingoRequestInterceptorRegistry;


    public CustomOdataJpaSingleProcessor(ODataJPAContext oDataJPAContext, JPAProcessor jpaProcessor) {
        super(oDataJPAContext);
        this.jpaProcessor = jpaProcessor;
    }

    @Override
    public ODataResponse readEntitySet(final GetEntitySetUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            List<Object> jpaEntities = jpaProcessor.process(uriParserResultView);
            jpaEntities = (List<Object>) olingoRequestInterceptorRegistry.executePostSecurityOperation(getLoggedUtenteAuthentication(), uriParserResultView, jpaEntities, oDataJPAContext);

            oDataResponse =
                    responseBuilder.build(uriParserResultView, jpaEntities, contentType);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse readEntity(final GetEntityUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            Object jpaEntity = jpaProcessor.process(uriParserResultView);
            jpaEntity = olingoRequestInterceptorRegistry.executePostSecurityOperation(getLoggedUtenteAuthentication(), uriParserResultView, jpaEntity, oDataJPAContext);

            oDataResponse =
                    responseBuilder.build(uriParserResultView, jpaEntity, contentType);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse countEntitySet(final GetEntitySetCountUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            long jpaEntityCount = jpaProcessor.process(uriParserResultView);
            oDataResponse = responseBuilder.build(jpaEntityCount);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse existsEntity(final GetEntityCountUriInfo uriInfo, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            long jpaEntityCount = jpaProcessor.process(uriInfo);
            oDataResponse = responseBuilder.build(jpaEntityCount);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    /**
     * @param uriParserResultView
     * @param content
     * @param requestContentType
     * @param contentType
     * @return
     * @throws ODataException
     */
    @Override
    public ODataResponse createEntity(final PostUriInfo uriParserResultView, final InputStream content,
                                      final String requestContentType, final String contentType) throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            CustomJpaProcessorImpl customJpaProcessor = (CustomJpaProcessorImpl) jpaProcessor;
            boolean isLocalTransaction = customJpaProcessor.setTransaction();
            Object createdJpaEntity = jpaProcessor.process(uriParserResultView, content, requestContentType);
            try {
                createdJpaEntity = olingoRequestInterceptorRegistry.executePostSecurityOperation(getLoggedUtenteAuthentication(), uriParserResultView, createdJpaEntity, oDataJPAContext);
            } catch (OlingoRequestRollbackException e) {
                customJpaProcessor.rollbackTransaction();
                throw new ODataException("Exception with rollback, entity: " + createdJpaEntity, e);
            } catch (Exception e) {
                customJpaProcessor.rollbackTransaction();
                throw new ODataException(e);
            }
            if (isLocalTransaction) {
                customJpaProcessor.persistEntity(createdJpaEntity);
                customJpaProcessor.commitTransaction();
            }
            oDataResponse =
                    responseBuilder.build(uriParserResultView, createdJpaEntity, contentType);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    /**
     * Update entity con gestione transazione per rollback
     *
     * @param uriParserResultView
     * @param content
     * @param requestContentType
     * @param merge
     * @param contentType
     * @return
     * @throws ODataException
     */
    @Override
    public ODataResponse updateEntity(final PutMergePatchUriInfo uriParserResultView, final InputStream content,
                                      final String requestContentType, final boolean merge, final String contentType) throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            CustomJpaProcessorImpl customJpaProcessor = (CustomJpaProcessorImpl) jpaProcessor;
            boolean isLocalTransaction = customJpaProcessor.setTransaction();
            Object jpaEntity = jpaProcessor.process(uriParserResultView, content, requestContentType);

            jpaEntity = executeInterceptorPostUpdateOp(uriParserResultView, customJpaProcessor, jpaEntity, isLocalTransaction);

            oDataResponse = responseBuilder.build(uriParserResultView, jpaEntity);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    protected Object executeInterceptorPostUpdateOp(PutMergePatchUriInfo uriParserResultView, CustomJpaProcessorImpl customJpaProcessor, Object jpaEntity, boolean isLocalTransaction) throws ODataException {
        try {
            jpaEntity = olingoRequestInterceptorRegistry.executePostSecurityOperation(getLoggedUtenteAuthentication(), uriParserResultView, jpaEntity, oDataJPAContext);
        } catch (OlingoRequestRollbackException e) {
            customJpaProcessor.rollbackTransaction();
            throw new ODataException("Exception with rollback on update, entity: " + jpaEntity, e);
        } catch (Exception e) {
            customJpaProcessor.rollbackTransaction();
            throw new ODataException(e);
        }
        if (isLocalTransaction)
            customJpaProcessor.commitTransaction();
        return jpaEntity;
    }

    /**
     * Delete entity con gestione transazione per rollback
     *
     * @param uriParserResultView
     * @param contentType
     * @return
     * @throws ODataException
     */
    @Override
    public ODataResponse deleteEntity(final DeleteUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            CustomJpaProcessorImpl customJpaProcessor = (CustomJpaProcessorImpl) jpaProcessor;
            boolean isLocalTransaction = customJpaProcessor.setTransaction();

            Object deletedObj = jpaProcessor.process(uriParserResultView, contentType);
            try {
                deletedObj = olingoRequestInterceptorRegistry.executePostSecurityOperation(getLoggedUtenteAuthentication(), uriParserResultView, deletedObj, oDataJPAContext);
            } catch (OlingoRequestRollbackException e) {
                customJpaProcessor.rollbackTransaction();
                throw new ODataException("Exception with rollback on delete, entity: " + deletedObj, e);
            } catch (Exception e) {
                customJpaProcessor.rollbackTransaction();
                throw new ODataException(e);
            }
            if (isLocalTransaction) {
                customJpaProcessor.removeEntity(deletedObj);
                customJpaProcessor.commitTransaction();
            }

            oDataResponse = responseBuilder.build(uriParserResultView, deletedObj);
        }
        catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        }
        finally{
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse executeFunctionImport(final GetFunctionImportUriInfo uriParserResultView,
                                               final String contentType) throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            List<Object> resultEntity = jpaProcessor.process(uriParserResultView);
            oDataResponse =
                    responseBuilder.build(uriParserResultView, resultEntity, contentType);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse executeFunctionImportValue(final GetFunctionImportUriInfo uriParserResultView,
                                                    final String contentType) throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            List<Object> result = jpaProcessor.process(uriParserResultView);
            oDataResponse =
                    responseBuilder.build(uriParserResultView, result.get(0));
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse readEntityLink(final GetEntityLinkUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            Object jpaEntity = jpaProcessor.process(uriParserResultView);
            oDataResponse =
                    responseBuilder.build(uriParserResultView, jpaEntity, contentType);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse readEntityLinks(final GetEntitySetLinksUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        ODataResponse oDataResponse = null;
        try {
            oDataJPAContext.setODataContext(getContext());
            List<Object> jpaEntity = jpaProcessor.process(uriParserResultView);
            oDataResponse =
                    responseBuilder.build(uriParserResultView, jpaEntity, contentType);
        }catch (Exception e){
            logger.error("Error JpaSingleProcessor ",e);
        } finally {
            close();
        }
        return oDataResponse;
    }

    @Override
    public ODataResponse createEntityLink(final PostUriInfo uriParserResultView, final InputStream content,
                                          final String requestContentType, final String contentType) throws ODataException {
        try {
            oDataJPAContext.setODataContext(getContext());
            jpaProcessor.process(uriParserResultView, content, requestContentType, contentType);
            return ODataResponse.newBuilder().build();
        } finally {
            close();
        }
    }

    @Override
    public ODataResponse updateEntityLink(final PutMergePatchUriInfo uriParserResultView, final InputStream content,
                                          final String requestContentType, final String contentType) throws ODataException {
        try {
            oDataJPAContext.setODataContext(getContext());
            jpaProcessor.process(uriParserResultView, content, requestContentType, contentType);
            return ODataResponse.newBuilder().build();
        } finally {
            close();
        }
    }

    @Override
    public ODataResponse deleteEntityLink(final DeleteUriInfo uriParserResultView, final String contentType)
            throws ODataException {
        try {
            oDataJPAContext.setODataContext(getContext());
            jpaProcessor.process(uriParserResultView, contentType);
            return ODataResponse.newBuilder().build();
        } finally {
            close();
        }
    }

    @Override
    public ODataResponse executeBatch(final BatchHandler handler, final String contentType, final InputStream content)
            throws ODataException {
        try {
            oDataJPAContext.setODataContext(getContext());

            ODataResponse batchResponse;
            List<BatchResponsePart> batchResponseParts = new ArrayList<BatchResponsePart>();
            PathInfo pathInfo = getContext().getPathInfo();
            EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
            List<BatchRequestPart> batchParts = EntityProvider.parseBatchRequest(contentType, content, batchProperties);

            for (BatchRequestPart batchPart : batchParts) {
                batchResponseParts.add(handler.handleBatchPart(batchPart));
            }
            batchResponse = EntityProvider.writeBatchResponse(batchResponseParts);
            return batchResponse;
        } finally {
            close(true);
        }
    }

    @Override
    public BatchResponsePart executeChangeSet(final BatchHandler handler, final List<ODataRequest> requests)
            throws ODataException {
        List<ODataResponse> responses = new ArrayList<ODataResponse>();
        try {
            oDataJPAContext.getODataJPATransaction().begin();

            for (ODataRequest request : requests) {
                oDataJPAContext.setODataContext(getContext());
                ODataResponse response = handler.handleRequest(request);
                if (response.getStatus().getStatusCode() >= HttpStatusCodes.BAD_REQUEST.getStatusCode()) {
                    // Rollback
                    oDataJPAContext.getODataJPATransaction().rollback();
                    List<ODataResponse> errorResponses = new ArrayList<ODataResponse>(1);
                    errorResponses.add(response);
                    return BatchResponsePart.responses(errorResponses).changeSet(false).build();
                }
                responses.add(response);
            }
            oDataJPAContext.getODataJPATransaction().commit();

            return BatchResponsePart.responses(responses).changeSet(true).build();
        } catch (Exception e) {

            List<ODataResponse> errorResponses = new ArrayList<ODataResponse>(1);
            errorResponses.add(ODataResponse.entity(e).status(HttpStatusCodes.INTERNAL_SERVER_ERROR).build());
            return BatchResponsePart.responses(errorResponses).changeSet(false).build();
        } finally {
            close(true);
        }
    }

    // ================================== MEDIA STREAM ==================================================

    /**
     * metodo per la lettura degli stream,
     * per trovare l'oggetto richiama le stesse procedure di {@link #readEntity(GetEntityUriInfo, String)}
     * poi utilizza l'intefaccia {@link EdmMediaStream} per farsi dare mimeType e stream
     * <p>
     * Per quanto riguarda gli {@link it.nextsw.olingo.interceptor.OlingoRequestInterceptor} segue le stesse vie delle altre query
     *
     * @param uriInfo
     * @param contentType
     * @return
     * @throws ODataException
     */
    @Override
    public ODataResponse readEntityMedia(final GetMediaResourceUriInfo uriInfo, final String contentType)
            throws ODataException {

        //tiro giu l'entità richiama le stesse procesure di  readEntity(final GetEntityUriInfo uriParserResultView, final String contentType)
        Object jpaEntity = jpaProcessor.process((GetEntityUriInfo) uriInfo);
        if (jpaEntity == null) {
            throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
        }

        //carico lo stream e lo invio in risposta
        if (EdmMediaStream.class.isAssignableFrom(jpaEntity.getClass())) {
            EdmMediaStream edmMediaStream = (EdmMediaStream) jpaEntity;
            try {
                InputStream inputStreamMedia = edmMediaStream.getInputStream();
                ODataResponse oDataResponse = ODataResponse.fromResponse(EntityProvider.writeBinary(edmMediaStream.getMimeType(), IOUtils.toByteArray(inputStreamMedia))).build();
                IOUtils.closeQuietly(inputStreamMedia);
                return oDataResponse;
            } catch (IOException e) {
                throw new ODataException("Exception during request stream for object " + jpaEntity, e);
            }
        } else {
            throw new ODataException("Request stream of object not implements EdmMediaStreaminterface " + jpaEntity.getClass());
        }
        //throw new ODataNotImplementedException();
    }

    /**
     * todo NOT TESTED YET, non so come testarlo
     * Metodo per l'update degli stream all'interno delle entità
     * per trovare l'oggetto richiama le stesse procedure di {@link #updateEntity(PutMergePatchUriInfo, InputStream, String, boolean, String)}
     * poi utilizza l'intefaccia {@link EdmMediaStream} per chiamare {@link EdmMediaStream#updateStream(InputStream, String)}
     * <p>
     * Per quanto riguarda gli {@link it.nextsw.olingo.interceptor.OlingoRequestInterceptor} segue le stesse vie degli altri update
     *
     * @param uriInfo
     * @param content            l'input stream per l'aggiornamento
     * @param requestContentType
     * @param contentType        il content type del content (?)
     * @return
     * @throws ODataException
     */
    @Override
    public ODataResponse updateEntityMedia(final PutMergePatchUriInfo uriInfo, final InputStream content,
                                           final String requestContentType, final String contentType) throws ODataException {
        CustomJpaProcessorImpl customJpaProcessor = (CustomJpaProcessorImpl) jpaProcessor;
        boolean isLocalTransaction = customJpaProcessor.setTransaction();
        //tiro giù l'entità
        Object jpaEntity = customJpaProcessor.process(uriInfo, content, requestContentType);
        if (jpaEntity == null) {
            throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
        }

        //chiamo l'update sull'interfaccia EdmMediaStream
        if (EdmMediaStream.class.isAssignableFrom(jpaEntity.getClass())) {
            EdmMediaStream edmMediaStream = (EdmMediaStream) jpaEntity;
            try {
                edmMediaStream.updateStream(content, contentType);
            } catch (Exception e) {
                throw new ODataException("Exception during update stream " + jpaEntity, e);
            }
            jpaEntity = executeInterceptorPostUpdateOp(uriInfo, customJpaProcessor, jpaEntity, isLocalTransaction);
            return responseBuilder.build(uriInfo, jpaEntity);
        } else {
            throw new ODataException("Request stream of object not implements EdmMediaStreaminterface " + jpaEntity.getClass());
        }

        // throw new ODataNotImplementedException();
    }


    /**
     * todo metodo non ancora implementato, da capire il da farsi
     *
     * @param uriInfo
     * @param contentType
     * @return
     * @throws ODataException
     */
    @Override
    public ODataResponse deleteEntityMedia(final DeleteUriInfo uriInfo, final String contentType) throws ODataException {
        throw new ODataNotImplementedException();
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
