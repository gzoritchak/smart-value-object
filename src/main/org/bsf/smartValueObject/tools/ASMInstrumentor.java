package org.bsf.smartValueObject.tools;

import org.objectweb.asm.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bsf.smartValueObject.tools.Instrumentor;
import org.bsf.smartValueObject.Versionable;
import org.bsf.smartValueObject.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.lang.reflect.Method;

/**
 * ASM specific implementation of Instrumentor. This code is ugly (mainly
 * due to ASM's low-levelness).
 *
 * @see org.bsf.smartValueObject.tools.Instrumentor
 * @see org.bsf.smartValueObject.tools.JavaAssistInstrumentor
 * @see <a href="http://asm.objectweb.org/">ASM homepage</a>
 */
public class ASMInstrumentor implements Instrumentor {
    private static Log log = LogFactory.getLog(ASMInstrumentor.class);
    private ClassWriter cw;
    private String className;

    public void modifyClass(String name) throws InstrumentorException {
        modifyClass(null, name);
    }

    public void modifyClass(String basedir, String name) throws InstrumentorException {
        String file;
        if (basedir != null) {
            file = basedir + File.separator + name;
        } else {
            file = name;
        }

        className = name
                .substring(0, name.length() - 6)
                .replace(File.separatorChar, '/');

        try {
            InputStream is = new FileInputStream(file);
            ClassReader cr = new ClassReader(is);
            cw = new ClassWriter(true);
            ClassVisitor cv = new SVOClassAdapter(className, cw);
            cr.accept(cv, true);
        } catch (Exception e) {
            throw new InstrumentorException(e);
        }

    }

    public byte[] getBytecode() throws InstrumentorException {
        if (cw == null) {
            throw new InstrumentorException();
        }
        return cw.toByteArray();
    }

    public Class defineClass() {
       throw new UnsupportedOperationException();
    }

    /**
     * Adapter to change class informations with ASM.
     */
    private class SVOClassAdapter extends ClassAdapter implements Constants {
        private Set methods = new HashSet();
        private boolean methodsCreated = false;
        private String internalName;

        public SVOClassAdapter(String className, ClassVisitor cv) {
            super(cv);
            this.internalName = className;
        }

        public void visit(int access, String name, String superName,
                          String[] interfaces, String srcfile) {
            log.debug("visit()");
            String[] newInterfaces;
            if (interfaces == null) {
                newInterfaces = new String[1];
            } else {
                newInterfaces = new String[interfaces.length + 1];
                System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
            }

            newInterfaces[newInterfaces.length - 1] = VERSIONINTERFACE.replace('.', '/');

            cv.visit(access, name, superName, newInterfaces, srcfile);
            createVersionableField(cv);
        }

        public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
            log.debug("visitMethod(" + s + ")");
            CodeVisitor mv = cv.visitMethod(i, s, s1, strings, attribute);
            return mv == null ? null : new SVOCodeAdapter(s, mv, this);
        }

        public void visitInnerClass(String s, String s1, String s2, int i) {
            log.debug("visitInnerClass()");
            if (!methodsCreated)
                createTrapMethods();
            cv.visitInnerClass(s, s1, s2, i);
        }

        public void visitAttribute(Attribute attribute) {
            log.debug("visitAttribute()");
            if (!methodsCreated)
                createTrapMethods();
            cv.visitAttribute(attribute);
        }

        public void visitEnd() {
            log.debug("visitEnd()");
            if (!methodsCreated)
                createTrapMethods();
            cv.visitEnd();
        }

        public void addMethod(MyMethod m) {
            log.debug("addMethod(" + m + ")");
            methods.add(m);
        }

        public String getInternalName() {
            return internalName;
        }

