package it.nextsw.olingo.interceptor.exception;

/**
 * Created by user on 05/07/2017.
 */
public class OlingoRequestNoInterceptorException extends RuntimeException {

    public OlingoRequestNoInterceptorException(String message) {
        super(message);
    }

    public OlingoRequestNoInterceptorException(String message, Throwable cause) {
        super(message, cause);
    }
}
