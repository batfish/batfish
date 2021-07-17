package org.batfish.vendor.check_point_gateway.representation;

/** Data model class representing a nexthop for a null route. */
public class NexthopBlackhole implements NexthopTarget {
  public static final NexthopBlackhole INSTANCE = new NexthopBlackhole();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof NexthopBlackhole;
  }

  @Override
  public int hashCode() {
    return 0x2BEAD5BD; // randomly generated
  }

  private NexthopBlackhole() {}
}
