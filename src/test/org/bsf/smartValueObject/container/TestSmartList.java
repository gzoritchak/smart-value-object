package org.bsf.smartValueObject.container;

import org.bsf.smartValueObject.container.SmartContainer;
import org.bsf.smartValueObject.container.SmartList;
import org.bsf.smartValueObject.container.AbstractTestSmartContainer;
import org.bsf.smartValueObject.TestVO;

import java.util.ArrayList;

/**
 * 
 * 
 */
public class TestSmartList extends AbstractTestSmartContainer {

    private SmartList sl;

    /**
     * Creates the container to be tested.
     * @return the container.
     */
    protected SmartContainer createContainer() {
        sl = new SmartList(new ArrayList(), getVersionable());
        return sl;
    }

    /**
     * Adds an elements to the container under test.
     * @param t the element to be added.
     * @return
     */
    protected Object addToContainer(TestVO t) {
        return sl.add(t) ? t : null;
    }

    /**
     * Removes an element from the container under test.
     * @param t the element to be removed.
     * @return
     */
    protected Object removeFromContainer(TestVO t) {
        return sl.remove(t) ? t : null;
    }

    /**
     * Gets a valid VO from the container.
     * @return a VO.
     */
    protected TestVO getOne() {
        assertTrue("Container is empty", sl.size() > 0);
        return (TestVO) sl.iterator().next();
    }

    public void testSomething() {

    }
}
