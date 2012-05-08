package org.bsf.smartValueObject.mediator;

import org.bsf.smartValueObject.SmartAccess;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * A mediator for EJBs. This class is to be used by a session facade
 * to retrieve/update objects based on graph of VOs.
 * Right now we use a dummy implementation to simulate
 * real ejb lookups. Implementation is not yet finished !
 * 
 */
public class EjbSvoMediator implements Mediator {
    private static Log log = LogFactory.getLog(EjbSvoMediator.class);
    /** The class of the VO. */
    private Class clazz;
    /** To config lookups etc. */
    private Map config;
    /** The index field of the VO */
    private String indexField;
    /* Dummy storage for testing purposes. */
    private Map objects;
    /* The JNDI context */
    private Context context = null;
    /* The Home Interface */
    private Class homeClass;

    public static final String CONTEXT = "context";
    public static final String HOMECLASS = "homeclass";

    /**
     * Creates a mediator for the given class.
     *
     * @param clazz  the class of the VO.
     * @param config configuration parameters.
     */
    public EjbSvoMediator(Class clazz, Map config) {
        this.clazz = clazz;
        this.config = config;
        this.objects = new HashMap();
        readConfig(this.config);
    }

    /**
     * Retrieves graph based on given prototype.
     *
     * @param prototype
     * @return
     * @throws MediatorException
     */
    public Object getGraph(Object prototype) throws MediatorException {
        log.info("getGraph(" + prototype + ")");
        if (prototype == null || prototype.getClass().getName() != clazz.getName())
                throw new MediatorException("Unknown prototype");

        return lookFor(prototype);
    }

    /**
     * Stores graph.
     * @param graph
     * @throws MediatorException
     */
    public ChangeSummary updateGraph(Object graph) throws MediatorException {
        log.info("updateGraph(" + graph + ")");
        if (graph == null || graph.getClass().getName() != clazz.getName())
                 throw new MediatorException("Unknown prototype");

        if (!SmartAccess.isVersionable(graph)) {
            throw new MediatorException("Object not versionable");
        }

        if (SmartAccess.isGraphDirty(graph)) {
            storeEJB(graph);
        }
        return new ChangeSummary(null, null);
    }

    /**
     * Deletes a graph.
     * @param graph the root element of the graph.
     * @throws MediatorException
     */
    public void deleteGraph(Object graph)
            throws MediatorException {
    }

    private void storeEJB(Object graph) throws MediatorException {
        log.info("storeEJB(" + graph + ")");

        Object index;
        try {
            index = graph.getClass().getField(indexField).get(graph);
        } catch (Exception e) {
            throw new MediatorException(e);
        }

        objects.put(index, graph);
    }

    private Object lookFor(Object o) throws MediatorException {
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getName().equals(indexField))  {
                Object index;
                try {
                    index = field.get(o);
                } catch (Exception e) {
                    throw new MediatorException(e);
                }

                if (index != null)
                    return lookForEJB(index);
            }
        }

        throw new MediatorException("Object not found");
    }

    private Object lookForEJB(Object index) throws MediatorException {
        log.info("lookForEJB: " + index);
        Object o = createNewVO();
        Object src = lookForIndex(index);

        try {
            BeanUtils.copyProperties(o, src);
        } catch (Exception e) {
            throw new MediatorException(e);
        }
        return o;
    }

    // fake ejb lookup
    private Object lookForIndex(Object index) {
        return objects.get(index);
    }

    private void readConfig(Map config) {
        indexField = (String) config.get(INDEXFIELD);
        context = (Context) config.get(CONTEXT);
    }

    /**
     * Gets an 'empty' VO.
     * @return
     * @throws MediatorException
     */
    private Object createNewVO() throws MediatorException {
        Object o;
        try {
            o = clazz.newInstance();
        } catch (Exception e) {
            throw new MediatorException(e);
        }
        return o;
    }

    /** Gets initial context. */
    private Context getContext() {
		if (context == null) {
			try {
				Properties props = new Properties();
				props.put(Context.INITIAL_CONTEXT_FACTORY,
					"org.jnp.interfaces.NamingContextFactory");
				props.put(Context.PROVIDER_URL,
					"jnp://localhost:1099");
				props.put(Context.URL_PKG_PREFIXES,
					"org.jboss.naming:org.jnp.interfaces");

				context = new InitialContext(props);
			} catch (NamingException e) {
				throw new RuntimeException(e.getLocalizedMessage());
			}
		}

		return context;
	}
}

