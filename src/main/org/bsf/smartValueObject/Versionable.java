package org.bsf.smartValueObject;

/**
 * Required interface for an object to be versionable.
 *
 * @see org.bsf.smartValueObject.Version
 */
public interface Versionable extends java.io.Serializable {
    /**
     * Touches the object. Signals that a field has been written to.
     */
    void touch();

    /**
     * Touches the object, detailling which field has been accessed.
     * @param field which was touched
     */
    void touch(String field);

    /**
     * Mark object for deletion.
     */
    void delete();

    /**
     * Mark object as created.
     */
    void create();

    /**
     * Has object been newly created ?
     */
    boolean isCreated();

    /**
     * Has object been deleted ?
     */
    boolean isDeleted();

    /**
     * Is object dirty ?
     */
    boolean isDirty();

    /**
     * Resets flags.
     */
    void markClean();

    /**
     * Gets the version number.
     */
    long getVersionId();

    /**
     * Sets the version number
     */
    void setVersionId(long id);
}
