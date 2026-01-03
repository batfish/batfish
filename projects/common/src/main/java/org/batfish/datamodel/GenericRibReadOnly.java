package org.batfish.datamodel;

import java.util.Set;
import javax.annotation.Nonnull;

public interface GenericRibReadOnly<R extends AbstractRouteDecorator> {

  /** Check whether a given route is present in the RIB */
  boolean containsRoute(AbstractRouteDecorator route);

  /** Return set of {@link AbstractRoute abstract routes} this RIB contains. */
  Set<AbstractRoute> getUnannotatedRoutes();

  /** Return set of routes stored for this exact prefix, if any. */
  Set<R> getRoutes(Prefix prefix);

  /** Return set of {@link R typed routes} this RIB contains. */
  Set<R> getRoutes();

  /** Return set of backup {@link R typed routes} this RIB contains. */
  Set<R> getBackupRoutes();

  /**
   * Execute the longest prefix match for a given IP address.
   *
   * <p><strong>Note</strong>: this function returns only forwarding routes, aka, routes where
   * {@link AbstractRoute#getNonForwarding()} returns false.
   *
   * @param address the IP address to match
   * @param restriction A predicate restricting which routes may be returned. Note that in general
   *     this may shorten the longest prefix.
   * @return a set of routes with the maximum allowable prefix length that match the {@code address}
   */
  @Nonnull
  Set<R> longestPrefixMatch(Ip address, ResolutionRestriction<R> restriction);

  /**
   * Execute a constrained longest prefix match for a given IP address.
   *
   * <p><strong>Note</strong>: this function returns only forwarding routes, aka, routes where
   * {@link AbstractRoute#getNonForwarding()} returns false.
   *
   * <p>Most callers should use {@link #longestPrefixMatch(Ip, ResolutionRestriction)}; this
   * function may be used when the longest prefix matches are unsatisfactory and less specific
   * routes are required.
   *
   * @param address the IP address to match
   * @param maxPrefixLength the maximum prefix length allowed (i.e., do not match more specific
   *     routes). This is a less than or equal constraint.
   * @param restriction A predicate restricting which routes may be returned. Note that in general
   *     this may shorten the longest prefix.
   * @return a set of routes that match the {@code address} given the constraint.
   */
  @Nonnull
  Set<R> longestPrefixMatch(Ip address, int maxPrefixLength, ResolutionRestriction<R> restriction);

  /**
   * Compare the preferability of one route with anther
   *
   * @param lhs 1st route with which to compare preference
   * @param rhs 2nd route with which to compare preference
   * @return -1 if lhs route is less preferable than rhs; 0 if lhs route and rhs are equally
   *     preferable (i.e. for multipath routing); 1 if lhs route is strictly more preferred than rhs
   */
  int comparePreference(R lhs, R rhs);

  /**
   * Returns {@code true} iff there is any intersection between the space of all the prefixes
   * belonging to routes in this rib (that would be returned by {@link #getRoutes}) and the provided
   * {@code prefixSpace}.
   */
  boolean intersectsPrefixSpace(PrefixSpace prefixSpace);
}
