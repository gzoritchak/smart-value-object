package org.bsf.smartValueObject;

/**
 * Concrete default implementation of <tt>Versionable</tt>.
 * <p>Versioning is done with timestamping.
 */
public class Version implements Versionable  {
    private long timestamp;
    private boolean dirty;
    private boolean created;
    private boolean deleted;
    private final boolean debug = false;

    /**
     * Creates a clean version. Timestamp is set to current time.
     */
    public Version() {
        markClean();
        create(); // consider new objects as created
    }

    /**
     * No specific purpose constructor, needed to satisfy a javassist requirement.
     * @param o ignored
     */
    public Version(Object o) {
        this();
    }

    public void touch() {
        touch("unknown");
    }

    public void touch(String field) {
        if (debug)
        System.out.println("touched (" + field + ") @ " +
                this.timestamp);
        dirty = true;
    }

    public void delete() {
        deleted = true;
        created = false;
        dirty = true;
    }

    public void create() {
        deleted = false;
        created = true;
        dirty = true;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        deleted = dirty = created = false;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the version number.
     */
    public long getVersionId() {
        return timestamp;
    }

    /**
     * Sets the version number
     */
    public void setVersionId(long id) {
        this.timestamp = id;
    }

    public String toString() {
        return "Version [timestamp: " + this.timestamp +
                " dirty:"   + (dirty   ? "yes" : "no") +
                " created:" + (created ? "yes" : "no") +
                " deleted:" + (deleted ? "yes" : "no") +
                "]";
    }
}

