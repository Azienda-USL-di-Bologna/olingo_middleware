package it.nextsw.olingo.edmextension.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Annotation utilizzata per identificare le classi che contengono Function Import
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface EdmFunctionImportClass {

}