        private void createTrapMethods() {
            log.debug("createTrapMethods()");
            for (Iterator it = methods.iterator(); it.hasNext(); ) {
                MyMethod method = (MyMethod) it.next();
                CodeVisitor codevisitor = cv.visitMethod(
                        ACC_PRIVATE,                    /* access */
                        method.getName(),               /* name */
                        "(" + method.getType() + ")V",  /* descriptor */
                        null,                           /* exceptions */
                        null);                          /* attributes */

                createTrapMethod(method, codevisitor);
            }

            createVersionableMethods(Versionable.class, VERSIONFIELD);
            methodsCreated = true;
        }

        private void createTrapMethod(MyMethod m, CodeVisitor cv) {
            log.debug("createTrapMethod(" + m + ")");

            if (m.hasSmartContainer()) {
                createContainerTrap(m, cv);
                return;
            }

            // check if field was changed (primitive types and java.lang.*)
            if (m.getType().length() == 1 ||
                m.getTypeInternalName().startsWith("java/lang")) {
                Label fieldDifferent = new Label();
                Label fieldNull = new Label();

                // load field on operand stack
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(GETFIELD, getInternalName(), m.getField(), m.getType());

                if (m.getType().startsWith("L")) {
                    // reference type, invoke equals if field != null
                    cv.visitInsn(DUP);
                    cv.visitJumpInsn(IFNULL, fieldNull);
                    cv.visitVarInsn(ALOAD, 1);
                    cv.visitMethodInsn(INVOKEVIRTUAL, m.getTypeInternalName(), "equals", "(Ljava/lang/Object;)Z");
                    cv.visitJumpInsn(IFEQ, fieldDifferent);
                    // field is equal, return
                } else if (m.getType().startsWith("D")) {
                    cv.visitVarInsn(DLOAD, 1);
                    cv.visitInsn(DCMPL);
                    cv.visitJumpInsn(IFNE, fieldDifferent);
                } else if (m.getType().startsWith("F")) {
                    cv.visitVarInsn(FLOAD, 1);
                    cv.visitInsn(FCMPL);
                    cv.visitJumpInsn(IFNE, fieldDifferent);
                } else if (m.getType().startsWith("J")) {
                    cv.visitVarInsn(LLOAD, 1);
                    cv.visitInsn(LCMP);
                    cv.visitJumpInsn(IFNE, fieldDifferent);
                } else if (m.getType().startsWith("]")) {
                    cv.visitJumpInsn(GOTO, fieldDifferent);
                } else {
                    cv.visitVarInsn(ILOAD, 1);
                    cv.visitJumpInsn(IF_ICMPNE, fieldDifferent);
                }

                cv.visitInsn(RETURN);

                // our field is null
                cv.visitLabel(fieldNull);
                // so forget the field on the operand stack
                cv.visitInsn(POP);
                // ... and check if the parameter is != null
                cv.visitVarInsn(ALOAD, 1);
                cv.visitJumpInsn(IFNONNULL, fieldDifferent);

                cv.visitInsn(RETURN);

                cv.visitLabel(fieldDifferent);
            }



            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD,
                    getInternalName(),
                    VERSIONFIELD,
                    Type.getDescriptor(Versionable.class));

            // call version.touch(String fieldname)
            cv.visitLdcInsn(m.getField());
            cv.visitMethodInsn(INVOKEINTERFACE,
                    Type.getDescriptor(Versionable.class),
                    VERSIONMETHOD, "(Ljava/lang/String;)V");

            // set new field value
            cv.visitVarInsn(ALOAD, 0);
            String type = m.getType();
            if (type.startsWith("L") || type.startsWith("["))
                cv.visitVarInsn(ALOAD, 1);
            else if (type.startsWith("J"))
                cv.visitVarInsn(LLOAD, 1);
            else if (type.startsWith("D"))
                cv.visitVarInsn(DLOAD, 1);
            else if (type.startsWith("F"))
                cv.visitVarInsn(FLOAD, 1);
            else
                cv.visitVarInsn(ILOAD, 1);

