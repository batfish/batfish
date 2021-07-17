package org.batfish.vendor.check_point_gateway.representation;

/**
 * Data model class representing a nexthop for a route that drops packets and sends a destination
 * unreachable notification to sender.
 */
public class NexthopReject implements NexthopTarget {
  public static final NexthopReject INSTANCE = new NexthopReject();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof NexthopReject;
  }

  @Override
  public int hashCode() {
    return 0x41EBC430; // randomly generated
  }

  private NexthopReject() {}
}
