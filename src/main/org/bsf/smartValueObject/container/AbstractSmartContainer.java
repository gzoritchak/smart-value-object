package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.Versionable;
import org.bsf.smartValueObject.VersionableFilters;

import java.util.Iterator;

/**
 * Skeleton implementation of a versionable container. All other
 * container should extend this to avoid code duplication.
 *
 * @see org.bsf.smartValueObject.container.SmartContainer
 */
public abstract class AbstractSmartContainer implements SmartContainer, Versionable {
    /** The version instance for this container. */
    private Versionable version;
    /** Number of created objects. */
    private int created = 0;
    /** Number of deleted objects. */
    private int deleted = 0;

    /**
     * Initialize the container with a version.
     * @param v the version object to use.
     */
    public AbstractSmartContainer(Versionable v) {
        this.version = v;
    }

    /**
     * Add object to container.
     * @param o the object to be added.
     * @return
     */
    protected abstract boolean addToContainer(Object o);

    /**
     * Adds object to container with key.
     * @param key the key to use.
     * @param o the object to be added.
     * @return
     * @throws java.lang.UnsupportedOperationException if the method is not supported.
     */
    protected abstract Object addToContainer(Object key, Object o);

    /**
     * Gets object from container.
     * @param key the key of the object.
     * @return
     * @throws java.lang.UnsupportedOperationException if the method is not supported.
     */
    protected abstract Object getFromContainer(Object key);

    /**
     * Removes object from container.
     * @param o the object to be removed.
     * @return
     */
    protected abstract boolean removeFromContainer(Object o);

    /**
     * Removes key from container.
     * @param key the key to be removed.
     * @return
     * @throws java.lang.UnsupportedOperationException if the method is not supported.
     */
    protected abstract Object removeKeyFromContainer(Object key);

    /**
     * Verifies if the container has the specified object.
     * @param o
     * @return
     */
    protected abstract boolean containerContains(Object o);

    /**
     * Verifies if the container has the specified key.
     * @param key
     * @return
     */
    protected abstract boolean containerContainsKey(Object key);

    /**
     * The 'raw' size of the container.
     * @return size of the container.
     */
    protected abstract int containerSize();

    /**
     * Gets the standard iterator for this container.
     * @return iterator.
     */
    protected abstract Iterator containerIterator();

    /**
     * Deletes all elements from the container.
     */
    protected abstract void containerClear();

    /**
     * Retrieves objects as an array.
     * @return
     * @throws java.lang.UnsupportedOperationException if the method is not supported.
     */
    protected abstract Object[] toObjectArray();

    /**
     * Removes object while respecting versioning.
     * @param o the object to be removed.
     * @return
     */
    protected boolean removeObject(Object o) {
        if (o instanceof Versionable) {
            Versionable v = (Versionable) o;
            if (v.isCreated()) {
                created--;
                return removeFromContainer(o);
            }
            v.delete();
            deleted++;
            touch();
            return true;
        } else {
           return removeFromContainer(o);
        }
    }

    /**
     * Removes object by key, w/ versioning.
     * @param key
     * @return
     */
    protected Object removeObjectByKey(Object key) {
        Object o = getFromContainer(key);
        if (o == null) {
            // nop
        } else if (o instanceof Versionable) {
            Versionable v = (Versionable) o;
            if (v.isCreated()) {
                created--;
                return removeKeyFromContainer(key);
            }
            v.delete();
            deleted++;
            touch();
        } else {
            return removeKeyFromContainer(key);
        }
        return o;
    }

    /**
     * Adds object w/ versioning.
     * @param o
     * @return
     */
    protected boolean addObject(Object o) {
        if (o instanceof Versionable) {
            Versionable v = (Versionable) o;

            // v.create();
            if (v.isCreated())
                created++;

            touch();
        }
        return addToContainer(o);
    }

    /**
     * Add object via key, w/ versioning.
     * @param key
     * @param o
     * @return
     */
    protected Object addObject(Object key, Object o) {
        if (o instanceof Versionable) {
            Versionable v = (Versionable) o;
            //v.create();
            if (v.isCreated())
                created++;

            touch();
        }
        return addToContainer(key, o);
    }

