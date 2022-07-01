package it.nextsw.olingo.edmextension;


import it.nextsw.olingo.edmextension.registry.EdmComplexTypeRegistry;
import it.nextsw.olingo.edmextension.registry.EdmSchemaExtensionRegistry;
import it.nextsw.olingo.edmextension.registry.EdmFunctionImportRegistry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che implementa {@link JPAEdmExtension}, implementa i metodi dell'interfaccia per
 * gestire comportamenti customizzati, come aggiugnere tipi complessi, aggiungere function import, importare un mapping model da xml,
 * gestione degli stream associati ad una entity
 * <p>
 * Deve essere importata nella {@link ODataJPAContext} nella {@link org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory}
 */

@Component
public class CustomProcessingEdmExtension implements JPAEdmExtension {

    private static final Logger logger = Logger.getLogger(CustomProcessingEdmExtension.class);

    /**
     * Il path dell'xml contenente gli override del mapping edm
     * conforme a @see <a href="http://www.apache.org/olingo/odata2/jpa/processor/api/model/mapping/">documento</a>
     * default empty string
     */
    @Value("${olingo.setting.edm.jpa.override.path:}")
    private String edmJpaOverridePath;

    @Autowired
    private EdmFunctionImportRegistry edmFunctionImportRegistry;
    @Autowired
    private EdmComplexTypeRegistry edmComplexTypeRegistry;
    @Autowired
    private EdmSchemaExtensionRegistry edmExtendsSchemaRegistry;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {

    }

    /**
     * Utilizzato per registrare eventuali FunctionImport (servizi REST personalizzati)
     *
     * @param view
     */
    @Override
    public final void extendWithOperation(JPAEdmSchemaView view) {
        //view.registerOperations(CustomRestProcessor.class, null);
        edmFunctionImportRegistry.registerOperation(view);

        List<Class<?>> classesToRegister = registerFunctionImport();
        if (CollectionUtils.isNotEmpty(classesToRegister))
            for (Class<?> aClass : classesToRegister)
                view.registerOperations(aClass, null);
    }

    /**
     * Metodo usato per registrare import function attraverso il metodo standard e non tramite annotazione @EdmFunctionImportClass
     * fare Override di questo metodo
     *
     * @return La lista delle classi con le function import da registrare
     */
    public List<Class<?>> registerFunctionImport() {
        return null;
    }

    /**
     * Metodo che permette di abilitare/creare dei tipi complessi nonchè di estendere lo schema
     *
     * @param view
     */
    @Override
    public void extendJPAEdmSchema(final JPAEdmSchemaView view) {
        Schema edmSchema = view.getEdmSchema();

        if (edmSchema.getComplexTypes() == null)
            edmSchema.setComplexTypes(new ArrayList<>());
        // aggiorno lo schema
        edmExtendsSchemaRegistry.updateSchema(edmSchema);

        // Carico quelle con annotation
        edmComplexTypeRegistry.registerComplexType(edmSchema);

        // carico quelle inserite tramite il metodo standard
        List<ComplexType> complexTypes = registerComplexType();
        if (CollectionUtils.isNotEmpty(complexTypes))
            edmSchema.getComplexTypes().addAll(complexTypes);

    }


    /**
     * Metodo usato per registrare ComplexType Attraverso il metodo standard e non tramite annotazione @EdmComplexType
     * fare Override di questo metodo
     *
     * @return la lista di ComplexType da registrare
     */
    public List<ComplexType> registerComplexType() {
        return null;
    }


    @Override
    public InputStream getJPAEdmMappingModelStream() {
        if (StringUtils.isNotBlank(edmJpaOverridePath)) {
            try {
                return applicationContext.getClassLoader().getResourceAsStream(edmJpaOverridePath);
            } catch (Exception e) {
                logger.error("edmJpaOverridePath not found " + edmJpaOverridePath + " not loaded");
            }
        }
        return null;
    }

}
