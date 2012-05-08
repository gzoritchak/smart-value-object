package org.bsf.smartValueObject.container;

import junit.framework.TestCase;

import java.util.*;

import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.TestVO;
import org.bsf.smartValueObject.Versionable;
import org.bsf.smartValueObject.Version;
import org.bsf.smartValueObject.SmartAccess;

/**
 * Abstract Testcase for 'smart' containers. Be independent
 * from actual bytecode modification when possible.
 *
 * @see org.bsf.smartValueObject.container.SmartCollection
 * @see org.bsf.smartValueObject.container.SmartMap
 * @see org.bsf.smartValueObject.container.SmartSet
 */
public abstract class AbstractTestSmartContainer extends TestCase {
    /**
     * A container for logging purposes. The container under test
     * should be the logic equivalent.
     */
    private Collection loggingContainer;
    /**
     * The implementation we want to test.
     */
    private SmartContainer containerUnderTest;

    /**
     * Initialize containers for testing.
     */
    public void setUp() {
        loggingContainer = new ArrayList();
        containerUnderTest = createContainer();
        assertTrue(containerUnderTest != null);
    }

    /**
     * Execute <code>checkIntegrity()</code> after testing.
     */
    public void tearDown() {
        // after tests, do a quick validation if
        // container under test and verify container
        // are properly synchronized
        checkIntegrity();
    }

    /**
     * Creates the container to be tested.
     * @return the container.
     */
    protected abstract SmartContainer createContainer();

    /**
     * Adds an elements to the container under test.
     * @param t the element to be added.
     * @return
     */
    protected abstract Object addToContainer(TestVO t);

    /**
     * Removes an element from the container under test.
     * @param t the element to be removed.
     * @return
     */
    protected abstract Object removeFromContainer(TestVO t);

    /**
     * Gets a valid VO from the container.
     * @return a VO.
     */
    protected abstract TestVO getOne();

    /**
     * Gets the size of the logging container.
     * @return the number of elements in logging container.
     */
    protected int getSize() {
        return loggingContainer.size();
    }

    /**
     * Gets random number from 0 - getSize();
     * @return
     */
    protected int randomNumber() {
        return (getSize() == 0) ? 0 :
            (int) (Math.random() * getSize()) + 1;
    }

    /**
     * Tests if object is contained in logging container.
     * @param o object to check for.
     * @return
     */
    protected boolean contains(Object o) {
        return loggingContainer.contains(o);
    }

    /**
     * Gets an iterator for the logging container.
     * @return
     */
    protected Iterator iterator() {
        return loggingContainer.iterator();
    }

    /**
     * Clears the logging container.
     */
    protected void clear() {
        loggingContainer.clear();
    }

    /**
     * Obtains default <tt>Versionable</tt> implementation.
     * @return Versionable
     */
    protected Versionable getVersionable() {
        return new Version();
    }


    /**
     * Deletes one element from the containers.
     * @return the deleted object.
     */
    protected TestVO deleteOne() {
        TestVO moribund = getOne();
        _removeFromContainer(moribund);
        return moribund;
    }

    /**
     * Deletes random number of elements from the containers.
     * @return the deleted objects.
     */
    protected TestVO[] deleteMany() {
        return deleteMany(randomNumber());
    }

    /**
     * Deletes specific number of elements from the containers.
     * @param n number of elements to delete.
     * @return the deleted objects.
     */
    protected TestVO[] deleteMany(int n) {
        assertTrue("You can't delete more than " + getSize() + " elements", n<=getSize());
        assertTrue("You must delete at least 1 element", n >= 1);
        TestVO[] a = new TestVO[n];
        for (int i = 0; i < n; i++) {
            TestVO t = getOne();
            _removeFromContainer(t);
            a[i] = t;
        }
        return a;
    }

    protected TestVO createOne() {
        return createOne(false);
    }

    protected TestVO[] createMany() {
        return createMany(false);
    }

    protected TestVO[] createMany(int n) {
        return createMany(n, false);
    }

    /**
     * Creates a VO and add it to the containers.
     * @return the created object.
     */
    protected TestVO createOne(boolean clean) {
        TestVO t = new TestVO();
        int id = (int) (Math.random() * Integer.MAX_VALUE);
        t.setName("New Object no. " + id);
        t.setId(id);

        if (clean) {
            t.markClean();
        }
        _addToContainer(t);

        return t;
    }

    /**
     * Creates a random number of VOs and adds them to the containers.
     * @return the created objects.
     */
    protected TestVO[] createMany(boolean clean) {
        return createMany((int) (Math.random() * 94) + 5, clean);
    }

    /**
     * Creates a specific number of VOs and adds them to the containers.
     * @param n the number of objects to create.
     * @return the created objects.
     */
    protected TestVO[] createMany(int n, boolean clean) {
        assertTrue("Use sensible parameters", n > 0 && n < 100);
        TestVO[] a = new TestVO[n];
        for (int i=0; i<n; i++) {
            a[i] = createOne(clean);
        }
        return a;
    }

    /**
     * Private helper method to add an element to both containers
     * (logging / test)
     * @param t
     */
    private void _addToContainer(TestVO t) {
        loggingContainer.add(t);
        addToContainer(t);
    }

    /**
     * Private helper method to remove an element from both containers
     * (logging / test)
     * @param t
     */
    private void _removeFromContainer(TestVO t) {
        loggingContainer.remove(t);
        removeFromContainer(t);
    }

    /**
     * Perform integrity checks to make sure the tests are left
     * in a valid state. The logging container should be
     * synchronized with the container under test, if tests
     * have been executed correctly.
     */
    private void checkIntegrity() {
        assertEquals("Container size mismatch",
                loggingContainer.size(), containerUnderTest.size());

        int deleted = containerUnderTest.getDeleted();
        int created = containerUnderTest.getCreated();

        Iterator it = SmartAccess.createdIterator(containerUnderTest);
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            // loggingContainer should have it, too
            assertTrue("Added object not found in logging container",
                    contains(it.next()));
        }
        assertEquals("Created objects count mismatch", created, counter);

        it = SmartAccess.deletedIterator(containerUnderTest);
        counter = 0;
        while (it.hasNext()) {
            counter++;
            // loggingContainer shouldn't have it
            assertTrue("Deleted object was found in logging container",
                    !contains(it.next()));
        }
        assertEquals("Deleted objects count mismatch", deleted, counter);
    }

    /**
     * Helper method, checks for presence of an element in a given array.
     * @param test element to search.
     * @param testarray the array to be searched.
     * @return
     */
    protected boolean isInArray(TestVO test, TestVO[] testarray) {
        for (int i = 0; i < testarray.length; i++) {
            TestVO testVO = testarray[i];
            if (test == testVO)
                return true;
        }
        return false;
    }

    public void testAddandRemove() {
        createMany(true);
        TestVO[] deleted = deleteMany();
        for (int i = 0; i < deleted.length; i++) {
            TestVO testVO = deleted[i];
            assertTrue(testVO.isDeleted());
        }
    }
}
