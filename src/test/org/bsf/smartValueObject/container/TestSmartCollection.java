package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.SmartCollection;
import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.container.AbstractTestSmartContainer;
import org.bsf.smartValueObject.TestVO;
import org.bsf.smartValueObject.Versionable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Testcase for SmartCollection.
 *
 * @see org.bsf.smartValueObject.container.SmartCollection
 */
public class TestSmartCollection extends AbstractTestSmartContainer {
    private SmartCollection sc;

    protected SmartContainer createContainer() {
        sc = new SmartCollection(new ArrayList(), getVersionable());
        return sc;
    }

    protected Object addToContainer(TestVO t) {
       assertTrue(sc.add(t));
       return t;
    }

    protected Object removeFromContainer(TestVO t) {
        assertTrue(sc.remove(t));
        return t;
    }

    protected TestVO getOne() {
        assertTrue("Container is empty", sc.size() > 0);
        TestVO t = (TestVO) sc.iterator().next();
        assertTrue(t != null);
        return t;
    }

    public void testSize() {
        createMany();
        assertEquals("Collection has incorrect size",
                getSize(), sc.size());
    }

    public void testContains() {
        createMany();

        for (Iterator it = iterator(); it.hasNext();) {
            Object o = it.next();
            assertTrue(sc.contains(o));
        }
    }

    public void testIterator() {
        createMany();

        Iterator it = sc.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            assertTrue(contains(o));
        }
    }

    public void testDelete() {
        TestVO alive = createOne(true);
        TestVO dead = deleteOne();

        assertTrue(alive == dead);
        assertTrue("Object is not flagged as deleted",
                dead.isDeleted());
        assertTrue("Objet is still in collection",
                !sc.contains(dead));
    }

    public void testDelete2() {
        createMany(true);

        int initialSize = getSize();
        TestVO[] deads = deleteMany();
        int n = deads.length;

        assertEquals("Collection has incorrect size afer deletion",
                initialSize - n, sc.size());
        for (int i = 0; i < deads.length; i++) {
            TestVO dead = deads[i];

            assertTrue("Object is not flagged as deleted",
                    dead.isDeleted());
            assertTrue("Object is still in collection",
                    !sc.contains(dead));
        }
    }

    public void testCreate() {
        TestVO t[] = createMany();

        for (int i = 0; i < t.length; i++) {
            TestVO testVO = t[i];
            _testCreate(testVO);
        }
    }

    private void _testCreate(TestVO t) {
        assertTrue("Object is not flagged created",
                t.isCreated());
        assertTrue("Object is not in collection",
                sc.contains(t));
    }

    public void testIsEmpty() {
        createMany();
        deleteMany(getSize());
        assertTrue("Collection is not flagged empty",
                sc.isEmpty());
    }

    public void testToArray() {
        createMany();
        deleteMany();

        Object[] oarray = sc.toArray();
        assertEquals(getSize(), oarray.length);

        for (int i = 0; i < oarray.length; i++) {
            Object o = oarray[i];
            assertTrue(o instanceof Versionable);
            // everyone's still alive ?
            assertTrue(! ((Versionable) o).isDeleted() );
        }
    }

    public void testToArray2() {
        createMany();
        deleteMany();

        Object[] oarray = new Object[getSize()];
        sc.toArray(oarray);
        assertEquals(getSize(), oarray.length);

        for (int i = 0; i < oarray.length; i++) {
            Object o = oarray[i];
            assertTrue(o instanceof Versionable);
            // everyone's still alive ?
            assertTrue(! ((Versionable) o).isDeleted() );
        }
    }

    public void testSizeAfterDelete() {
        createMany();
        deleteMany();

        testSize();
    }

    public void testIteratorAfterDelete() {
        createMany();

        TestVO dead = deleteOne();
        Iterator it = sc.iterator();
        while (it.hasNext()) {
            TestVO a = (TestVO) it.next();
            assertTrue(contains(a));
            assertTrue(a != dead);
        }
    }
}
