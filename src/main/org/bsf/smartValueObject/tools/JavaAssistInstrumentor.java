package org.bsf.smartValueObject.tools;

import javassist.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

/**
 * Javassist specific implementation.
 * <p>This class makes heavy use of advanced javassist features
 * like runtime compilation.
 *
 * @see org.bsf.smartValueObject.tools.Instrumentor
 * @see <a href="http://www.csg.is.titech.ac.jp/~chiba/javassist/">Javassist homepage</a>
 */
public class JavaAssistInstrumentor implements Instrumentor {
    private static final Log log = LogFactory.getLog(JavaAssistInstrumentor.class);
    /** Default pool to obtain CtClasses from. */
    private static final ClassPool pool;
    /** The codeconverter to be used to change field access. */
    private static final CodeConverter converter = new CodeConverter();
    /** Custom classloader to define classes at runtime. */
    private static InstClassLoader instCL = new InstClassLoader(JavaAssistInstrumentor.class.getClassLoader());
    /** A The modified class in javassist's representation. */
    private CtClass ctclass = null;
    /** The trap method to be used for field access. */
    private CtMethod standardTrap = null;

    static {
        // default classpool = java.lang.Object.class.getClassLoader()
        pool = ClassPool.getDefault();
        pool.insertClassPath(new LoaderClassPath(JavaAssistInstrumentor.class.getClassLoader()));
    }

    /**
     * Creates new instance. Use <tt>modifyClass()</tt> to do the
     * actual modification.
     */
    public JavaAssistInstrumentor() {
    }

    /**
     * The instrumentor is initialized by this constructor.
     * @param name class to be modified, either as path or in package notation.
     * @throws org.bsf.smartValueObject.tools.InstrumentorException when encountering problems while loading/
     * modifying the class.
     */
    public JavaAssistInstrumentor(String name) throws InstrumentorException {
        modifyClass(name);
    }

    /**
     * Added for convenience.
     * @param clazz to be modified
     * @throws org.bsf.smartValueObject.tools.InstrumentorException
     */
    public JavaAssistInstrumentor(Class clazz) throws InstrumentorException {
        this(clazz.getName());
    }

    public void modifyClass(String name) throws InstrumentorException {
        if (name.endsWith(".class")) {
            name = fileToClass(name);
        }

        try {
            ctclass = pool.get(name);
            if (alreadyModified(ctclass)) {
                return;
            } else {
                modifyClass(ctclass);
            }
        } catch (Exception e) {
            throw new InstrumentorException("JavaAssistInstrumentor: error while transforming", e);
        }
    }

    public void modifyClass(String basedir, String file) throws InstrumentorException {
        try {
            FileInputStream fis = new FileInputStream(new File(basedir, file));
            byte[] bytecode = readStream(fis);
            pool.insertClassPath(new ByteArrayClassPath(fileToClass(file), bytecode));
            pool.appendClassPath(basedir);
        } catch (NotFoundException e) {
            throw new InstrumentorException(e);
        }  catch (IOException e) {
            throw new InstrumentorException(e);
        }
        modifyClass(file);
    }

    public byte[] getBytecode() throws InstrumentorException {
        if (ctclass == null) throw new IllegalStateException("use modifyClass first");
        try {
            return ctclass.toBytecode();
        } catch (Exception e) {
            throw new InstrumentorException(e);
        }
    }

    public Class defineClass() {
        return instCL.loadClass(ctclass);
    }

    /**
     * Applies all necessary modifications to make class versionable.
     * @param cc class to be modified.
     * @return modified class.
     */
    private CtClass modifyClass(CtClass cc) throws InstrumentorException {
        log.debug("modifyClass: " + cc);

        try {
            standardTrap = createTrapWrite(cc);
            addFieldInterceptors(cc);
            makeFieldsPublic(cc);
            makeVersionable(cc);
            cc.instrument(converter);
        } catch (Exception e) {
            log.warn("exeception while modifying", e);
            throw new InstrumentorException(e);
        }

        return cc;
    }

