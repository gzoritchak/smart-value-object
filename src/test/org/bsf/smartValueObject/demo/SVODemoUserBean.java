package org.bsf.smartValueObject.demo;

import org.bsf.commons.ejb.SessionAdapterBean;
import org.bsf.smartValueObject.mediator.*;

import java.util.*;
import java.lang.reflect.Constructor;

/**
 * This stateful bean is the one used by each client to simulate a
 * persistence service.
 *
 *  @ejb:bean       type="Stateful"
 *                  name="SVODemoUser"
 *                  jndi-name="ejb/SVODemoUser"
 *                  view-type="both"
 *
 *  @ejb:home       local-extends="javax.ejb.EJBLocalHome"
 *                  extends="javax.ejb.EJBHome"
 *
 *  @ejb:interface  local-extends="javax.ejb.EJBLocalObject"
 *                  extends="javax.ejb.EJBObject"
 *
 *  @ejb:transaction type="Required"
 *
 *  @ejb.env-entry  name="mediator.class_name"
 *                  value="org.bsf.smartValueObject.mediator.HibernateMediator"
 *
 */
public class SVODemoUserBean extends SessionAdapterBean {

    private Mediator userMediator;


    /**
     * @ejb:interface-method
     */
    public void ejbCreate() {
        /*
        TODO deprecated, throws exception

        String mediatorClass = getEJBContext()
                .getEnvironment()
                .getProperty("mediator.class_name");
        */

        String mediatorClass = "org.bsf.smartValueObject.mediator.HibernateMediator";
        logInfo("using " + mediatorClass + " as mediator impl");
        try {
            Class clazz =
                SVODemoUserBean.class.getClassLoader().loadClass(mediatorClass);

            Constructor ctor;
            try {
               ctor = clazz.getConstructor(new Class[] { MediatorConfig.class });
               userMediator = (Mediator)
                        ctor.newInstance( new Object[] { new DemoConfig() } );
            } catch (NoSuchMethodException e) {
               userMediator = (Mediator) clazz.newInstance();
            }

        } catch(Exception e) {
            logError("error", e);
            handleExceptionAsSystemException(e);
        }

        try {
            userMediator.updateGraph( buildUserGraph() );
        } catch( MediatorException e ) {
            handleExceptionAsSystemException( e );
        }
        logInfo( "SVODemoUser created..." );
    }

    /**
     * @ejb:interface-method
     */
    public Object getCompanyVo( Object propotype ) {
        Object result = null;
        try {
            result = userMediator.getGraph( propotype );
        } catch( MediatorException e ) {
            handleExceptionAsSystemException( e );
        }
        return result;
    }

    /**
     * @ejb:interface-method
     */
    public String updateCompanyVo( Object propotype ) throws org.bsf.smartValueObject.mediator.ConcurrencyException {
        try {
            ChangeSummary cs =  userMediator.updateGraph( propotype );
            return cs.getReport();
        } catch( MediatorException e ) {
            if ( e instanceof ConcurrencyException ) {
                ConcurrencyException ce = (ConcurrencyException) e;
                throw ce;
            } else {
                handleExceptionAsSystemException( e );
            }
        }
        return null;
    }

    /**
     * Simulates a concurrent modification on the graph
     *
     * @ejb:interface-method
     */
    public void modifyConcurrently() {
        try {
            CompanyVO vo = new CompanyVO();
            vo.setId( new Long( 0 ) );
            CompanyVO comp = (CompanyVO) userMediator.getGraph( vo );
            Iterator subIt = comp.subsidiaries();
            while ( subIt.hasNext() ) {
                SubsidiaryVO subsidiaryVO = (SubsidiaryVO) subIt.next();
                subsidiaryVO.setName( subsidiaryVO.getName() + " (Modified)" );
                break;
            }
            userMediator.updateGraph( comp );
        } catch( MediatorException e ) {
            handleExceptionAsSystemException( e );
        }

    }

    private static final Object buildUserGraph() {
        CompanyVO comp = new CompanyVO();
        comp.setName( "The big big company" );

        // set time to actual date, 0:00:00
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "Europe/Paris" ) );
        cal.set( Calendar.HOUR_OF_DAY, 12 ); // For the $@&#! offset, keep the same day
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
        comp.setCreationDate( cal.getTime() );

        SubsidiaryVO sub = new SubsidiaryVO();
        sub.setName( "Paris" );
        sub.setWorkforce( 2 );
        comp.addSubsidiary( sub );

        sub = new SubsidiaryVO();
        sub.setName( "London" );
        sub.setWorkforce( 1 );
        comp.addSubsidiary( sub );

        sub = new SubsidiaryVO();
        sub.setName( "Berlin" );
        sub.setWorkforce( 1 );
        comp.addSubsidiary( sub );

        return comp;
    }

    /*
    private static class DemoConfig implements MediatorConfig {
        private static final Collection classes = new ArrayList();

        static {
            classes.add(org.bsf.smartValueObject.demo.CompanyVO.class);
            classes.add(org.bsf.smartValueObject.demo.SubsidiaryVO.class);
        }


        public Collection getClasses() {
            return classes;
        }
    }
    */
}
