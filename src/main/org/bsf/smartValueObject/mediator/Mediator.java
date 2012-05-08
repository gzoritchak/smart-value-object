package org.bsf.smartValueObject.mediator;

/**
 * The mediator interface.
 * 
 */
public interface Mediator {
    String INDEXFIELD = "indexfield";

    /**
     * Retrieves a graph based on given prototype.
     *
     * @param prototype a prototype to obtain a initialized graph.
     * @return
     * @throws MediatorException
     */
    Object getGraph(Object prototype)
        throws MediatorException;

    /**
     * Stores a graph.
     *
     * @param graph the root element of the graph.
     * @throws MediatorException
     * @returns
     */
    ChangeSummary updateGraph(Object graph)
        throws MediatorException;

    /**
     * Deletes a graph.
     * @param graph the root element of the graph.
     * @throws MediatorException
     */
    void deleteGraph(Object graph)
        throws MediatorException;
}
