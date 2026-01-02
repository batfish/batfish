package org.batfish.datamodel;

public interface GenericRib<R extends AbstractRouteDecorator> extends GenericRibReadOnly<R> {

  /**
   * Add a route to this RIB.
   *
   * @param route route to add
   * @return true if the route was merged, or false if it was discarded (e.g., a better route
   *     already exists)
   */
  boolean mergeRoute(R route);
}
