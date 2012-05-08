package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.VersionableFilter;
import org.bsf.smartValueObject.Versionable;

import java.util.*;

/**
 * A replacement for <tt>java.util.Iterator</tt>.
 * <p>This iterator respects the state of versionable objects,
 * as they can be marked 'deleted' and should not be given out
 * by an interator. It is configured via an <tt>VersionableFilter</tt>.
 *
 * @see org.bsf.smartValueObject.container.SmartCollection
 * @see org.bsf.smartValueObject.container.SmartMap
 * @see org.bsf.smartValueObject.VersionableFilter
 */
public class SmartIterator implements Iterator {
    /** Marker object as placeholder. */
    private static final Object NOOBJECT = new Object();
    /** The underlying iterator. */
    private Iterator it;
    private Object next = NOOBJECT;
    /** This filter decides which objects are visible to the user. */
    private VersionableFilter filter;

    /**
     * Initializes this iterator with another iterator and
     * a <tt>VersionableFilter</tt>.
     *
     * @param it the underlying iterator.
     * @param f the filter to use while iterating.
     */
    public SmartIterator(Iterator it, VersionableFilter f) {
        this.it = it;
        this.filter = f;
    }

    public boolean hasNext() {
       while (it.hasNext()) {
           next = it.next();
           if (next instanceof Versionable) {
               if (!filter.accept((Versionable)next)) {
                   // skip filtered objects
                   continue;
               } else {
                   return true;
               }
           } else {
               // no versionable Object found
               continue;
           }
       }
       return false;
    }

    public Object next() {
        if (next == NOOBJECT) {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Object temp = next;
            next = NOOBJECT;
            return temp;

        } else {
            return next;
        }
    }

    /**
     * We dont't support this.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
