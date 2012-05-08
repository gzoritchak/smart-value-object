package org.bsf.smartValueObject.container;

import java.util.Iterator;

/**
 * Track changes in container objects.
 */
public interface SmartContainer {
    /**
     * Number of elements (not counting deleted ones).
     * @return number of existing elements.
     */
    int size();

    /**
     * Number of elements which have been deleted.
     * @return number of deleted elements.
     */
    int getDeleted();

    /**
     * Number of elements which have been created.
     * @return number of created elements.
     */
    int getCreated();

    /**
     * Gets the backing container containing all elements.
     * @return the underlying container.
     */
    Object getContainer();

    /**
     * Gets the backing iterator for all elements.
     * @return  the underlying iterator.
     */
    Iterator getIterator();
}
