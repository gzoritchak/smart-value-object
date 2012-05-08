package org.bsf.smartValueObject.mediator;

import junit.framework.TestCase;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.bsf.smartValueObject.demo.CompanyVO;
import org.bsf.smartValueObject.demo.SubsidiaryVO;
import org.bsf.smartValueObject.demo.DemoConfig;

/**
 * Testcase Hibernate Mediator
 *
 */
public class TestHibernateMediator extends TestCase {
    private Mediator mediator;
    private Date date;
    private final String createCompanySql = "CREATE TABLE company(" +
            "id IDENTITY," +
            "name VARCHAR(30)," +
            "creationDate DATE" +
            ");";
    private final String createSubsidiarySql = "CREATE TABLE subsidiary(" +
            "id IDENTITY," +
            "name VARCHAR(30)," +
            "workforce INT," +
            "companyId INT" +
            ");";
    private final String destroyCompanySql = "DROP TABLE company IF EXISTS;";
    private final String destroySubsidiarySql = "DROP TABLE subsidiary IF EXISTS;";

    public void setUp() throws Exception {
        createTestDb();
        mediator = new HibernateMediator(new DemoConfig());
    }

    public void tearDown() throws Exception {
        destroyTestDb();
    }

    public void testUpdate() throws MediatorException {
        ChangeSummary cs = mediator.updateGraph(buildUserGraph());
        CompanyVO company = new CompanyVO();
        company.setId((Long) cs.getKey());
        CompanyVO result = (CompanyVO) mediator.getGraph(company);

        assertTrue(result != null);
        assertEquals("The big big company", result.getName());

        assertEquals(date, result.getCreationDate());
        assertEquals(3, result.getSubsidiaries().size());

        mediator.deleteGraph(result);

        result = (CompanyVO) mediator.getGraph(company);
        assertTrue(result == null);
    }

    public void testConcurrency() throws MediatorException {

    }

    private Object buildUserGraph() {
        CompanyVO comp = new CompanyVO();
        comp.setName( "The big big company" );

        // set time to actual date, 0:00:00
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "Europe/Paris" ) );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );

        date = cal.getTime();
        comp.setCreationDate( date );

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

    //////////// private helper methods /////////////
    private Connection getConnection() throws Exception {
        String driver = "org.hsqldb.jdbcDriver";
        Class.forName(driver).newInstance();
        String url = "jdbc:hsqldb:";
        String database = "test";
        String user = "sa";
        String password = "";
        Connection conn = DriverManager.getConnection(
                url + database,
                user,
                password);
        return conn;
    }

    /**
     * Creates test database.
     * @throws Exception
     */
    private void createTestDb() throws Exception {
        Connection conn = getConnection();
        Statement s = conn.createStatement();
        s.execute(createCompanySql);
        s.close();
        s = conn.createStatement();
        s.execute(createSubsidiarySql);
        s.close();
    }

    /**
     * Destroys database.
     * @throws Exception
     */
    private void destroyTestDb() throws Exception {
        Connection conn = getConnection();
        Statement s = conn.createStatement();
        s.execute(destroyCompanySql);
        s.close();
        s = conn.createStatement();
        s.execute(destroySubsidiarySql);
        s.close();
    }
}
