package org.bsf.smartValueObject;

import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.container.SmartIterator;
import org.bsf.smartValueObject.tools.Instrumentor;

import java.util.*;
import java.lang.reflect.Field;

/**
 * Class to encapsulate implementation details and scary casts for
 * <tt>SmartContainer</tt> and <tt>Versionable</tt> objects.
 * It also allows to obtain iterators for various version states, as well
 * as methods for graph traversals.
 *
 * @see org.bsf.smartValueObject.container.SmartContainer
 * @see org.bsf.smartValueObject.Versionable
 */
public class SmartAccess {
    /**
     * Gets an iterator for newly created objects.
     * @param o an object of type <tt>SmartContainer</tt>.
     * @return  the iterator.
     * @throws IllegalArgumentException when passing an object not
     * implementing <tt>SmartContainer</tt>.
     */
    public static Iterator createdIterator(Object o) {
        checkSmartContainer(o);
        SmartContainer sc = (SmartContainer) o;
        return new SmartIterator(sc.getIterator(), VersionableFilters.CREATED);
    }

    /**
     * Gets an iterator for modified objects.
     * @param o an object of type <tt>SmartContainer</tt>.
     * @return  the iterator.
     * @throws IllegalArgumentException when passing an object not
     * implementing <tt>SmartContainer</tt>.
     */
    public static Iterator modifiedIterator(Object o) {
        checkSmartContainer(o);
        SmartContainer sc = (SmartContainer) o;
        return new SmartIterator(sc.getIterator(), VersionableFilters.DIRTY);
    }

    /**
     * Gets an iterator for deleted objects.
     * @param o an object of type <tt>SmartContainer</tt>.
     * @return  the iterator.
     * @throws IllegalArgumentException when passing an object not
     * implementing <tt>SmartContainer</tt>.
     */
    public static Iterator deletedIterator(Object o) {
        checkSmartContainer(o);
        SmartContainer sc = (SmartContainer) o;
        return new SmartIterator(sc.getIterator(), VersionableFilters.DELETED);
    }

    /**
     * Gets an iterator for all versionable objects.
     * @param o
     * @return
     */
    public static Iterator iterator(Object o) {
        checkSmartContainer(o);
        SmartContainer sc = (SmartContainer) o;
        return new SmartIterator(sc.getIterator(), VersionableFilters.ALL);
    }

    public static int createdSize(Object o) {
        checkSmartContainer(o);
        SmartContainer sc = (SmartContainer) o;
        return sc.getCreated();
    }

    public static int deletedSize(Object o) {
        checkSmartContainer(o);
        SmartContainer sc = (SmartContainer) o;
        return sc.getDeleted();
    }

    /**
     * Gets all smartcontainers in object o.
     * @param o a versionable object.
     * @return iterator with all smartcontainers.
     * @see SmartContainer
     */
    public static Iterator getSmartContainers(Object o) {
        checkVersionable(o);
        return new SmartIterator(getVersionables(o), VersionableFilters.SMARTCONTAINER);
    }

