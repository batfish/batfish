package org.batfish.common;

/**
 * BGP Well-known Communities
 *
 * <p>See IANA page:
 * https://www.iana.org/assignments/bgp-well-known-communities/bgp-well-known-communities.xml
 */
public final class WellKnownCommunity {
  public static final long INTERNET = 0L;
  public static final long GRACEFUL_SHUTDOWN = 0xFFFF_0000L;
  public static final long ACCEPT_OWN = 0xFFFF_0001L;
  public static final long ROUTE_FILTER_TRANSLATED_V4 = 0xFFFF_0002L;
  public static final long ROUTE_FILTER_V4 = 0xFFFF_0003L;
  public static final long ROUTE_FILTER_TRANSLATED_V6 = 0xFFFF_0004L;
  public static final long ROUTE_FILTER_V6 = 0xFFFF_0005L;
  public static final long LLGR_STALE = 0xFFFF_0006L;
  public static final long NO_LLGR = 0xFFFF_0007L;
  public static final long ACCEPT_OWN_NEXTHOP = 0xFFFF_0008L;
  public static final long BLACKHOLE = 0xFFFF_029AL;
  public static final long NO_EXPORT = 0xFFFF_FF01L;
  public static final long NO_ADVERTISE = 0xFFFF_FF02L;
  public static final long NO_EXPORT_SUBCONFED = 0xFFFF_FF03L;
  public static final long NO_PEER = 0xFFFF_FF04L;

  private WellKnownCommunity() {} // prevent instantiation
}
