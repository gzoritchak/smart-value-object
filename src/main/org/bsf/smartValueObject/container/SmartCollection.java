package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.Versionable;
import org.bsf.smartValueObject.Version;

import java.util.Iterator;
import java.util.Collection;

/**
 * A smart wrapper class around <tt>java.util.Collection</tt>.
 *
 * @see org.bsf.smartValueObject.container.SmartContainer
 * @see java.util.Collection
 */
public class SmartCollection extends AbstractSmartContainer
                              implements Collection {
    private Collection coll;

    public SmartCollection(Collection c, Versionable v) {
        super(v);
        coll = c;
    }

    protected boolean addToContainer(Object o) {
        return coll.add(o);
    }

    protected Object addToContainer(Object key, Object o) {
        throw new UnsupportedOperationException();
    }

    protected Object getFromContainer(Object key) {
        throw new UnsupportedOperationException();
    }

    protected boolean removeFromContainer(Object o) {
        return coll.remove(o);
    }

    protected Object removeKeyFromContainer(Object key) {
        throw new UnsupportedOperationException();
    }

    protected boolean containerContains(Object o) {
        return coll.contains(o);
    }

    protected boolean containerContainsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    protected int containerSize() {
        return coll.size();
    }

    protected Iterator containerIterator() {
        return coll.iterator();
    }

    protected void containerClear() {
        coll.clear();
    }

    protected Object[] toObjectArray() {
        return coll.toArray();
    }

    public Object getContainer() {
        return coll;
    }

    public boolean remove(Object o) {
        return removeObject(o);
    }

    public boolean containsAll(Collection c) {
        return false;
    }

    public boolean addAll(Collection c) {
        return false;
    }

    public boolean removeAll(Collection c) {
        return false;
    }

    public boolean retainAll(Collection c) {
        return false;
    }
}
