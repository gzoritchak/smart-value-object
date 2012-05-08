package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.SmartCollection;
import org.bsf.smartValueObject.container.AbstractSmartContainer;
import org.bsf.smartValueObject.Versionable;

import java.util.*;

/**
 * A smart wrapper class around <tt>java.util.Map</tt>.
 *
 * @see org.bsf.smartValueObject.container.SmartContainer
 * @see java.util.Map
 */
public class SmartMap extends AbstractSmartContainer implements Map {
    private Map map;

    public SmartMap(Map m, Versionable v) {
        super(v);
        this.map = m;
    }

    protected boolean addToContainer(Object o) {
        return map.put(o, o) == o;
    }

    protected Object addToContainer(Object key, Object o) {
        return map.put(key, o);
    }

    protected Object getFromContainer(Object key) {
        return map.get(key);
    }

    protected boolean removeFromContainer(Object o) {
        throw new UnsupportedOperationException();
    }

    protected Object removeKeyFromContainer(Object key) {
        return map.remove(key);
    }

    protected boolean containerContains(Object o) {
        return map.containsValue(o);
    }

    protected boolean containerContainsKey(Object key) {
        return map.containsKey(key);
    }

    protected int containerSize() {
        return map.size();
    }

    protected Iterator containerIterator() {
        return map.values().iterator();
    }

    protected void containerClear() {
        map.clear();
    }

    protected Object[] toObjectArray() {
        throw new UnsupportedOperationException();
    }

    public Object getContainer() {
        return map;
    }

    public Object remove(Object key) {
        return removeObjectByKey(key);
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    public Set keySet() {
        // new SmartSet() ??
        Set set = map.keySet();
        Set newset = new HashSet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object o = map.get(key);
            if (o instanceof Versionable) {
                if (((Versionable) o).isDeleted()) {
                    continue;
                }
            }
            newset.add(key);
        }

        return newset;
    }

    public Collection values() {
        // TODO this = ok ?
        return new SmartCollection(map.values(), this);
    }

    public Set entrySet() {
        return new SmartSet(map.entrySet(), this);
    }

    /**
     * Overrides baseclass with a specialized method.
     * @see org.bsf.smartValueObject.container.AbstractSmartContainer#clear
     */
    public void clear() {
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
           removeObjectByKey(it.next());
        }
    }
}
