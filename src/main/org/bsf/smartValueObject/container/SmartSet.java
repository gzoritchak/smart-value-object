package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.AbstractSmartContainer;
import org.bsf.smartValueObject.Versionable;

import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

/**
 * A smart wrapper class around <tt>java.util.Set</tt>.
 *
 * @see org.bsf.smartValueObject.container.SmartContainer
 * @see java.util.Set
 */
public class SmartSet extends AbstractSmartContainer implements Set {
    /** The underlying set. */
    private Set set;

    /**
     * Inititalize SmartCollection
     * @param s set to be wrapped.
     * @param v version object
     */
    public SmartSet(Set s, Versionable v) {
        super(v);
        this.set  = s;
    }

    protected boolean addToContainer(Object o) {
        return set.add(o);
    }

    protected Object addToContainer(Object key, Object o) {
        throw new UnsupportedOperationException();
    }

    protected Object getFromContainer(Object key) {
        throw new UnsupportedOperationException();
    }

    protected boolean removeFromContainer(Object o) {
        return set.remove(o);
    }

    protected Object removeKeyFromContainer(Object key) {
        throw new UnsupportedOperationException();
    }

    protected boolean containerContains(Object o) {
        return set.contains(o);
    }

    protected boolean containerContainsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    protected int containerSize() {
        return set.size();
    }

    protected Iterator containerIterator() {
        return set.iterator();
    }

    protected void containerClear() {
        set.clear();
    }

    protected Object[] toObjectArray() {
        return set.toArray();
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

    public boolean retainAll(Collection c) {
        return false;
    }

    public boolean removeAll(Collection c) {
        return false;
    }

    public Iterator getIterator() {
        return this.set.iterator();
    }

    public Object getContainer() {
        return set;
    }

    public String toString() {
        return "SmartSet [" + set.toString() + "]";
    }
}
