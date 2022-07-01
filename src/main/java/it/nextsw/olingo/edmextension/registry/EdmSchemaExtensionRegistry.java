package it.nextsw.olingo.edmextension.registry;

import it.nextsw.olingo.edmextension.EdmMediaStream;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.springframework.stereotype.Service;

/**
 * Classe che si occupa di estendere e modificare lo Schema di odata,
 * viene usata nel {@link org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension}
 * <p>
 * Created by f.longhitano on 24/07/2017.
 */

@Service
public class EdmSchemaExtensionRegistry {


    /**
     * Classe di ingresso per le modifiche allo Schema
     *
     * @param schema
     */
    public void updateSchema(Schema schema) {
        if (schema.getEntityTypes() != null)
            schema.getEntityTypes().stream().forEach(entityType -> {
                Class<?> jpaClass = getJpaClassOfEntity(entityType);
                this.manageStreamProperty(entityType, jpaClass);
            });
    }

    /**
     * Gestisce le proprietà dello stream odata tramite la presenza dell'interfaccia {@link EdmMediaStream}
     *
     * @param entityType
     * @param managedJpaClass
     */
    protected void manageStreamProperty(EntityType entityType, Class<?> managedJpaClass) {
        if (EdmMediaStream.class.isAssignableFrom(managedJpaClass))
            entityType.setHasStream(true);
    }


    protected Class<?> getJpaClassOfEntity(EntityType entityType) {
        if (entityType.getMapping().getClass().isAssignableFrom(JPAEdmMappingImpl.class))
            return ((JPAEdmMappingImpl) entityType.getMapping()).getJPAType();
        return null;
    }

}
