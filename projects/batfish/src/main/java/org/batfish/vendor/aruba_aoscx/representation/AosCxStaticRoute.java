package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Vendor-specific representation of an Aruba AOS-CX IPv4 static route. */
public final class AosCxStaticRoute implements Serializable {

  public enum NextHopType {
    IP,
    INTERFACE,
    NULL_ROUTE,
    REJECT
  }

  public AosCxStaticRoute(Prefix prefix, NextHopType nextHopType, String nextHop) {
    this(prefix, nextHopType, nextHop, null);
  }

  public AosCxStaticRoute(
      Prefix prefix, NextHopType nextHopType, String nextHop, @Nullable String vrfName) {
    _prefix = prefix;
    _nextHopType = nextHopType;
    _nextHop = nextHop;
    _vrfName = vrfName;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public @Nonnull NextHopType getNextHopType() {
    return _nextHopType;
  }

  public @Nonnull String getNextHop() {
    return _nextHop;
  }

  public @Nullable String getVrfName() {
    return _vrfName;
  }

  private final @Nonnull Prefix _prefix;
  private final @Nonnull NextHopType _nextHopType;
  private final @Nonnull String _nextHop;
  private final @Nullable String _vrfName;
}
