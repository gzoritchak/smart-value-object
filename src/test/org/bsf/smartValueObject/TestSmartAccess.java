package org.bsf.smartValueObject;

import org.bsf.smartValueObject.container.SmartCollection;
import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.container.AbstractTestSmartContainer;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Testcase for SmartAccess.
 *
 * @see org.bsf.smartValueObject.SmartAccess
 */
public class TestSmartAccess extends AbstractTestSmartContainer {
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
        return (TestVO) sc.iterator().next();
    }

    public void testGetDeleted() {
        int numCreate = 20;
        int numDelete = 10;
        createMany(numCreate, true);
        TestVO[] dead = deleteMany(numDelete);

        assertEquals("deleteMany() failed",
                numDelete, dead.length);
        assertEquals("Invalid delete count",
                numDelete, sc.getDeleted());

        Iterator it = SmartAccess.deletedIterator(sc);
        int counter = 0;
        loop: while (it.hasNext()) {
            counter++;
            TestVO t = (TestVO) it.next();
            assertTrue("Object is not flagged as deleted", t.isDeleted());
            for (int i=0; i<numDelete; i++) {
                if (dead[i] == t) {
                    continue loop;
                }
            }
            fail("Not all deleted elements found");
        }
        assertEquals(numDelete, counter);
    }

    public void testGetCreated() {
        TestVO[] living1 = createMany();
        for (int i = 0; i < living1.length; i++) {
            TestVO testVO = living1[i];
            testVO.markClean();
        }
        int firstCreate = living1.length;
        int numCreate = 10;

        TestVO[] living2 = createMany(numCreate);

        assertEquals("createMany() failed",
                numCreate, living2.length);
        assertEquals("Invalid create count",
                firstCreate+numCreate, sc.getCreated());

        Iterator it = SmartAccess.createdIterator(sc);
        int counter = 0;
        loop: while (it.hasNext()) {
            counter++;
            TestVO t = (TestVO) it.next();
            assertTrue("Object is not flagged as created", t.isCreated());
            for (int i=0; i<numCreate; i++) {
                if (living2[i] == t)
                    continue loop;
            }
            fail("Not all created elements found");
        }
        assertEquals(numCreate, counter);

        // re-mark objects as created to quiet
        // down AbstractTestSmartContainer.checkIntegrity()
        for (int i = 0; i < living1.length; i++) {
            TestVO testVO = living1[i];
            testVO.create();
        }
    }

    public void testIsDirty() {
        TestVO test = new TestVO();
        test.touch();
        assertTrue("Test object is not marked dirty",
            SmartAccess.isDirty(test));
    }

    public void testIsCreated() {
        TestVO test = new TestVO();
        test.create();
        assertTrue("Test object is not marked created",
            SmartAccess.isCreated(test));
    }

    public void testIsDeleted() {
        TestVO test = new TestVO();
        test.delete();
        assertTrue("Test object is not marked deleted",
            SmartAccess.isDeleted(test));
    }

    public void testIsDependantDirtSimple() {
        TestVO test1 = new TestVO();
        TestVO test2 = new TestVO();
        test1.setOtherTestVO(test2);

        test1.markClean();
        test2.markClean();

        assertTrue("Dependent object is not marked clean",
             !SmartAccess.isGraphDirty(test1));

        test2.touch();

        assertTrue("Dependent object is not marked dirty",
             SmartAccess.isGraphDirty(test1));
    }

    public void testIsDependantDirtyGraph() {
        TestVO test1 = new TestVO();
        TestVO test2 = new TestVO();
        TestVO test3 = new TestVO();
        test1.setOtherTestVO(test2);
        test2.setOtherTestVO(test3);

        test1.markClean();
        test2.markClean();
        test3.markClean();

        assertTrue("Dependent object is not marked clean",
             !SmartAccess.isGraphDirty(test1));

        test3.touch();

        assertTrue("Dependent object is not marked dirty",
             SmartAccess.isGraphDirty(test1));
    }

    public void testIsDependantDirtyCyclic() {
        TestVO test1 = new TestVO();
        TestVO test2 = new TestVO();
        TestVO test3 = new TestVO();

        test1.setOtherTestVO(test2);
        test2.setOtherTestVO(test3);
        // create cycle
        test3.setOtherTestVO(test1);

        test1.markClean();
        test2.markClean();
        test3.markClean();

        assertTrue("Dependent object is not marked clean",
            !SmartAccess.isGraphDirty(test1));

        test3.touch();

        assertTrue("Dependent object is not marked dirty",
             SmartAccess.isGraphDirty(test1));
    }

    public void testResetAll() {
        TestVO test1 = new TestVO();
        TestVO test2 = new TestVO();
        TestVO test3 = new TestVO();

        test1.setOtherTestVO(test2);
        test2.setOtherTestVO(test3);
        // create cycle
        test3.setOtherTestVO(test1);

        SmartAccess.resetGraph(test1);
        assertTrue(!SmartAccess.isGraphDirty(test1));
    }

    public void testAfterAddRemoveShouldBeClean() {
        TestVO test1 = new TestVO();
        TestVO test2 = new TestVO();

        SmartAccess.reset(test1);
        test1.addTestVO(test2);
        test1.removeTestVO(test2);

        assertTrue("Dependent objects should be clean after add-remove",
                !SmartAccess.isGraphDirty(test1));
    }
}
