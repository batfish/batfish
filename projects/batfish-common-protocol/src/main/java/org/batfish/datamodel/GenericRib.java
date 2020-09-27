package org.batfish.datamodel;

public interface GenericRib<R extends AbstractRouteDecorator> extends GenericRibReadOnly<R> {

  /**
   * Compare the preferability of one route with anther
   *
   * @param lhs 1st route with which to compare preference
   * @param rhs 2nd route with which to compare preference
   * @return -1 if lhs route is less preferable than rhs; 0 if lhs route and rhs are equally
   *     preferable (i.e. for multipath routing); 1 if lhs route is strictly more preferred than rhs
   */
  int comparePreference(R lhs, R rhs);

  boolean mergeRoute(R route);
}
