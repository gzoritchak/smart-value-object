package org.bsf.smartValueObject.demo;

import junit.framework.TestCase;
import org.bsf.smartValueObject.mediator.ConcurrencyException;
import org.bsf.smartValueObject.mediator.MediatorException;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Testcase DemoMediator.
 * 
 */
public class TestDemoMediator extends TestCase {
    private DemoMediator mediator;

    private CompanyVO company1;
    private CompanyVO company2;

    public void setUp() {
        mediator = new DemoMediator();
        company1 = new CompanyVO();
        company2 = new CompanyVO();
        company1.setName("company1");
        company2.setName("company2");
        company1.setCreationDate(new Date());
        company2.setCreationDate(new Date());

        SubsidiaryVO sub1 = new SubsidiaryVO();
        sub1.setWorkforce(1000);
        sub1.setName("sub1");

        SubsidiaryVO sub2 = new SubsidiaryVO();
        sub2.setWorkforce(2000);
        sub2.setName("sub2");

        SubsidiaryVO sub3 = new SubsidiaryVO();
        sub3.setWorkforce(3000);
        sub3.setName("sub3");

        SubsidiaryVO sub4 = new SubsidiaryVO();
        sub4.setWorkforce(4000);
        sub4.setName("sub4");

        company1.addSubsidiary(sub1);
        company1.addSubsidiary(sub2);
        company2.addSubsidiary(sub3);
        company2.addSubsidiary(sub4);
    }

    public void testUpdateAndGetGraph() throws MediatorException {
        CompanyVO result = updateCompany1();

        Collection c = result.getSubsidiaries();
        assertEquals(2, c.size());
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            SubsidiaryVO vo = (SubsidiaryVO) iterator.next();
            assertTrue(
                    vo.getName().equals("sub1") ||
                    vo.getName().equals("sub2"));
            assertTrue(
                    vo.getWorkforce() == 1000 ||
                    vo.getWorkforce() == 2000);

        }
    }

    public void testDeleted() throws MediatorException {
        CompanyVO result = updateCompany1();

        Collection c = result.getSubsidiaries();
        assertEquals(2, c.size());

        SubsidiaryVO vo = null;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            vo = (SubsidiaryVO) iterator.next();
        }

        c.remove(vo);

        mediator.updateGraph(result);

        // get new graph
        result = (CompanyVO) mediator.getGraph(getPrototype(0));
        c = result.getSubsidiaries();
        assertEquals(1, c.size());

        SubsidiaryVO vo2 = (SubsidiaryVO) c.iterator().next();
        assertTrue(
                !vo.getId().equals(vo2.getId()) &&
                !vo.getName().equals(vo2.getName()));
    }

    public void testCreated() throws MediatorException {
        CompanyVO result = updateCompany1();
        Collection c = result.getSubsidiaries();
        SubsidiaryVO sub = new SubsidiaryVO();
        sub.setName("new subsidiary");
        sub.setWorkforce(222);
        assertTrue(c.add(sub));

        mediator.updateGraph(result);

        CompanyVO result2 = (CompanyVO) mediator.getGraph(getPrototype(0));
        assertTrue(result != result2);

        c = result2.getSubsidiaries();
        assertEquals(3, c.size());
        boolean verified = false;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            SubsidiaryVO vo = (SubsidiaryVO) iterator.next();
            if (vo.getId().equals(new Long(2))) {
                assertEquals("new subsidiary", vo.getName());
                assertEquals(222, vo.getWorkforce());
                verified = true;
            }
        }
        assertTrue(verified);
    }

    public void testUpdate() throws MediatorException {
        CompanyVO company = updateCompany1();
        company.setName("new name");
        mediator.updateGraph(company);

        CompanyVO result =  (CompanyVO) mediator.getGraph(getPrototype(0));
        assertEquals("new name", result.getName());
    }


    public void testModified() throws MediatorException {
        CompanyVO result = updateCompany1();

        Collection c = result.getSubsidiaries();
        assertEquals(2, c.size());

        SubsidiaryVO vo = null;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            vo = (SubsidiaryVO) iterator.next();
        }

        vo.setName("modifiedSub");
        Long voKey = vo.getId();

        mediator.updateGraph(result);

        result = (CompanyVO) mediator.getGraph(getPrototype(0));
        boolean verified = false;
        for (Iterator iterator = result.subsidiaries(); iterator.hasNext();) {
            vo = (SubsidiaryVO) iterator.next();
            if (vo.getId().equals(voKey)) {
                assertEquals("modifiedSub", vo.getName());
                verified = true;
            }
        }
        assertTrue(verified);
    }

    public void testModified2() throws MediatorException {
        CompanyVO result = updateCompany1();
        SubsidiaryVO vo = null;
        for (Iterator iterator = result.subsidiaries(); iterator.hasNext();) {
            vo = (SubsidiaryVO) iterator.next();
            result.removeSubsidiary(vo);
        }

        System.out.println(mediator.updateGraph(result).getReport());
    }

    public void testDeleteGraph() throws MediatorException {
        CompanyVO result = updateCompany1();
        mediator.deleteGraph(result);

        try {
            result = (CompanyVO) mediator.getGraph(getPrototype(0));
        } catch (MediatorException e) {
            // expected
            return;
        }
        fail("Got no exception while trying to retrieve deleted graph");
    }

    public void testTwoCompanies() throws MediatorException {
        updateCompany1();
        updateCompany2();
    }

    public void testConcurrency() throws MediatorException {
        CompanyVO result = updateCompany1();

        result.setName("new");
        mediator.updateGraph(result);
        result.setName("new2");
        try {
            mediator.updateGraph(result);
        } catch (ConcurrencyException e) {
            // expected
            return;
        }
        fail("Got no concurrency exception while trying to update graph");
    }

    //- private helper methods.
    private CompanyVO updateCompany1() throws MediatorException {
        mediator.updateGraph(company1);
        CompanyVO companyVO = getPrototype(0);
        CompanyVO result = (CompanyVO) mediator.getGraph(companyVO);
        assertEquals(company1.getName(), result.getName());

        return result;
    }

    private CompanyVO updateCompany2() throws MediatorException {
        mediator.updateGraph(company2);
        CompanyVO companyVO = getPrototype(1);
        CompanyVO result = (CompanyVO) mediator.getGraph(companyVO);
        assertEquals(company2.getName(), result.getName());

        return result;
    }

    private CompanyVO getPrototype(long pk) {
        CompanyVO companyVO = new CompanyVO();
        companyVO.setId(new Long(pk));
        return companyVO;
    }
}
