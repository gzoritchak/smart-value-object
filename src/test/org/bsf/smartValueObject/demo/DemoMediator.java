package org.bsf.smartValueObject.demo;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bsf.smartValueObject.SmartAccess;
import org.bsf.smartValueObject.mediator.ConcurrencyException;
import org.bsf.smartValueObject.mediator.Mediator;
import org.bsf.smartValueObject.mediator.MediatorException;
import org.bsf.smartValueObject.mediator.ChangeSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Mock Mediator for the Demo.
 *
 */
public class DemoMediator implements Mediator, java.io.Serializable {

    private static Log log = LogFactory.getLog(DemoMediator.class);
   /** Our storage, to avoid EJB local entities. */
    private Map storageCompany;
    /** Our storage, to avoid EJB local entities. */
    private Map storageSubsidiary;
    /** Class of the VO (would be configured on runtime). */
    private Class voClazzCompany = CompanyVO.class;
    /** The index field in the VO (would be configured on runtime). */
    private String indexField = "id";
    private Map versionCache;

    private CompanyLocalHome companyHome;
    private SubsidiaryLocalHome subsidiaryHome;


    public DemoMediator() {
        storageCompany = new HashMap();
        storageSubsidiary = new HashMap();

        companyHome = new CompanyLocalHome();
        subsidiaryHome = new SubsidiaryLocalHome();

        versionCache = new HashMap();
    }

    /**
     * Retrieves graph based on given prototype.
     *
     * @param prototype
     * @return
     * @throws MediatorException
     */
    public Object getGraph(Object prototype)
            throws MediatorException {
        log.debug("getGraph(" + prototype + ")");
        verifyGraph(prototype);

        CompanyVO company = (CompanyVO) prototype;

        CompanyEntity ce = getCompanyEntityByVO(company);
        if (ce == null)
            throw new MediatorException("Object not found");

        CompanyVO result = newCompanyVO(ce);

        // cache version
        Long id = getVersionCache(result);
        if (id != null) {
            SmartAccess.setVersionId(result, id.longValue());
        } else {
            updateVersionCache(result);
        }
        return result;
    }


    /**
     * Stores graph and return summary of changes.
     *
     * @param graph
     * @return update log.
     * @throws MediatorException
     */
    public ChangeSummary updateGraph(Object graph)
            throws MediatorException {
        log.info("updateGraph(" + graph + ")");
        String status = "No updates performed.";
        verifyGraph(graph);
        checkConcurrency(graph);
        CompanyVO company = (CompanyVO) graph;
        CompanyVO newCompany = null;

        if (SmartAccess.isGraphDirty(company)) {
            StringWriter writer = new StringWriter();
            CompanyEntity ce = updateCompany(company, new PrintWriter(writer));
            newCompany = newCompanyVO(ce);
            updateVersionCache(newCompany);
            status = writer.toString();
            return new ChangeSummary(getPK(newCompany), status);
        } else {
            log.info("Nothing to do.");
            return new ChangeSummary(null, "Nothing");
        }
    }

    /**
     * Deletes a graph.
     * @param graph the root element of the graph.
     * @throws MediatorException
     */
    public void deleteGraph(Object graph)
            throws MediatorException {
        verifyGraph(graph);
        checkConcurrency(graph);
        CompanyVO company = (CompanyVO) graph;
        removeCompany(company);
        removeVersionCache(company);
    }

    /**
     * Creates new company based on VO.
     * @param vo
     * @throws MediatorException
     */
    private CompanyEntity newCompany(CompanyVO vo, PrintWriter logger) throws MediatorException {
        log.debug("newCompany(" + vo + ")");
        CompanyEntity ce = companyHome.create();
        copyProperties(ce, vo);
        logger.println("created company entity " + ce);

        Collection subsidiaries = vo.getSubsidiaries();
        Iterator created = SmartAccess.createdIterator(subsidiaries);
        while (created.hasNext()) {
            SubsidiaryVO subvo = (SubsidiaryVO) created.next();
            SubsidiaryEntity se = newSubsidiary(subvo);
            ce.getSubsidiaries().add(se);
            logger.println("added subsidiary " + subvo);
        }
        return ce;
    }