    /**
     * Checks if container has specified object, respects
     * versioning.
     * @param o
     * @return
     */
    protected boolean containsObject(Object o) {
        if (containerContains(o)) {
            if (o instanceof Versionable) {
                return !((Versionable) o).isDeleted();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Implementation for Collection interface.
     * @param o
     * @return
     * @see java.util.Collection#add
     */
    public boolean add(Object o) {
        return addObject(o);
    }

    /**
     * Implementation for Collection interface.
     * @param o
     * @return
     * @see java.util.Collection#contains
     */
    public boolean contains(Object o) {
        return containsObject(o);
    }

    /**
     * Implementation for Map interface.
     * @param o
     * @return
     * @see java.util.Map#containsValue
     */
    public boolean containsValue(Object o) {
        return containsObject(o);
    }

    /**
     * Implementation for Map interface.
     * @param key
     * @return
     * @see java.util.Map#containsValue
     */
    public boolean containsKey(Object key) {
        if (!containerContainsKey(key)) {
            return false;
        }

        Object o = getFromContainer(key);
        if (o instanceof Versionable) {
            return !((Versionable) o).isDeleted();
        } else {
            return true;
        }
    }

    /**
     * Implementation for Collection interface.
     * @return
     * @see java.util.Collection#iterator
     */
    public Iterator iterator() {
        return new SmartIterator(containerIterator(),
                VersionableFilters.EXISTING);
    }

    /**
     * Implementation for Collection/Map/... interface.
     *
     * @see java.util.Collection#clear
     * @see java.util.Map#clear
     */
    public void clear() {
        Iterator it = containerIterator();
        while (it.hasNext()) {
            removeObject(it.next());
        }
    }

    /**
     * Implementaion for Collection/Map/... interface.
     * @return
     * @see java.util.Collection#isEmpty
     * @see java.util.Map#isEmpty
     */
    public boolean isEmpty() {
        return (containerSize()-getDeleted() == 0);
    }

    /**
     * Implementation for Map interface.
     * @param key
     * @param value
     * @return
     * @see java.util.Map#put
     */
    public Object put (Object key, Object value) {
        return addObject(key, value);
    }

    /**
     * Implementation for Map interface.
     * @param key
     * @return
     * @see java.util.Map#get
     */
    public Object get(Object key) {
        Object o = getFromContainer(key);
        if (o == null)
            return null;

        if (o instanceof Versionable) {
            return ((Versionable) o).isDeleted() ? null : o;
        } else {
            return o;
        }
    }

    /**
     * Implementation for Collection/... interface.
     * @return
     * @see java.util.Collection#toArray
     */
    public Object[] toArray() {
        Object[] a = toObjectArray();
        Object[] b = new Object[size()];

        for (int i = 0,j = 0; i < a.length; i++) {
            Object o = a[i];
            if (o instanceof Versionable) {
                if (((Versionable) o).isDeleted())
                    continue;
            }
            b[j++] = a[i];
        }
        return b;
    }

    /**
     * Implementation for Collection/... interface.
     * @return
     * @see java.util.Collection#toArray
     */
    public Object[] toArray(Object a[]) {
        int size = size();
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }

        System.arraycopy(toArray(), 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }


    //- SmartContainer
    public int getCreated() {
        return created;
    }

    public int getDeleted() {
        return deleted;
    }

    public Iterator getIterator() {
        return containerIterator();
    }

    public int size() {
        return containerSize()-getDeleted();
    }

    public abstract Object getContainer();

    //- Versionable
    public void touch() {
        version.touch();
    }

    public void touch(String s) {
        version.touch(s);
    }

    public boolean isDirty() {
        return version.isDirty();
    }

    public boolean isCreated() {
        return version.isCreated();
    }

    public boolean isDeleted() {
        return version.isDeleted();
    }

    public void markClean() {
        created = deleted = 0;
        version.markClean();
    }

    public long getVersionId() {
        return version.getVersionId();
    }

    public void setVersionId(long id) {
        version.setVersionId(id);
    }

    public void delete() {}
    public void create() {}
}
