package org.bsf.smartValueObject.mediator;

/**
 * Exceptions caused by a mediator.
 * 
 */
public class MediatorException extends Exception {
    public MediatorException() {
    }

    public MediatorException(String message) {
        super(message);
    }

    public MediatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediatorException(Throwable cause) {
        super(cause);
    }
}
