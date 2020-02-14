package org.batfish.datamodel;

import org.batfish.common.ip.Prefix;

/** Interface for classes that contain an instance of an {@link AbstractRoute}. */
public interface AbstractRouteDecorator {
  AbstractRoute getAbstractRoute();

  Prefix getNetwork();
}
