package org.bsf.smartValueObject.tools;

import java.util.Properties;

/**
 * Minimal set of methods for instrumenting classes.
 * <p>A Class implementing instrumentor is seen
 * as a "black-box" which makes passed in classes versionable and
 * hands out the bytecode needed.
 * <p>The general idea is to be independent of the underlying implementation.
 */
public interface Instrumentor {
    /** Interface to be used for versionable objects. */
    String VERSIONINTERFACE = "org.bsf.smartValueObject.Versionable";
    /** Default implementation for VERSIONINTERFACE. */
    String VERSIONCLASS     = "org.bsf.smartValueObject.Version";
    /** The helper class used by instrumentors + TOs (on runtime) */
    String VERSIONHELPER    = "org.bsf.smartValueObject.VersionHelper";
    /** Name of the field in versionable classes. */
    String VERSIONFIELD     = "version";
    /** Method to call upon field write access. */
    String VERSIONMETHOD    = "touch";
    /** To verify is object has been altered. */
    String DIRTYMETHOD      = "isDirty";
    /** Method to clean flags. */
    String CLEANMETHOD      = "markClean";
    /** To verify if object is marked for deletion. */
    String DELETEDMETHOD    = "isDeleted";
    /** To verify if object has newly created. */
    String CREATEDMETHOD    = "isCreated";
    /** Mark object for deletion. */
    String DELETEMETHOD     = "delete";
    /** Mark object as freshly created. */
    String CREATEMETHOD     = "create";

    /** A replacement for <tt>java.util.Collection</tt>. */
    String SMARTCOLLECTION  = "org.bsf.smartValueObject.container.SmartCollection";
    /** A replacement for <tt>java.util.Map</tt>. */
    String SMARTMAP         = "org.bsf.smartValueObject.container.SmartMap";
    /** A replacement for <tt>java.util.List</tt>. */
    String SMARTLIST         = "org.bsf.smartValueObject.container.SmartList";
    /** A replacement for <tt>java.util.Set</tt>. */
    String SMARTSET          = "org.bsf.smartValueObject.container.SmartSet";

    /** A map containing container classes and their smart replacements. */
    Properties SMARTCONTAINERS = SmartReplacements.containerReplacementProps;

    /**
     * Modifies this class.
     *
     * @param name class to modify, package notation or filename.
     * @throws org.bsf.smartValueObject.tools.InstrumentorException in case of errors
     */
    void modifyClass(String name) throws InstrumentorException;
    void modifyClass(String basedir, String file) throws InstrumentorException;

    /**
     * Get modified class as byte array.
     *
     * @return versionable class as bytecode.
     * @throws org.bsf.smartValueObject.tools.InstrumentorException
     */
    byte[] getBytecode() throws InstrumentorException;

    /**
     * Use internal classloader to build class object.
     * <p>Exists rather for testing purposes, as classes won't be compatible !
     *
     *  @return Versionable class.
     */
    Class defineClass();

    public static class SmartReplacements {
        private static final Properties containerReplacementProps = new Properties();
        static final String[][] containerReplacements = {
          { "java.util.Collection",   SMARTCOLLECTION },
          { "java.util.Map",          SMARTMAP },
          { "java.util.List",         SMARTLIST },
          { "java.util.Set",          SMARTSET }
        };

        static {
            for (int i = 0; i < containerReplacements.length; i++) {
                String[] containerReplacement = containerReplacements[i];
                containerReplacementProps.setProperty(
                    containerReplacement[0],
                    containerReplacement[1]);
            }
        }
    }
}
