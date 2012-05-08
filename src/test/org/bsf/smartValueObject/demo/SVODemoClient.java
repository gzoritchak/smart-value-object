package org.bsf.smartValueObject.demo;

import junit.framework.TestCase;
import org.bsf.remoting.EJBDefinition;
import org.bsf.remoting.http.HttpServiceFactory;
import org.bsf.smartValueObject.mediator.ConcurrencyException;
import org.bsf.smartValueObject.Versionable;
import org.bsf.smartValueObject.SmartAccess;

import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * Handles the client-server exchanges of the demo.
 */
public class SVODemoClient extends TestCase {

    private static final EJBDefinition SVO_SERVICE =
            new EJBDefinition( "ejb/SVODemoService",
                               "org.bsf.smartValueObject.demo.SVODemoServiceHome",
                               "org.bsf.smartValueObject.demo.SVODemoService" );

    private SVODemoService svoService = null;
    private SVODemoUser svoUser = null;

    public SVODemoClient() {
        HttpServiceFactory factory = new HttpServiceFactory();
        factory.setHost( "localhost" );
        factory.setPort( 8080 );
        factory.setServerContext( "SvoDemo" );
        svoService = (SVODemoService) factory.getService( SVO_SERVICE );
        try {
            svoUser = svoService.createUserService();
        } catch( java.rmi.RemoteException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the user company graph
     * @return
     */
    public CompanyVO getCompanyGraph() throws RemoteException {
        CompanyVO vo = new CompanyVO();

        //The id is normally retrieved from a list of object (JTable).
        vo.setId( new Long( 0 ) );

        return (CompanyVO) svoUser.getCompanyVo( vo );
    }

    public String storeCompanyGraph( Object graph ) throws RemoteException,
            ConcurrencyException {
        return svoUser.updateCompanyVo( graph );
    }

    /**
     * Used to simulate a concurrent modification on datas.
     */
    public void modifyConcurrently() throws RemoteException {
        svoUser.modifyConcurrently();
    }

    ///////////////////////////////////////////////////////////////////////
    // TESTS
    //////////////////////////////////////////////////////////////////////

    /**
     * Test method
     * @throws Exception
     */
    public void testSvoUser() throws Exception {
        CompanyVO vo = this.getCompanyGraph();
        assertNotNull( vo );
        assertEquals( new Long( 0 ), vo.getId() );

        SubsidiaryVO[] subAr = (SubsidiaryVO[])
                vo.getSubsidiaries().toArray(
                        new SubsidiaryVO[ vo.getSubsidiaries().size() ] );
        SubsidiaryVO parisSub = subAr[ 0 ];
        parisSub.setName( "Joli Paris" );

        System.out.println( this.storeCompanyGraph( vo ) );
        vo = this.getCompanyGraph();

        this.modifyConcurrently();

        vo.setName( vo.getName().toUpperCase() );

        try {
            this.storeCompanyGraph( vo );
        } catch( ConcurrencyException e ) {
            return;
        }
        //Should have thrown a concurrency exception
        fail( "Expected concurrency exception" );
    }

    public void testBug() throws Exception {
        CompanyVO vo = this.getCompanyGraph();
        assertNotNull( vo );
        assertEquals( new Long( 0 ), vo.getId() );

        for (Iterator it = vo.subsidiaries(); it.hasNext(); ) {
            vo.removeSubsidiary((SubsidiaryVO) it.next());
        }
        assertTrue(!((Versionable) vo).isDirty());
        System.out.println(this.storeCompanyGraph(vo));
    }

    public void testEverythingCleanOnBegin() throws Exception {
        CompanyVO vo = this.getCompanyGraph();
        assertNotNull( vo );
        assertEquals( new Long( 0 ), vo.getId() );
        assertTrue(!SmartAccess.isGraphDirty(vo));
    }
}
