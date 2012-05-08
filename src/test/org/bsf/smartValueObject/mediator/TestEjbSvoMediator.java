package org.bsf.smartValueObject.mediator;

import junit.framework.TestCase;
import org.bsf.smartValueObject.mediator.EjbSvoMediator;
import org.bsf.smartValueObject.mediator.Mediator;
import org.bsf.smartValueObject.mediator.MediatorException;
import org.bsf.smartValueObject.TestVO;

import java.util.Properties;

/**
 * Testcase for EjbSvoMediator.
 *
 * @see org.bsf.smartValueObject.mediator.EjbSvoMediator
 */
public class TestEjbSvoMediator extends TestCase {
    private EjbSvoMediator mediator;

    public void setUp() {
        Properties props = new Properties();
        props.setProperty(Mediator.INDEXFIELD, "id");

        mediator = new EjbSvoMediator(TestVO.class, props);
        try {
            storeSampleObjects();
        } catch (MediatorException e) {
            fail("could not initialize TestCase");
        }
    }

    private void storeSampleObjects() throws MediatorException {
        TestVO test1 = new TestVO();
        test1.setId(42);
        test1.setName("Number 42");
        mediator.updateGraph(test1);

        TestVO test2 = new TestVO();
        test2.setId(23);
        test2.setName("Number 23");
        mediator.updateGraph(test2);
    }

    public void testGetGraph() throws MediatorException {
        int id = 42;
        TestVO test1 = new TestVO();
        test1.setId(id);
        TestVO test2 = (TestVO) mediator.getGraph(test1);

        assertTrue("Got no new reference", test1 != test2);
        assertEquals("Got no correct instance", id, test2.getId());
        assertEquals("Got no correcy instance", "Number 42", test2.getName());
    }

    public void testNotFound() {
        TestVO test1 = new TestVO();
        test1.setId(10);

        try {
            TestVO test2 = (TestVO) mediator.getGraph(test1);
        } catch (MediatorException e) {
            // expected
            return;
        }

        fail("Got not exception while searching non-existent object");
    }

    public void testStoreGraph() throws MediatorException {
        TestVO test1 = new TestVO();
        test1.setId(99);
        test1.setName("air balloons");
        mediator.updateGraph(test1);

        TestVO testProto = new TestVO();
        testProto.setId(99);

        TestVO result = (TestVO) mediator.getGraph(testProto);

        assertTrue(test1 != result);
        assertEquals(test1.getId(), result.getId());
        assertEquals(test1.getName(), result.getName());
    }
}
