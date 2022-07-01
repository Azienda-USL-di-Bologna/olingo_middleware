package it.nextsw.olingo.edmextension.registry;


import it.nextsw.olingo.edmextension.annotation.EdmSimpleProperty;
import it.nextsw.olingo.edmextension.util.JavaOlingoTypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.annotation.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che legge le annotazioni relative ai completype, classi annotate con {@link EdmComplexType} e proprietà annotate con {@link EdmSimpleProperty}
 * e su richiesta le registra sul {@link JPAEdmSchemaView}
 * Created by f.longhitano on 29/06/2017.
 */

@Service
public class EdmComplexTypeRegistry {

    //private List<Class> classesImportFunction = null;
    private List<ComplexType> complexTypes = null;

    @Autowired
    private ApplicationContext applicationContext;

    private void loadClasses() {

    }

    private void createComplexType() {
        List<Class> classesImportFunction = new ArrayList<>();
        String classesStringImportFunction[] = applicationContext.getBeanNamesForAnnotation(EdmComplexType.class);
        for (String classString : classesStringImportFunction)
            classesImportFunction.add(applicationContext.getType(classString));
        complexTypes = new ArrayList<>();
        for (Class classComplex : classesImportFunction) {
            ComplexType complexType = new ComplexType();
            // nome complex type
            EdmComplexType edmComplexType = (EdmComplexType) classComplex.getAnnotation(EdmComplexType.class);
            if (StringUtils.isNotBlank(edmComplexType.name()))
                complexType.setName(edmComplexType.name());
            else
                complexType.setName(classComplex.getSimpleName());
            // simple proprieta
            List<Property> properties = new ArrayList<>();
            for (Field field : classComplex.getDeclaredFields()) {
                SimpleProperty simpleProperty = new SimpleProperty();
                EdmSimpleProperty edmProperty = field.getAnnotation(EdmSimpleProperty.class);
                simpleProperty.setName(StringUtils.isNotBlank(edmProperty.name()) ? edmProperty.name() : field.getName());
                simpleProperty.setType(!edmProperty.type().equals(EdmSimpleTypeKind.Null) ? edmProperty.type() : JavaOlingoTypeConverter.convert(field.getType()));
                properties.add(simpleProperty);
            }
            complexType.setProperties(properties);
            complexTypes.add(complexType);
        }
    }

    public void registerComplexType(Schema edmSchema) {
        if (complexTypes == null)
            this.createComplexType();
        if(edmSchema.getComplexTypes()==null)
            edmSchema.setComplexTypes(new ArrayList<>());
        edmSchema.getComplexTypes().addAll(complexTypes);
//        for(ComplexType complexType: complexTypes){
//
//        }

    }


}
