package it.nextsw.olingo.edmextension.registry;

import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che legge le annotazioni relative alle function import, classi annotate con {@link EdmFunctionImportClass}
 * e su richiesta le registra sul {@link JPAEdmSchemaView}
 *
 * Created by f.longhitano on 29/06/2017.
 */

@Service
public class EdmFunctionImportRegistry {

    private List<Class> functionImportClasses = null;

    @Autowired
    private ApplicationContext applicationContext;

    private void loadClasses() {
        functionImportClasses = new ArrayList<>();
        String classesStringImportFunction[] = applicationContext.getBeanNamesForAnnotation(EdmFunctionImportClass.class);
        for (String classString : classesStringImportFunction)
            functionImportClasses.add(applicationContext.getType(classString));
    }

    public void registerOperation(JPAEdmSchemaView view) {
        if (functionImportClasses == null)
            this.loadClasses();
        for (Class classImportFunction : functionImportClasses)
            view.registerOperations(classImportFunction, null);
    }


}
