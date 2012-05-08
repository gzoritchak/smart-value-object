package org.bsf.smartValueObject.tools;

import junit.framework.TestCase;

import java.util.Collection;

import org.bsf.smartValueObject.demo.SubsidiaryVO;
import org.bsf.smartValueObject.demo.CompanyVO;
import org.bsf.smartValueObject.Versionable;

/**
 * Testcase for implementations of <code>Instrumentor</code> and
 * <code>Versionable</code>.
 *
 * <ul>
 * <li> is the generate bytecode valid ?</li>
 * <li> are the interfaces correctly implemented ?</li>
 * </ul>
 *
 * @see org.bsf.smartValueObject.tools.Instrumentor
 * @see org.bsf.smartValueObject.Versionable
 */
public class TestSmartifier extends TestCase {

    // TODO: self containedness ?
    public void setUp() {
    }

    public void testInstantiation() {
       SubsidiaryVO subVO = new SubsidiaryVO();
       CompanyVO CompanyVO = new CompanyVO();
    }

    public void testVersionable() {
       CompanyVO CompanyVO = new CompanyVO();
        if (!(CompanyVO instanceof Versionable)) {
            fail("CompanyVO doesn't implement Versionable");
        }
        SubsidiaryVO subVO = new SubsidiaryVO();
        if (!(subVO instanceof Versionable)) {
            fail("SubsidiaryVO doesn't implement Versionable");
        }
    }

    public void testDelete() {
        CompanyVO compVO = new CompanyVO();
        Versionable v = (Versionable) compVO;
        v.delete();

        assertTrue("compVO is not marked as deleted", v.isDeleted());
    }

    public void testCreate() {
        CompanyVO compVO = new CompanyVO();
        Versionable v = (Versionable) compVO;

        v.create();
        assertTrue("compVO is not marked as created", v.isCreated());
    }

    public void testDirty() {
        CompanyVO compVO = new CompanyVO();
        compVO.setName("foo");

        Versionable v = (Versionable) compVO;
        v.markClean();
        assertTrue("compVO is not clean", !v.isDirty());

        compVO.setName("foo2");
        assertTrue("compVO is not dirty", v.isDirty());

        // set the same value again, object should stay clean
        v.markClean();
        compVO.setName("foo2");
        assertTrue("compVO is not clean", !v.isDirty());

        // another value
        compVO.setName("foo3");
        assertTrue("compVO is not dirty", v.isDirty());

        // check primitives
        compVO.setId(new Long(20));
        v.markClean();
        compVO.setId(new Long(20));
        assertTrue("compVO is not clean", !v.isDirty());
    }

    public void testNull() {
        CompanyVO compVO = new CompanyVO();
        Versionable v = (Versionable) compVO;

        compVO.setName(null);
        v.markClean();
        compVO.setName(null);
        assertTrue("compVO is not clean", !v.isDirty());

        compVO.setName("test");
        assertTrue("compVO is not dirty", v.isDirty());
    }

    public void testAssignment() {
        CompanyVO compVO = new CompanyVO();
        compVO.setId(new Long(10));
        assertEquals("could not set id", new Long(10), compVO.getId());
        compVO.setName("test");
        assertEquals("could not set name", "test", compVO.getName());
    }

    public void testCollections() {
        CompanyVO compVO = new CompanyVO();
        compVO.addSubsidiary(new SubsidiaryVO());
        compVO.addSubsidiary(new SubsidiaryVO());
        compVO.addSubsidiary(new SubsidiaryVO());

        Collection c = compVO.getSubsidiaries();
        assertTrue("collection doesn't implement versionable",
                c instanceof Versionable);
    }
}
