package org.bsf.smartValueObject.tools;

/**
 * Exception for wrapping low-level errors caused by the bytecode
 * manipulating libraries.
 */
public class InstrumentorException extends Exception {
    public InstrumentorException() {
        super();
    }

    public InstrumentorException(String msg) {
        super(msg);
    }

    public InstrumentorException(String msg, Throwable t) {
        super(msg, t);
    }

    public InstrumentorException(Throwable t) {
        super(t);
    }
}
