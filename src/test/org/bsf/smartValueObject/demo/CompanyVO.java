package org.bsf.smartValueObject.demo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Company.
 *
 * @hibernate.class table="company"
 */
public class CompanyVO implements Serializable {
    private Long id;
    private String name;
    private Date creationDate;
    private Collection subsidiaries = new ArrayList();

    private final SimpleDateFormat _dateFormat = new SimpleDateFormat( "dd/MM/yy" );

    /** @hibernate.id generator-class="native" column="id" unsaved-value="0" */
    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    /** @hibernate.property column="name" */
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    /** @hibernate.property column="creationdate" */
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate( Date creationDate ) {
        this.creationDate = creationDate;
    }

    public void addSubsidiary( SubsidiaryVO s ) {
        s.setCompanyVO(this);
        subsidiaries.add( s );
    }

    public void removeSubsidiary( SubsidiaryVO s ) {
        subsidiaries.remove( s );
    }

    /**
     * @hibernate.bag inverse="true" cascade="all"
     * @hibernate.collection-one-to-many class="org.bsf.smartValueObject.demo.SubsidiaryVO"
     * @hibernate.collection-key column="companyId"
     */
    public Collection getSubsidiaries() {
        return subsidiaries;
    }

    // hibernate requirement
    private void setSubsidiaries(Collection s) {
        this.subsidiaries = s;
    }

    public Iterator subsidiaries() {
        return subsidiaries.iterator();
    }

    public String toString() {
        String dateAsString = "";

        if ( creationDate != null ) {
            // We don't want to return the time...
            dateAsString = _dateFormat.format( creationDate );
        }

        return "CompanyVO: id=" + id + " name=" + name + " creationdate=" + dateAsString;
    }
}
