package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.container.SmartSet;
import org.bsf.smartValueObject.container.AbstractTestSmartContainer;
import org.bsf.smartValueObject.TestVO;

import java.util.HashSet;

/**
 * Testcase for SmartSet.
 *
 * @see org.bsf.smartValueObject.container.SmartSet
 */
public class TestSmartSet extends AbstractTestSmartContainer {
    private SmartSet ss;

    protected SmartContainer createContainer() {
        ss = new SmartSet(new HashSet(), getVersionable());
        return ss;
    }

    protected Object addToContainer(TestVO t) {
        return ss.add(t) ? t : null;
    }

    protected Object removeFromContainer(TestVO t) {
        return ss.remove(t) ? t : null;
    }

    protected TestVO getOne() {
        assertTrue(getSize() > 0);
        return (TestVO) ss.iterator().next();
    }

    public void testSize() {
        createMany();
        assertEquals("Incorrect size", getSize(), ss.size());
    }

    public void testSizeAfterDelete() {
        createMany();
        deleteMany(getSize());
        testSize();
    }
}