    /**
     * Makes fields public.
     * @param cc
     */
    private void makeFieldsPublic(CtClass cc) {
        CtField[] fields = cc.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            CtField field = fields[i];
            if ((field.getModifiers() & Modifier.STATIC) == 0)
                field.setModifiers(Modifier.PUBLIC);
        }
    }

    /**
     * Makes class versionable.
     * Adds version field, implements VERSIONINTERFACE by delegating
     * all the methods to it.
     *
     * @see org.bsf.smartValueObject.tools.Instrumentor#VERSIONINTERFACE
     * @see org.bsf.smartValueObject.tools.Instrumentor#VERSIONFIELD
     */
    private void makeVersionable(CtClass cc)
            throws CannotCompileException, NotFoundException, InstrumentorException {
        CtField versionField = addVersionField(cc);
        CtClass versionInterface = pool.get(VERSIONINTERFACE);
        addDelegations(versionInterface, versionField, cc);
        cc.addInterface(versionInterface);
    }

    /**
     * Generic method to implement an interface by delegation.
     * <p>E.g. <code>declaring.isDirty()</code> ==>
     * <code>declaring.field.isDirty()</code>.
     *
     * @param iface interface to implement.
     * @param field field to delegate to.
     * @param declaring class to add interface to.
     */
    private void addDelegations(CtClass iface, CtField field, CtClass declaring)
            throws InstrumentorException, NotFoundException, CannotCompileException {

        if (!iface.isInterface()) {
            throw new InstrumentorException("need Interface");
        }
        if (!field.getType().subtypeOf(iface)) {
            throw new InstrumentorException("field doesn't implement interface");
        }

        // use all methods as declared by the interface
        CtMethod[] methods = iface.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            StringBuffer body = new StringBuffer();
            if (method.getReturnType() != CtClass.voidType) {
                body.append("return ");
            }

            // ($$) javassist specific macro (expanded to parameters)
            // field.methodname(1st parameter, 2nd parameter...)
            body.append(field.getName() + (".") + method.getName() + "($$);");

            // make new method using the signature of the interface's method
            // and the body defined above, add it to class
            CtMethod newMethod = CtNewMethod.make(
                    method.getReturnType(),
                    method.getName(),
                    method.getParameterTypes(),
                    method.getExceptionTypes(),
                    body.toString(),
                    declaring);
            declaring.addMethod(newMethod);
        }
    }

    /** Adds interceptors to all fields declared in cc. */
    private void addFieldInterceptors(CtClass cc)
            throws NotFoundException, CannotCompileException {
        CtField[] fields = cc.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }

            addFieldInterceptor(fields[i], SMARTCONTAINERS);
        }
    }

    /**
     * Adds an interceptor  to a field (for write access).
     * Implemented by 'wrapping around' a trap method for type safety.
     * Because field interception can only be intercepted by calling
     * a static method we need to implement a static wrapper called 'trap'
     * which will finally do an interception on the object itself.
     *
     * <p>write access to field foo of type Bar will result in:
     * <code>static write_foo(Object o, Bar bar) { trap_method };</code>
     *
     * @param field field to be intercepted.
     * @param ifaces interfaces with their 'smart' replacements.
     */
    private void addFieldInterceptor(CtField field, Properties ifaces)
            throws NotFoundException, CannotCompileException {
        String name = field.getName();
        CtClass cc = field.getDeclaringClass();
        log.debug("addFieldInterceptor: " + name);

        CtMethod trap;
        String fieldtype = field.getType().getName();
        String replacement = ifaces.getProperty(fieldtype);

        // create special traps if we assign to interfaces with 'smart'
        // replacements
        if (replacement != null) {
            trap = createTrapWriteGeneric(cc, fieldtype, replacement);
        } else {
            // else use standard trap
            trap = standardTrap;
        }

        CtClass[] writeParam = new CtClass[2];
        writeParam[0] = pool.get("java.lang.Object");
        writeParam[1] = field.getType();
        CtMethod method = CtNewMethod.wrapped(
                CtClass.voidType, // return type
                fieldWrite(name), // name of method
                writeParam, // params
                null, // execeptions
                trap, // body
                CtMethod.ConstParameter.string(name), // const parameter (String/int/null)
                cc);                // declaring class
        // needed because replaceFieldWrite dispatches
        // only to static methods
        method.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        cc.addMethod(method);

        converter.replaceFieldWrite(field, cc, fieldWrite(name));
    }

    /** Convention to name methods. */
    private static String fieldWrite(String name) {
        return "write_" + name;
    }

    /**
     * Adds a version field to the class.
     * @param cc target class.
     * @return the version field.
     * @throws javassist.CannotCompileException
     * @throws javassist.NotFoundException
     * @see org.bsf.smartValueObject.tools.Instrumentor#VERSIONFIELD
     * @see #createVersionField(javassist.CtClass declaring)
     */
    private CtField addVersionField(CtClass cc)
            throws CannotCompileException, NotFoundException {
        log.debug("addVersionField: " + cc);
        CtField field;

        field = createVersionField(cc);
        // add field to class, it will be initialised by 'new' on runtime
        // e.g. Version version = new Version();
        cc.addField(field, CtField.Initializer.byNew(pool.get(VERSIONCLASS)));

        return field;
    }

    /**
     * Creates the version field.
     *
     * @see #addVersionField(javassist.CtClass cc)
     */
    private CtField createVersionField(CtClass declaring)
            throws CannotCompileException, NotFoundException {
        String name = VERSIONFIELD;
        CtClass type = pool.get(VERSIONCLASS);
        CtField field = new CtField(type, name, declaring);
        field.setModifiers(Modifier.PUBLIC);

        return field;
    }

    /**
     * Creates a 'trap' for interception.
     * The created 'trap' will call VERSIONMETHOD (e.g. 'touch')
     * on the object and set the field using reflection. In case of fields in
     * the java.lang.* package or primitive types we do an invocation of the
     * equals method to verify if a real change has taken place or if the field
     * already contains the value. In this case the object will not be
     * marked as 'dirty'.
     *
     * @param cc target class.
     * @return trap method.
     * @see #addFieldInterceptor
     */
    private CtMethod createTrapWrite(CtClass cc)
            throws CannotCompileException {
        String classname = cc.getName();

        String body =
                "protected static Object trapWrite(Object[] args, String name) {" +
                classname + " foo = (" + classname + ") args[0];" +
                "try {" +
                "   java.lang.reflect.Field field = foo.getClass().getField(name);" +
                "   if (" + VERSIONHELPER + ".doEquals(field)) {" +
                "       if (field.get(foo) != null && field.get(foo).equals(args[1]))" +
                "           return null;" +
                "       else if (args[1] == null)" +
                "           return null;" +
                "   }" +
                "   field.set(foo, args[1]); " +
                "   ((" + VERSIONINTERFACE + ")foo)." + VERSIONMETHOD + "(field.getName());" +
                "} catch (Throwable t) { throw new RuntimeException(t); } " +
                "return null;" +
                "}";

        log.debug(body);
        return CtNewMethod.make(body, cc);
    }

    /**
     * To replace assignment to specific interfaces by a wrapped version.
     * This allows for Collections doing versionable transactions (remove,...).
     * Assignments to fields of type 'dumb' are replaced by <code>new SmartXXX(dumb,
     * versionable)</code>.
     * <p>By using 'versionable' as the second parameter, the newly created object gets
     * a reference to the version state of its parent.
     *
     * @param cc target class.
     * @param dumb package name of class to replace.
     * @param smart package name of replacing class.
     * @return trap method.
     */
    private CtMethod createTrapWriteGeneric(CtClass cc, String dumb, String smart)
            throws CannotCompileException {
        String classname = cc.getName();
        int lastDot = dumb.lastIndexOf('.');
        String suffix;
        if (lastDot != -1) {
            suffix = dumb.substring(lastDot + 1);
        } else {
            suffix = dumb;
        }

        String body =
                "protected static Object trapWrite" + suffix + "(Object[] args, String name) {" +
                classname + " foo = (" + classname + ") args[0];" +
                "try { " +
                "   java.lang.reflect.Field field = foo.getClass().getField(name);" +
                dumb + " o = new " + smart +
                "    ((" + dumb + ") args[1]," +
                // using version object of parent
                // "    (" + VERSIONINTERFACE + ") foo);" +
                // use new version object
                "    new " + VERSIONCLASS + "());" +
                "   field.set(foo, o); " +
                "} catch (Throwable t) {} " +
                "return null;" +
                "}";

        log.debug(body);
        return CtNewMethod.make(body, cc);
    }

    // --------------------------------------------------------------------------
    /**
     * Custom ClassLoader using <tt>CtClass</tt>. This ClassLoader can be used
     * to define modified classes. It exists mainly for testing purposes.
     */
    private static class InstClassLoader extends ClassLoader {
        /**
         * Creates new ClassLoader.
         * @param c the parent classloader.
         */
        public InstClassLoader(ClassLoader c) {
            super(c);
        }

        public Class loadClass(CtClass cc) throws ClassFormatError {
            byte[] bytecode;
            try {
                bytecode = cc.toBytecode();
            } catch (Exception e) {
                throw new ClassFormatError(e.getMessage());
            }

            return loadClass(cc.getName(), bytecode);
        }

        public Class loadClass(String name, byte[] bytecode) throws ClassFormatError {
            Class c = defineClass(name, bytecode, 0, bytecode.length);
            resolveClass(c);
            return c;
        }

        public Class loadAndDefine(String name) throws
                ClassNotFoundException {
            String cname = name.replace('.', '/') + ".class";
            InputStream ins = getResourceAsStream(cname);
            if (ins == null) {
                throw new ClassNotFoundException();
            }

            byte[] bytecode;
            try {
                bytecode = readStream(ins);
            } catch (IOException e) {
                throw new ClassFormatError(e.getMessage());
            }

            return loadClass(name, bytecode);
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
        }
    }

    /** Helper method to read inputstream in byte array. */
    private static byte[] readStream(InputStream fin) throws IOException {
         byte[][] bufs = new byte[8][];
         int bufsize = 4096;

         for (int i = 0; i < 8; ++i) {
             bufs[i] = new byte[bufsize];
             int size = 0;
             int len;
             do {
                 len = fin.read(bufs[i], size, bufsize - size);
                 if (len >= 0)
                     size += len;
                 else {
                     byte[] result = new byte[bufsize - 4096 + size];
                     int s = 0;
                     for (int j = 0; j < i; ++j) {
                         System.arraycopy(bufs[j], 0, result, s, s + 4096);
                         s = s + s + 4096;
                     }

                     System.arraycopy(bufs[i], 0, result, s, size);
                     return result;
                 }
             } while (size < bufsize);
             bufsize *= 2;
         }

         throw new IOException("too much data");
    }

    private static String fileToClass(String filename) {
        filename = filename.
                replace(File.separatorChar, '.').
                substring(0, filename.length() - 6);
        return filename;
    }

    /** Prevents class from being instrumented twice. */
    private static boolean alreadyModified(CtClass ctclass) throws NotFoundException {
        CtClass[] ifaces = ctclass.getInterfaces();
        for (int i = 0; i < ifaces.length; i++) {
            CtClass iface = ifaces[i];
            if (iface.getName().equals(VERSIONINTERFACE))
                return true;
        }
        return false;
    }
}
