package org.batfish.datamodel;

/** Interface for classes that contain an instance of an {@link AbstractRoute}. */
public interface AbstractRouteDecorator {
  AbstractRoute getAbstractRoute();

  Prefix getNetwork();
}
