package org.bsf.smartValueObject;

/**
 * A helper class providing implementations for some common filters.
 *
 * @see org.bsf.smartValueObject.VersionableFilter
 * @see org.bsf.smartValueObject.Versionable
 */
public class VersionableFilters {
    /**
     * No filter, get all objects.
     */
    public static final VersionableFilter ALL =
        new VersionableFilter() {
            public boolean accept(Versionable v) {
                return true;
            }
        };

    /**
     * Get only deleted objects.
     */
    public static final VersionableFilter DELETED =
        new VersionableFilter() {
            public boolean accept(Versionable v) {
                return v.isDeleted();
            }
        };

    /**
     * Get newly created objects.
     */
    public static final VersionableFilter CREATED =
        new VersionableFilter() {
            public boolean accept(Versionable v) {
                return v.isCreated() && !v.isDeleted();
            }
        };

    /**
     * Get object that actually exist.
     */
    public static final VersionableFilter EXISTING =
          new VersionableFilter() {
            public boolean accept(Versionable v) {
                return !v.isDeleted();
            }
        };

    /**
     * Get modified (dirty) objects.
     */
    public static final VersionableFilter DIRTY =
        new VersionableFilter() {
            public boolean accept(Versionable v) {
                return v.isDirty();
            }
        };

    /**
     * Gets smart container objects.
     */
    public static final VersionableFilter SMARTCONTAINER =
        new VersionableFilter() {
            public boolean accept(Versionable v) {
                return SmartAccess.isSmartContainer(v);
            }
        };
}