            cv.visitFieldInsn(PUTFIELD,
                    getInternalName(),
                    m.getField(),
                    m.getType());
            cv.visitInsn(RETURN);
            cv.visitMaxs(4,2);
        }

        private void createContainerTrap(MyMethod m, CodeVisitor cv) {
            log.debug("createContainerTrap(" + m + ")");
            String typeClassName = m.getTypeClassName();
            String typeClassDesc = typeClassName.replace('.', '/');
            String replacement = SMARTCONTAINERS.getProperty(typeClassName);

            if (replacement == null) {
                log.debug("createContainerTrap: no replacement for " + typeClassName);
                return;
            }

            String replacementDesc = replacement.replace('.', '/');
            String versionableDesc = VERSIONINTERFACE.replace('.', '/');

            // create smart container
            cv.visitVarInsn(ALOAD, 0);
            cv.visitTypeInsn(NEW, replacementDesc);
            cv.visitInsn(DUP);

            // load parameter on stack
            cv.visitVarInsn(ALOAD, 1);

            // load versionable field on stack
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, getInternalName(), VERSIONFIELD, "L" + versionableDesc + ";");

            // invoke contructor
            cv.visitMethodInsn(INVOKESPECIAL,
                                replacementDesc,
                                "<init>",
                                "(L" + typeClassDesc + ";L" + versionableDesc + ";)V");

            // store new object in field
            cv.visitFieldInsn(PUTFIELD, getInternalName(), m.getField(), m.getType());
            cv.visitInsn(RETURN);
            cv.visitMaxs(4, 2);
        }

        private void createVersionableField(ClassVisitor cv) {
            log.debug("createVersionableField()");
            cv.visitField(
                    ACC_PRIVATE,                            /* access */
                    VERSIONFIELD,                           /* name */
                    Type.getDescriptor(Versionable.class),  /* descriptor */
                    null,                                   /* static value */
                    null);                                  /* attributes */
        }

        private void createVersionableMethods(Class clazz, String field) {
            log.debug("createVersionableMethods(" + clazz + ", " + field);
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                Class[] _exceptions = method.getExceptionTypes();
                String[] exceptions;
                if (_exceptions.length != 0) {
                    exceptions = new String[_exceptions.length];
                    for (int j = 0; j < exceptions.length; j++) {
                        Class exception = _exceptions[j];
                        exceptions[j] = Type.getDescriptor(exception);
                    }
                } else {
                    exceptions = null;
                }

                String desc = Type.getMethodDescriptor(method);

                // create method signature
                CodeVisitor codevisitor = cv.visitMethod(ACC_PUBLIC,
                    name, desc, exceptions, null);
                // load 'this'
                codevisitor.visitVarInsn(ALOAD, 0);
                // get version field
                codevisitor.visitFieldInsn(GETFIELD,
                        getInternalName(),
                        VERSIONFIELD,
                        Type.getDescriptor(Versionable.class));

                // load parameters on operand stack
                Class[] parameters = method.getParameterTypes();
                for (int j = 0; j < parameters.length; j++) {
                    Class parameter = parameters[j];
                    if (!parameter.isPrimitive()) {
                        codevisitor.visitVarInsn(ALOAD, 1+j);
                    } else if (parameter == Double.TYPE) {
                        codevisitor.visitVarInsn(DLOAD, 1+j);
                    } else if (parameter == Float.TYPE) {
                        codevisitor.visitVarInsn(FLOAD, 1+j);
                    } else if (parameter == Long.TYPE) {
                        codevisitor.visitVarInsn(LLOAD, 1+j);
                    } else {
                        codevisitor.visitVarInsn(ILOAD, 1+j);
                    }
                }

                // invoke method on version field
                codevisitor.visitMethodInsn(INVOKEINTERFACE,
                        Type.getDescriptor(Versionable.class),
                        name,
                        desc);

                // return result to caller
                Class returnType = method.getReturnType();
                if (returnType == Void.TYPE) {
                    codevisitor.visitInsn(RETURN);
                } else if (returnType.isPrimitive()) {
                    if ( returnType == Double.TYPE) {
                        codevisitor.visitInsn(DRETURN);
                    } else if ( returnType == Float.TYPE) {
                        codevisitor.visitInsn(FRETURN);
                    } else if ( returnType == Long.TYPE ) {
                        codevisitor.visitInsn(LRETURN);
                    } else {
                        codevisitor.visitInsn(IRETURN);
                    }
                }  else {
                    codevisitor.visitInsn(ARETURN);
                }

                codevisitor.visitMaxs(1 + parameters.length, 1 + parameters.length);
            }
        }
    }

    /**
     * Adapter to change the bytecode with ASM.
     */
    private class SVOCodeAdapter extends CodeAdapter implements Constants {
        private SVOClassAdapter ca;
        private boolean isConstructor;
        private boolean isInitialized;
        private String methodName;

        public SVOCodeAdapter(String methodName, CodeVisitor cv, SVOClassAdapter ca) {
            super(cv);
            this.ca =ca;
            this.methodName = methodName;
            isConstructor = methodName.startsWith("<init>");
            isInitialized = false;
        }

        public void visitFieldInsn(int i, String s, String s1, String s2) {
            if (i == PUTFIELD) {
                log.debug("visitFieldInsn(" + i + "," + s + "," + s1 + "," + s2 + ")");
                ca.addMethod(new MyMethod(methodName(s1), s1, s2));
                log.debug("visitMethodInsn(INVOKEVIRTUAL, " + className + "," +  methodName(s1) + ", (" + s2 + ")V" + ")");
                cv.visitMethodInsn(INVOKEVIRTUAL, className, methodName(s1), "(" + s2 + ")V");
            } else {
                cv.visitFieldInsn(i, s, s1, s2);
            }
        }

        public void visitMethodInsn(int i, String s, String s1, String s2) {
            // initialize the version field as soon as possible
            // right after invoking the baseclass constructor
            if (isConstructor && i == INVOKESPECIAL && !isInitialized) {
                cv.visitMethodInsn(i, s, s1, s2);
                initVersionable();
            } else {
                super.visitMethodInsn(i, s, s1, s2);
            }
        }

        private String methodName(String field) {
            return "write_" + field;
        }

        private void initVersionable() {
            String versionDesc = Type.getDescriptor(Version.class);
            String versionableDesc = Type.getDescriptor(Versionable.class);
            String ownerDesc = ca.getInternalName();

            cv.visitVarInsn(ALOAD, 0);
            cv.visitTypeInsn(NEW, versionDesc);
            cv.visitInsn(DUP);
            cv.visitMethodInsn(INVOKESPECIAL, versionDesc, "<init>", "()V");
            cv.visitFieldInsn(PUTFIELD,
                    ownerDesc,
                    VERSIONFIELD,
                    versionableDesc);
            isInitialized = true;
        }
    }

    /**
     * Representation of a method.
     */
    private static class MyMethod {
        private String name, field, type;
        public MyMethod(String name, String field, String type) {
            this.name = name;
            this.field = field;
            this.type = type;
        }

        public String getName()  { return name; }
        public String getType()  { return type; }
        public String getField() { return field; }
        public String toString() { return "void " + name + "(" + type + " " + field + ")"; }

        public String getTypeInternalName() {
            if (!type.startsWith("L")) {
                return null;
            }
            return type.substring(1, type.length()-1);
        }

        public String getTypeClassName() {
            String internalName = getTypeInternalName();
            if (internalName == null)
                return null;
            else
                return internalName.replace('/', '.');
        }

        public boolean hasSmartContainer() {
            String className = getTypeClassName();
            if (className == null)
                return false;
            else
                return (SMARTCONTAINERS.containsKey(className));
        }

        public boolean equals(Object o) {
           if (o == null || ! (o instanceof MyMethod))
             return false;

           MyMethod m = (MyMethod) o;
           return (m.getName().equals(this.name) &&
                m.getType().equals(this.type) &&
                m.getField().equals(this.field));
        }

        public int hashCode() {
            return 42 +
                    this.name.hashCode() +
                    this.field.hashCode() +
                    this.type.hashCode();
        }
    }
}
