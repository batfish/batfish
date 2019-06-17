package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for an IPv4 address family */
@ParametersAreNonnullByDefault
public final class Ipv4UnicastAddressFamily extends AddressFamily {

  private static final long serialVersionUID = 1L;
  private static final Ipv4UnicastAddressFamily INSTANCE = new Ipv4UnicastAddressFamily();

  private Ipv4UnicastAddressFamily() {}

  @JsonCreator
  public static Ipv4UnicastAddressFamily instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof Ipv4UnicastAddressFamily;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
