package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.AbstractSmartContainer;
import org.bsf.smartValueObject.Versionable;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;

/**
 *  A smart wrapper class around <tt>java.util.List</tt>.
 *
 * @see org.bsf.smartValueObject.container.SmartContainer
 * @see java.util.List
 */
public class SmartList extends AbstractSmartContainer implements List {

    private List list;

    public SmartList(List list, Versionable v) {
        super(v);
        this.list = list;
    }

    protected boolean addToContainer(Object o) {
        return list.add(o);
    }

    protected Object addToContainer(Object key, Object o) {
        throw new UnsupportedOperationException();
    }

    protected Object getFromContainer(Object key) {
        throw new UnsupportedOperationException();
    }

    protected boolean removeFromContainer(Object o) {
        return list.remove(o);
    }

    protected Object removeKeyFromContainer(Object key) {
        throw new UnsupportedOperationException();
    }

    protected boolean containerContains(Object o) {
        return list.contains(o);
    }

    protected boolean containerContainsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    protected int containerSize() {
        return list.size();
    }

    protected Iterator containerIterator() {
        return list.iterator();
    }

    protected void containerClear() {
        list.clear();
    }

    protected Object[] toObjectArray() {
        return list.toArray();
    }

    public Object getContainer() {
        return list;
    }

    //- List interface
    public boolean remove(Object o) {
        return removeObject(o);
    }

    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    public boolean addAll(Collection c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return list.addAll(index, c);
    }

    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }

    public Object get(int index) {
        return list.get(index);
    }

    public Object set(int index, Object element) {
        return list.set(index, element);
    }

    public void add(int index, Object element) {
        list.add(index, element);
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return list.listIterator();
    }

    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}
