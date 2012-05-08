package org.bsf.smartValueObject.demo;

import org.bsf.smartValueObject.mediator.MediatorConfig;

import java.util.Collection;
import java.util.ArrayList;

/**
 * DemoConfig.
 * 
 */
public class DemoConfig implements MediatorConfig {
    private static final Collection classes = new ArrayList();

    static {
        classes.add(org.bsf.smartValueObject.demo.CompanyVO.class);
        classes.add(org.bsf.smartValueObject.demo.SubsidiaryVO.class);
    }

    /**
     * Gets classes of the VOs.
     * @return
     */
    public Collection getClasses() {
        return classes;
    }
}

