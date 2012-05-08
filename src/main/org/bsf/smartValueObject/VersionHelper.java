package org.bsf.smartValueObject;

import java.lang.reflect.Field;

/**
 * Helper class to be used by versionable objects. Used
 * to refactor as much code as possible from bytecode modification to this
 * class to allow for easy customization.
 */
public class VersionHelper {

    /**
     * Checks if the equals method should be performed before writing
     * to a field. This is used to detect modifications which don't change
     * the state of the object.
     *
     *  Because we can't rely on user-provided equals implementations,
     * we only allow certain classes from the java.* hierarchy and the primitive types.
     *
     * @param field
     * @return
     */
    public static boolean doEquals(Field field) {
        String type = field.getType().getName();
        return (type.startsWith("java.lang.") ||
                type.equals("java.util.Date") ||
                type.equals("java.math.BigDecimal") ||
                type.equals("java.math.BigInteger") ||
                field.getType().isPrimitive());
    }
}
