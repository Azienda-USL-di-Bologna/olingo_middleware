package it.nextsw.olingo.interceptor;

import it.nextsw.olingo.interceptor.bean.OlingoQueryObject;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


/**
 * Created by f.longhitano on 30/06/2017.
 */
@Component
public abstract class OlingoRequestInterceptorImpl implements OlingoRequestInterceptor {

    private static Logger logger=Logger.getLogger(OlingoRequestInterceptorImpl.class);


    @Override
    public void onUpdateEntityQueryEdit(OlingoQueryObject olingoQueryObject) {

    }


    @Override
    public Object onUpdateEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
        return object;
    }

    @Override
    public void onQueryEntityQueryEdit(OlingoQueryObject olingoQueryObject) {

    }


    @Override
    public Object onQueryEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) {
        return object;
    }

    @Override
    public void onDeleteEntityQueryEdit(OlingoQueryObject olingoQueryObject) {

    }


    @Override
    public void onDeleteEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {

    }


    @Override
    public Object onCreateEntityPostProcess(Object object, ODataJPAContext oDataJPAContext) throws OlingoRequestRollbackException {
        return object;
    }

    protected Authentication getAuthentication(){
        if(SecurityContextHolder.getContext().getAuthentication()!=null)
            return SecurityContextHolder.getContext().getAuthentication();
        return null;
    }



}
