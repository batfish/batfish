package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix6;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute.NextHopType;

/** Vendor-specific Aruba AOS-CX IPv6 static route. */
public final class AosCxStaticRoute6
    implements Serializable {

  public AosCxStaticRoute6(
      Prefix6 prefix,
      NextHopType nextHopType,
      String nextHop,
      @Nullable String vrfName) {
    _prefix = prefix;
    _nextHopType = nextHopType;
    _nextHop = nextHop;
    _vrfName = vrfName;
  }

  public @Nonnull String getNextHop() {
    return _nextHop;
  }

  public @Nonnull NextHopType getNextHopType() {
    return _nextHopType;
  }

  public @Nonnull Prefix6 getPrefix() {
    return _prefix;
  }

  public @Nullable String getVrfName() {
    return _vrfName;
  }

  private final @Nonnull String _nextHop;
  private final @Nonnull NextHopType _nextHopType;
  private final @Nonnull Prefix6 _prefix;
  private final @Nullable String _vrfName;
}
