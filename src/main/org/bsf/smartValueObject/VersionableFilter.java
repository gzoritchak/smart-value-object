package org.bsf.smartValueObject;

/**
 * Filter objects based on their version state.
 *
 * @see org.bsf.smartValueObject.VersionableFilters
 * @see org.bsf.smartValueObject.Versionable
 */
public interface VersionableFilter {
    /**
     * Tests wether or not the specified object implementing
     * <tt>Versionable</tt> is accepted.
     * @param v object to test.
     * @return
     */
    boolean accept(Versionable v);
}
