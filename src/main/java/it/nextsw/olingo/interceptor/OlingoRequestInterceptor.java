package it.nextsw.olingo.interceptor;

import it.nextsw.olingo.interceptor.bean.BinaryGrantExpansionValue;
import it.nextsw.olingo.interceptor.bean.OlingoQueryObject;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;

import java.util.List;

/**
 * Interfaccia per gli interceptor delle richieste con la libreria olingo
 * Supporta anche l'annotation {@link javax.annotation.Priority} per la priorità di esecuzione,
 * un solo handler eseguito per ogni request
 * Created by user on 30/06/2017.
 */
public interface OlingoRequestInterceptor {


    /**
     *
     * @return la classe su cui lavora l'handler
     */
    public Class<?> getReferenceEntity();



//    public List<GrantedAuthority> getRoles();


    /**
     *  Viene chiamato alla richiesta di un expansion di tipo {@link #getReferenceEntity()}
     * @param binaryGrantExpansionValues contiene una lista delle richieste di expands col nome dell'expands richiesto,
     *                                   settare true o false sul grant per concederla o meno
     */
    public void onGrantExpandsAuthorization(List<BinaryGrantExpansionValue> binaryGrantExpansionValues);


    /** Metodo che viene chiamato quando viene richiesta una update, prima che la query venga eseguita
     *  In questo metodo è possibile modificare la query jpql testuale
     *
     * @param olingoQueryObject la query jpql originale che cerca l'entità da modificare
     */
    public void onUpdateEntityQueryEdit(OlingoQueryObject olingoQueryObject);

    /** Metodo che viene chiamato quando viene richiesta l'update di un'entità di tipo {@link #getReferenceEntity()}
     *
     * @param object l'oggetto di tipo {@link #getReferenceEntity()} modificato
     * @param oDataJPAContext il contesto olingo odata della request
     * @return l'oggetto di tipo {@link #getReferenceEntity()} dopo le modifiche del metodo
     * @throws OlingoRequestRollbackException nel caso in cui si voglia chiedere la rollback
     */
    public Object onUpdateEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException;




    /** Metodo che viene chiamato quando viene richiesta una query, prima che la query venga eseguita
     *  In questo metodo è possibile modificare la query jpql testuale
     *
     * @param olingoQueryObject la query jpql originale
     */
    public void onQueryEntityQueryEdit(OlingoQueryObject olingoQueryObject);

    /**  Metodo che viene chiamato quando viene richiesta una query, dopo che la query è stata eseguita
     *
     * @param object La collection o il singolo oggetto di tipo {@link #getReferenceEntity()} risultato della query.
     * @param oDataJPAContext il contesto olingo odata della request
     * @return La collection o il singolo oggetto di tipo {@link #getReferenceEntity()} modificata dal metodo
     */
    public Object onQueryEntityPostProcess(Object object, ODataJPAContext oDataJPAContext);



    /** Metodo che viene chiamato quando viene richiesta una delete, prima che la query venga eseguita
     *  In questo metodo è possibile modificare la query jpql testuale
     *
     * @param olingoQueryObject la query jpql originale che cerca l'entità da eliminare
     */
    public void onDeleteEntityQueryEdit(OlingoQueryObject olingoQueryObject);

    /**
     * Metodo che viene chiamato quando viene richiesta la delete di un'entità di tipo {@link #getReferenceEntity()}
     *
     * @param object l'oggetto di tipo {@link #getReferenceEntity()} da cancellare
     * @param oDataJPAContext il contesto olingo odata della request
     * @throws OlingoRequestRollbackException nel caso in cui si voglia chiedere la rollback
     */
    public void onDeleteEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException;



    /** Metodo che viene chiamato quando viene richiesta la creazione di un'entità di tipo {@link #getReferenceEntity()}
     *
     * @param object l'oggetto di tipo {@link #getReferenceEntity()} creato
     * @param oDataJPAContext il contesto olingo odata della request
     * @return l'oggetto di tipo {@link #getReferenceEntity()} modificato dal metodo
     * @throws OlingoRequestRollbackException nel caso in cui si voglia chiedere la rollback
     */
    public Object onCreateEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException;




}