    /**
     * Updates (or creates) company based on VO.
     * @param vo
     * @throws MediatorException
     */
    private CompanyEntity updateCompany(CompanyVO vo, PrintWriter logger) throws MediatorException {
        log.debug("updateCompany(" + vo + ")");
        Object pk = getPK(vo);
        CompanyEntity ce = null;
        if (pk == null && vo.getName() != null) {
           return newCompany(vo, logger);
        } else if ((ce = companyHome.findByPk(pk)) == null) {
            if (vo.getName() != null) {
                return newCompany(vo, logger);
            } else {
                throw new MediatorException("object for update not found (" +
                        vo + ") and missing attribute" +
                        " (name) to create new object");
            }
        }

        if (SmartAccess.isDirty(vo)) {
            copyProperties(ce, vo);
            logger.println("Updated company " + vo);
        }

        Collection c = vo.getSubsidiaries();

        Iterator modified = SmartAccess.modifiedIterator(c);
        while (modified.hasNext()) {
            SubsidiaryVO subsidiaryVO = (SubsidiaryVO) modified.next();

            if (SmartAccess.isDeleted(subsidiaryVO)) {
                ce.getSubsidiaries().remove(getSubsidiaryEntitybyVO(subsidiaryVO));
                removeSubsidiary(subsidiaryVO);
                logger.println("Removed " + subsidiaryVO);
            } else if (SmartAccess.isCreated(subsidiaryVO)) {
                SubsidiaryEntity se = newSubsidiary(subsidiaryVO);
                ce.getSubsidiaries().add(se);
                logger.println("Added " + subsidiaryVO);
            } else {
                updateSubsidiary(subsidiaryVO);
                logger.println("Updated " + subsidiaryVO);
            }
        }

        return ce;
    }

    /**
     * Removes company based on VO.
     * @param vo
     * @throws MediatorException
     */
    private void removeCompany(CompanyVO vo) throws MediatorException {
        CompanyEntity ce = getCompanyEntityByVO(vo);
        if (ce == null)
            throw new MediatorException("Object not found");

        Collection subsidiares = ce.getSubsidiaries();
        for (Iterator iterator = subsidiares.iterator(); iterator.hasNext();) {
            SubsidiaryEntity entity = (SubsidiaryEntity) iterator.next();
            subsidiaryHome.remove(entity.getId());
        }
        ce.getSubsidiaries().clear();
        companyHome.remove(ce.getId());
    }

    /**
     * Creates new subsidiary based on VO.
     * @param vo
     * @return
     * @throws MediatorException
     */
    private SubsidiaryEntity newSubsidiary(SubsidiaryVO vo) throws MediatorException {
        log.debug("newSubsidiary(" + vo + ")");
        SubsidiaryEntity se = subsidiaryHome.create();
        copyProperties(se, vo);
        return se;
    }

    /**
     * Updates subsidiary based on VO.
     * @param vo
     * @throws MediatorException
     */
    private void updateSubsidiary(SubsidiaryVO vo) throws MediatorException {
        log.debug("updateSubsidiary(" + vo + ")");
        SubsidiaryEntity se = getSubsidiaryEntitybyVO(vo);
        copyProperties(se, vo);
    }

    /**
     * Removes subsidiary based on VO.
     * @param vo
     * @throws MediatorException
     */
    private void removeSubsidiary(SubsidiaryVO vo) throws MediatorException {
        log.debug("removeSubsidiary(" + vo + ")");
        subsidiaryHome.remove(getPK(vo));
    }

    /**
     * Gets primary key from object (from the field specied in indexField).
     * @param o
     * @return
     * @throws MediatorException
     */
    private Object getPK(Object o) throws MediatorException {
        Object pk;
        try {
            Field field = o.getClass().getField(indexField);
            pk = field.get(o);
        } catch (Exception e) {
            throw new MediatorException("Could not find pk", e);
        }
        return pk;
    }

    /**
     * Gets company entity from VO.
     * @param vo
     * @return
     * @throws MediatorException
     */
    private CompanyEntity getCompanyEntityByVO(CompanyVO vo) throws MediatorException {
        Object pk = getPK(vo);
        if (pk == null)
            throw new MediatorException("No pk for object " + vo + " found");

        CompanyEntity ce = companyHome.findByPk(pk);
        return ce;
    }

    /**
     * Gets subsidiary entitiy from VO.
     * @param vo
     * @return
     * @throws MediatorException
     */
    private SubsidiaryEntity getSubsidiaryEntitybyVO(SubsidiaryVO vo) throws MediatorException {
        Object pk = getPK(vo);
        if (pk == null)
            throw new MediatorException("No pk for object " + vo + " found");

        SubsidiaryEntity se = subsidiaryHome.findByPk(pk);
        return se;
    }

    /**
     * Verifies if the graph argument is valid.
     * @param graph
     * @throws MediatorException
     */
    private void verifyGraph(Object graph) throws MediatorException {
        if (graph == null || graph.getClass().getName() !=  voClazzCompany.getName()) {
            throw new MediatorException("Invalid graph object");
        }

        if (!SmartAccess.isVersionable(graph)) {
            throw new MediatorException("Object not versionable");
        }

        if (!graph.getClass().getName().equals(voClazzCompany.getName())) {
            throw new MediatorException("Object not a root object");
        }
    }

    private void updateVersionCache(Object vo) throws MediatorException {
        Object key = getPK(vo);
        Map cache = (Map) versionCache.get(vo.getClass());
        if (cache == null) {
            cache = new HashMap();
            versionCache.put(vo.getClass(), cache);
        }
        cache.put(key, new Long(SmartAccess.getVersionId(vo)));
    }

