package org.bsf.smartValueObject.demo;

import org.bsf.commons.ejb.SessionAdapterBean;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This EJB is used as the server service for the SVO demo.
 * Its main goal is to create the stateful bean that the client will
 * use.
 *
 *  @ejb:bean       type="Stateless"
 *                  name="SVODemoService"
 *                  jndi-name="ejb/SVODemoService"
 *                  generate="true"
 *                  view-type="remote"
 *
 *  @ejb:home       extends="javax.ejb.EJBHome"
 *                  generate="remote"
 *
 *  @ejb:interface  extends="javax.ejb.EJBObject"
 *                  generate="remote"
 *
 *  @ejb:transaction type="Required"
 *
 *  @ejb:ejb-ref    ejb-name="SVODemoUser"
 *                  view-type="remote"
 *
 */
public class SVODemoServiceBean extends SessionAdapterBean {
    SVODemoUserHome userHome = null;

    /**
     * @ejb:interface-method
     */
    public SVODemoUser createUserService(){
        try {
            logInfo("SVODemo : new user connected.");

            return userHome.create();
        } catch (Exception e) {
            handleExceptionAsSystemException(e);
            return null;
        }
    }

    /**
     * During the creation of the EJB we retrieve a ref on the local home
     * of the SVODemoUser
     * @throws CreateException
     */
    public void ejbCreate() throws CreateException {

        // Retrieves the local home of the Lov Entity bean and keeps it for further use
        try {
            InitialContext ic = new InitialContext();
            userHome = (SVODemoUserHome)
                    ic.lookup("java:comp/env/ejb/SVODemoUser");
        } catch( NamingException e ) {
            handleExceptionAsSystemException( e );
        }
    }
}
