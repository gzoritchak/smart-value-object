package org.bsf.smartValueObject.demo;

import java.io.Serializable;

/**
 * Subsidiary.
 *
 * @hibernate.class table="subsidiary"
 */
public class SubsidiaryVO implements Serializable {
    private Long id;
    private String name;
    private long workforce;
    private CompanyVO companyVO;

    /** @hibernate.id generator-class="native" column="id" unsaved-value="0" */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** @hibernate.property column="name" */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @hibernate.property column="workforce" */
    public long getWorkforce() {
        return workforce;
    }

    public void setWorkforce(long workforce) {
        this.workforce = workforce;
    }

    /** @hibernate.many-to-one column="companyId" not-null="true" */
    public CompanyVO getCompanyVO() {
        return this.companyVO;
    }

    public void setCompanyVO(CompanyVO c) {
        this.companyVO = c;
    }

    public String toString() {
        return "SubsidiaryVO: id=" + id + " name=" + name + " workforce=" + workforce;
    }
}
