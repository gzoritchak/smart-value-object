package org.bsf.smartValueObject.tools;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.bsf.smartValueObject.tools.Instrumentor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Ant task to do bytecode modification on compile time. Relies on
 * a concrete implementation of <tt>Instrumentor</tt>.
 * <p><blockquote><pre>
 * &lt;smartify instrumentor="org.bsf.smartValueObject.tools.JavaAssistInstrumentor"&gt;
 *      &lt;fileset dir="${build.dir}/test"&gt;
 *               &lt;include name="*VO.class"/&gt;
 *      &lt;/fileset&gt;
 * &lt;/smartify&gt;
 * </pre></blockquote>
 */
public class SmartTask extends org.apache.tools.ant.taskdefs.MatchingTask {
    private Vector filesets = new Vector();
    /** The classname of the instrumentor to use. */
    private String instrumentor =
            "org.bsf.smartValueObject.tools.JavaAssistInstrumentor";

    public void addFileset(FileSet f) {
        filesets.addElement(f);
    }

    /**
     * Name of a class implementing
     * <tt>org.bsf.smartValueObject.tools.Instrumentor</tt>.
     * @param s implementation to use.
     * @see org.bsf.smartValueObject.tools.Instrumentor
     */
    public void setInstrumentor(String s) {
        this.instrumentor = s;
    }

    public String getInstrumentor() {
        return this.instrumentor;
    }

    public void execute() throws BuildException {
        if (filesets.size() == 0) {
            throw new BuildException("Need a fileset!");
        }

        Enumeration e = filesets.elements();
        while (e.hasMoreElements()) {
            FileSet fs = (FileSet) e.nextElement();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();

            smartify(ds.getBasedir().getAbsolutePath(), files);
        }
    }

    /**
     * Smartify all files. Gets called by <code>execute()</code>.
     *
     * @param basedir relative directory
     * @param files list of files as specified by the fileset-tag.
     */
    private void smartify(String basedir, String[] files) {
        Instrumentor instrumentor = getInstrumentorInstance();
        log("SmartTask: using " + getInstrumentor() + " to instrument classes");
        for (int i = 0; i < files.length; i++) {
            String file = files[i];

            log("SmartTask: smartify " + file, Project.MSG_INFO);

            try {
                instrumentor.modifyClass(basedir, file);
            } catch (InstrumentorException e) {
                throw new BuildException(e.getLocalizedMessage(), e);
            }

            try {
                byte[] bytecode = instrumentor.getBytecode();
                if (bytecode.length == 0)
                    continue;
                FileOutputStream fos = new FileOutputStream(
                        new File(basedir + File.separator + file));
                fos.write(bytecode);
                fos.close();
            } catch (IOException e) {
                throw new BuildException(e.getLocalizedMessage(), e);
            } catch (InstrumentorException e) {
                throw new BuildException(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Gets an concrete instrumentor instance.
     */
    private Instrumentor getInstrumentorInstance() {
        Instrumentor instrumentor;
        try {
            instrumentor = (Instrumentor)
                    Class.forName(getInstrumentor()).newInstance();
        } catch (Exception e) {
            throw new BuildException("Error while instantiating " +
                    "instrumentor " + getInstrumentor(), e);
        }
        return instrumentor;
    }
}
