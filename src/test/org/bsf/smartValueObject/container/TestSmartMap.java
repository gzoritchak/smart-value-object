package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.SmartMap;
import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.container.AbstractTestSmartContainer;
import org.bsf.smartValueObject.TestVO;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Testcase for SmartMap.
 *
 * @see org.bsf.smartValueObject.container.SmartMap
 */
public class TestSmartMap extends AbstractTestSmartContainer {
    private SmartMap sm;

    protected SmartContainer createContainer() {
        sm = new SmartMap(new HashMap(), getVersionable());
        return sm;
    }

    protected Object addToContainer(TestVO t) {
        return sm.put(t, t);
    }

    protected Object removeFromContainer(TestVO t) {
        return sm.remove(t);
    }

    protected TestVO getOne() {
        assertTrue("Container is empty", sm.size() > 0);
        TestVO t = (TestVO) sm.values().iterator().next();
        assertTrue(t != null);
        return t;
    }

    public void testSize() {
        createMany();
        assertEquals("Incorrect size of map", getSize(), sm.size());
    }

    public void testSizeAfterDelete() {
        createMany();
        deleteMany();
        testSize();
    }

    public void testContainsValue() {
        createMany();
        for (Iterator it = iterator(); it.hasNext();) {
            assertTrue("Map doesn't contain all values",
                    sm.containsValue(it.next()));
        }
    }

    public void testContainsValueAfterDelete() {
        createMany();
        TestVO[] deleted = deleteMany();
        testContainsValue();

        for (Iterator it = iterator(); it.hasNext();) {
            TestVO test = (TestVO) it.next();

            assertTrue("Container has still deleted elements",
                !isInArray(test, deleted));
        }
    }

    public void testContainsKey() {
        for (Iterator it = iterator(); it.hasNext();) {
           TestVO t = (TestVO) it.next();
           assertTrue("Map doesn't contain all keys",
                sm.containsKey(t));
        }
    }

    public void testContainsKeyAfterDelete() {
        createMany();
        deleteMany();
        testContainsKey();
    }

    public void testIsEmpty() {
        createMany(true);  // TODO
        sm.clear();
        clear();
        assertTrue("Map in not empty", sm.isEmpty());
    }

    public void testGet() {
        createMany();
        for (Iterator it = iterator(); it.hasNext();) {
          TestVO t = (TestVO) it.next();
          TestVO t_ = (TestVO) sm.get(t);
          assertTrue("Failed to find entry in map", t_ != null);
          assertTrue("Incorrect identity in map", t == t_);
        }
    }

    public void testPut() {
        TestVO t = createOne();
        TestVO t_ = (TestVO) sm.get(t);
        assertTrue("Failed to put entry in map",
                t == t_);
        assertTrue("Object not flagged as created", t_.isCreated());
    }

    public void testRemove() {
        createOne(true);
        TestVO t = deleteOne();
        Object o = sm.get(t);
        assertTrue("Failed to remove entry",
                o == null);
        assertTrue("Object not flagged as deleted", t.isDeleted());
    }

    public void testKeySet() {
        createMany();
        Set keys = sm.keySet();
        assertTrue("Number of keys doesn't match",
                keys.size() == getSize());
        for (Iterator it = iterator(); it.hasNext();) {
            TestVO t = (TestVO) it.next();
            assertTrue("Key not found in keySet",
                keys.contains(t));
        }
    }

    public void testValues() {
        createMany();
        for (Iterator it = sm.values().iterator(); it.hasNext();) {
            assertTrue(contains(it.next()));
        }
    }
}
