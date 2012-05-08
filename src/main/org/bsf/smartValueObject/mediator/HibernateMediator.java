package org.bsf.smartValueObject.mediator;

import net.sf.hibernate.*;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.cfg.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.lang.reflect.Field;

/**
 * Mediator for Hibernate.
 * 
 */
public class HibernateMediator implements Mediator {
    private static final Log log = LogFactory.getLog(HibernateMediator.class);
    private SessionFactory sessions;
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final Object DBNAME   = "test";
    private String indexField = "id";
    private MediatorConfig config;

    public HibernateMediator(MediatorConfig config) {
        this.config = config;
        initHibernate(config.getClasses());
    }

    private void initHibernate(Collection classes) {
        try {
            Configuration config = new Configuration();

            for (Iterator it = classes.iterator(); it.hasNext(); )
                config.addClass((Class)it.next());

//            config.setProperties(getMySqlProperties());
            config.setProperties(getHsqlDbProperties());
            sessions = config.buildSessionFactory();
            log.debug("created SessionFactory");
        } catch (HibernateException e) {
            log.fatal("could not create SessionFactory", e);
        }
    }

    /**
     * Gets configuration for MySQL.
     * @return
     */
    private Properties getMySqlProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect",
                "net.sf.hibernate.dialect.MySQLDialect");

        props.setProperty("hibernate.cache.provider_class",
                "net.sf.hibernate.cache.HashtableCacheProvider");

        props.setProperty("hibernate.connection.driver_class",
                "com.mysql.jdbc.Driver");

        props.setProperty("hibernate.connection.username", USERNAME);
        props.setProperty("hibernate.connection.password", PASSWORD);
        props.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/" + DBNAME);
        return props;
    }

    /**
     * Gets configuration for HsqlDB.
     * @return
     */
    private Properties getHsqlDbProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect",
                "net.sf.hibernate.dialect.HSQLDialect");

        props.setProperty("hibernate.cache.provider_class",
                "net.sf.hibernate.cache.HashtableCacheProvider");

        props.setProperty("hibernate.connection.driver_class",
                "org.hsqldb.jdbcDriver");

        props.setProperty("hibernate.connection.username", "sa");
        props.setProperty("hibernate.connection.password", "");
        props.setProperty("hibernate.connection.url", "jdbc:hsqldb:" + DBNAME);
        return props;
    }

    /**
     * Retrieves a graph based on given prototype.
     *
     * @param prototype a prototype to obtain a initialized graph.
     * @return
     * @throws MediatorException
     */
    public Object getGraph(Object prototype)
            throws MediatorException {
        try {
            Object pk = getPK(prototype);

            Session s = sessions.openSession();
            Criteria crit = s.createCriteria(prototype.getClass());
            crit.add(Expression.eq(indexField, pk));
            return crit.uniqueResult();
        } catch (HibernateException e) {
            throw new MediatorException(e);
        }
    }

    /**
     * Stores a graph.
     *
     * @param graph the root element of the graph.
     * @throws MediatorException
     */
    public ChangeSummary updateGraph(Object graph)
            throws MediatorException {
        Session s = null;
        Transaction tx = null;

        try {
            s = sessions.openSession();
            tx = s.beginTransaction();
            s.saveOrUpdate(graph);
            tx.commit();
            s.close();
        } catch (HibernateException e) {
            try { if (tx!=null) tx.rollback(); }
			catch (HibernateException he) {}
            throw new MediatorException(e);
        }
        return new ChangeSummary(getPK(graph), null);
    }

    /**
     * Deletes a graph.
     * @param graph the root element of the graph.
     * @throws MediatorException
     */
    public void deleteGraph(Object graph)
            throws MediatorException {
        Transaction tx = null;
        Session s = null;
        try {
            s = sessions.openSession();
            tx = s.beginTransaction();
            s.delete(graph);
            tx.commit();
            s.close();
        } catch (HibernateException e) {
            try { if (tx!=null) tx.rollback(); }
			catch (HibernateException he) {}
            throw new MediatorException();
        }
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
}
