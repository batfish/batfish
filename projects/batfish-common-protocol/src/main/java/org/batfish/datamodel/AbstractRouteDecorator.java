package org.batfish.datamodel;

/**
 * Interface for classes that contain an instance of an {@link AbstractRoute}.
 *
 * <p><i>Note:</i> This class implements {@link Comparable} because we put AbstractRouteDecorator in
 * ordered collections all throughout the codebase.
 */
public interface AbstractRouteDecorator {
  AbstractRoute getAbstractRoute();

  Prefix getNetwork();
}
