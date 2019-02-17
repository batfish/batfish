package org.batfish.datamodel;

/**
 * Interface for classes that contain an instance of an {@link AbstractRoute}.
 *
 * <p><i>Note:</i> This class implements {@link Comparable} because we put HasAbstractRoute in
 * ordered collections all throughout the codebase. {@link #compareTo(HasAbstractRoute)} has
 * <b>NO</b> impact on route preference.
 */
public interface HasAbstractRoute extends Comparable<HasAbstractRoute> {
  AbstractRoute getAbstractRoute();

  Prefix getNetwork();

  @Override
  int compareTo(HasAbstractRoute o);
}