    private Long getVersionCache(Object vo) throws MediatorException {
        Object key = getPK(vo);
        Map cache = (Map) versionCache.get(vo.getClass());
        if (cache == null) {
            return null;
        } else {
            return (Long) cache.get(key);
        }
    }

    private void removeVersionCache(Object vo) throws MediatorException {
        Object key = getPK(vo);
        Map cache = (Map) versionCache.get(vo.getClass());
        if (cache != null)
            cache.remove(key);
    }

    private void checkConcurrency(Object vo) throws MediatorException {
        Long id = getVersionCache(vo);
        if (id == null)
            return; // no version id, ok (we assume object has not been created yet)

        if (SmartAccess.getVersionId(vo) != id.longValue())
            throw new ConcurrencyException();
    }

    /**
     * Wrapper around BeanUtils.copyProperties.
     * @param dst
     * @param src
     * @throws MediatorException
     */
    private static void copyProperties(Object dst, Object src) throws MediatorException {
        try {
            BeanUtils.copyProperties(dst, src);
        } catch (Exception e) {
            throw new MediatorException("Error copying properties", e);
        }

    }

    /**
     * Creates new CompanyVO object.
     * @return
     */
    private static CompanyVO newCompanyVO() {
        return new CompanyVO();
    }

    /**
     * Creates new SubsidiaryVO object.
     * @return
     */
    private static SubsidiaryVO newSubsidiaryVO() {
        return new SubsidiaryVO();
    }

    /**
     * Creates new SubsidiaryVO object and initializes its field with
     * the given SubsidiaryEntity.
     *
     * @param se
     * @return
     * @throws MediatorException
     */
    private static SubsidiaryVO newSubsidiaryVO(SubsidiaryEntity se) throws MediatorException {
        SubsidiaryVO vo = newSubsidiaryVO();
        copyProperties(vo, se);
        SmartAccess.reset(vo);
        return vo;
    }

    private static CompanyVO newCompanyVO(CompanyEntity ce) throws MediatorException {
        CompanyVO result = newCompanyVO();
        copyProperties(result, ce);
        Collection subsidiaries = ce.getSubsidiaries();
        for (Iterator iterator = subsidiaries.iterator(); iterator.hasNext();) {
            SubsidiaryEntity entity = (SubsidiaryEntity) iterator.next();
            SubsidiaryVO vo = newSubsidiaryVO(entity);
            result.addSubsidiary(vo);
        }

        SmartAccess.resetGraph(result);
        return result;
    }

    /**
     * Mock class for real entity.
     */
    public static class CompanyEntity implements java.io.Serializable {
        private Long id;
        private String name;
        private java.util.Date creationDate;
        private Collection subsidiaries = new ArrayList();

        public CompanyEntity(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Collection getSubsidiaries() {
            return subsidiaries;
        }

        public java.util.Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(java.util.Date creationDate) {
            this.creationDate = creationDate;
        }
    }

    /**
     * Mock class for real entity.
     */
    public static class SubsidiaryEntity implements java.io.Serializable {
        private Long id;
        private String name;
        private Long workforce;

        public SubsidiaryEntity(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getWorkforce() {
            return workforce;
        }

        public void setWorkforce(Long workforce) {
            this.workforce = workforce;
        }
    }

    /**
     * LocalHome mock for Company.
     */
     private  class CompanyLocalHome implements java.io.Serializable {
        private long counter = 0;

        public CompanyEntity findByPk(Object pk) {
             return (CompanyEntity) storageCompany.get(pk);
        }

        public CompanyEntity create() {
            CompanyEntity ce = new CompanyEntity(new Long(counter++));
            persist(ce);
            return ce;
        }

        public void persist(CompanyEntity ce) {
            storageCompany.put(ce.getId(), ce);
        }

        public void remove(Object pk) {
            storageCompany.remove(pk);
        }
     }

    /**
     * LocalHome mock for Subsidiary.
     */
    private class SubsidiaryLocalHome implements java.io.Serializable {
        private long counter = 0;

        public SubsidiaryEntity findByPk(Object pk) {
            return (SubsidiaryEntity) storageSubsidiary.get(pk);
        }

        public SubsidiaryEntity create() {
            SubsidiaryEntity se = new SubsidiaryEntity(new Long(counter++));
            persist(se);
            return se;
        }

        public void persist(SubsidiaryEntity se) {
            storageSubsidiary.put(se.getId(), se);
        }

        public void remove(Object pk) {
            log.debug("SubsidiaryLocalHome.remove(" + pk + ")");
            if (storageSubsidiary.remove(pk) == null)
                log.debug("SubsidiaryLocalHome.remove failed");
        }
    }
}