    /**
     * Gets all versionable objects contained in o (itself a versionable).
     *
     * @param o
     * @return
     */
    public static Iterator getVersionables(Object o) {
        checkVersionable(o);
        Collection c = new ArrayList();
        Field[] fields = o.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getName().equals(Instrumentor.VERSIONFIELD) ||
                field.getType().isPrimitive())
                continue;
            Object fieldObject;
            try {
                fieldObject = field.get(o);
            } catch (Exception e) {
                continue;
            }
            if (isVersionable(fieldObject)) {
                c.add(fieldObject);
            }
        }
        return c.iterator();
    }

    /**
     * Checks if the object is a smart container.
     * @param o
     * @return
     */
    public static boolean isSmartContainer(Object o) {
        return (o instanceof SmartContainer);
    }

    /**
     * Checks if the object is versionable.
     */
    public static boolean isVersionable(Object o) {
        return (o instanceof Versionable);
    }

    /**
     * Checks if object is modified.
     * @param o versionable object.
     * @return
     * @throws java.lang.IllegalArgumentException if object is not versionable.
     */
    public static boolean isDirty(Object o) {
        checkVersionable(o);
        return ((Versionable) o).isDirty();
    }

    /**
     * Checks if objects in graph o have been modified.
     *
     * @param o versionable object with versionable child objects.
     * @return
     */
    public static boolean isGraphDirty(Object o) {
        DirtyVisitor dv = new DirtyVisitor();
        traverseGraph(o, dv);

        return dv.isDirty();
    }

    /**
     * Traverses the graph using a visitor.
     * @param o
     * @param visitor
     */
     private static void traverseGraph(Object o, SmartVisitor visitor) {
        if (SmartAccess.isSmartContainer(o)) {
            traverseSmartContainer(o, visitor);
        } else if (SmartAccess.isVersionable(o)) {
            traverseVersionable(o, visitor);
        } else {
            throw new IllegalArgumentException();
        }
     }

     /**
      * Traverses the given smart container.
      * @param o
      * @param visitor
      */
     private static void traverseSmartContainer(Object o, SmartVisitor visitor) {
         checkSmartContainer(o);
         if (!visitor.visitSmartContainer(o)) return;
         for (Iterator it = iterator(o); it.hasNext();) {
             traverseGraph(it.next(), visitor);
         }
     }

     /**
      * Traverses the given versionable object.
      * @param o
      * @param visitor
      */
     private static void traverseVersionable(Object o, SmartVisitor visitor) {
        checkVersionable(o);
        if (!visitor.visitVersionable(o)) return;
        for (Iterator it = getVersionables(o); it.hasNext(); ) {
            traverseGraph(it.next(), visitor);
        }
    }

    /**
     * Checks if object was created.
     * @param o versionable object.
     * @return
     * @throws java.lang.IllegalArgumentException if object is not versionable.
     */
    public static boolean isCreated(Object o) {
        checkVersionable(o);
        return ((Versionable) o).isCreated();
    }

    /**
     * Checks if object was deleted.
     * @param o versionable object.
     * @return
     * @throws java.lang.IllegalArgumentException if object is not versionable.
     */
    public static boolean isDeleted(Object o) {
        checkVersionable(o);
        return ((Versionable) o).isDeleted();
    }

    /**
     * Resets objects version state.
     * @param o versionable object.
     * @throws java.lang.IllegalArgumentException if object is not versionable.
     */
    public static void reset(Object o) {
        checkVersionable(o);
        ((Versionable) o).markClean();
    }

    /**
     * Resets all objects on the graph,including the root object.
     * @param o
     */
    public static void resetGraph(Object o) {
        checkVersionable(o);
        traverseGraph(o, new ResetVisitor());
    }

    /**
     * Checks if object o1, o2 have the same version
     * number.
     *
     * @param o1 versionable object.
     * @param o2 versionable object.
     * @return
     */
    public static boolean sameVersion(Object o1, Object o2) {
        checkVersionable(o1);
        checkVersionable(o2);
        return ((Versionable) o1).getVersionId() ==
               ((Versionable) o2).getVersionId();
    }

    /**
     * Gets version id from object.
     * @param o
     * @return
     */
    public static long getVersionId(Object o) {
        checkVersionable(o);
        return ((Versionable) o).getVersionId();
    }

    /**
     * Sets version id.
     * @param o
     * @param id
     */
    public static void setVersionId(Object o, long id) {
        checkVersionable(o);
        ((Versionable) o).setVersionId(id);
    }

    /**
     * Interface for visiting graphs.
     *
     * @see #traverseGraph
     */
    private static interface SmartVisitor {
        /** Visits a versionable object */
        boolean visitVersionable(Object o);
         /** Visits a smart container */
        boolean visitSmartContainer(Object o);
        /** To obtain a return value after traversal. */
        Object visitorResponse();
    }

    /**
     * A visitor which searches the graph for dirty objects.
     */
    private static class DirtyVisitor implements SmartVisitor {
        private Map map = new HashMap();
        private boolean dirty = false;

        public boolean visitVersionable(Object o) {
            if (!continueVisiting(o))
                return false;

            //log("visitVersionable" + o);
            markVisited(o);
            dirty = SmartAccess.isDirty(o);
            return dirty == false;
        }

        public boolean visitSmartContainer(Object o) {
            if (!continueVisiting(o))
                return false;

            //log("visitSmartContainer " + o);
            markVisited(o);
            return dirty == false;
        }

        public Object visitorResponse() {
            return new Boolean(dirty);
        }

        public boolean isDirty() {
            return dirty;
        }

        private boolean continueVisiting(Object o) {
            if (dirty)
                return false;
            else
                return !map.containsKey(o);
        }

        private void markVisited(Object o) {
            map.put(o, o);
        }
    }

    /**
     * A visitor which resets all versionable objects.
     */
    private static class ResetVisitor implements SmartVisitor {
        private Map map = new HashMap();

        public boolean visitVersionable(Object o) {
            if (!continueVisiting(o))
                return false;

            markVisited(o);
            reset(o);
            return true;
        }

        public boolean visitSmartContainer(Object o) {
            if (!continueVisiting(o))
                return false;

            markVisited(o);
            reset(o);
            return true;
        }

        public Object visitorResponse() {
            return null;
        }

        private boolean continueVisiting(Object o) {
            return !map.containsKey(o);
        }

        private void markVisited(Object o) {
            map.put(o, o);
        }
    }

    private static void log(String s) {
        System.out.println(s);
    }

    /**
     * Check if object is versionable, else throw IllegalArgumentException
     * @param o
     */
    private static void checkVersionable(Object o) {
        if (!isVersionable(o))
            throw new IllegalArgumentException("passed non-versionable object");
    }

    /**
     * Check if object is smartcontainer, else throw IllegalArgumentException
     * @param o
     */
    private static void checkSmartContainer(Object o) {
        if (!isSmartContainer(o)) {
            throw new IllegalArgumentException("passed non-smartcontainer object");
        }
    }
}
