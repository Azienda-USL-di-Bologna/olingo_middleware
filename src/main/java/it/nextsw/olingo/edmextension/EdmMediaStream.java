package it.nextsw.olingo.edmextension;

import java.io.InputStream;

/**
 * Interfaccia che deve essere implementata negli oggetti che vogliano dichiarare stream per lo standard odata
 *
 * Created by f.longhitano on 24/07/2017.
 */
public interface EdmMediaStream {

    /**
     *  Viene richiamato alla richiesta $value dell'entity type per generare la response
     * @return il mimeType associato allo stream
     */
    public String getMimeType();

    /**
     *  Viene richiamato alla richiesta $value dell'entity type per generare la response
     * @return lo stream dell'entity type
     */
    public InputStream getInputStream();

    /**
     * Metodo per l'update e creazione(?) dello stream
     * @param inputStream l'inpiut stream da salvare
     * @param mimeType il mimeType associato all'inputstream
     */
    public void updateStream(InputStream inputStream, String mimeType);

    /**
     * Metodo per cancellare lo stream
     * NON ANCORA IMPLEMENTATA LA GESTIONE
     */
    public void deleteStream();

}
