package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.Prefix6;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute.NextHopType;

/** Vendor-specific Aruba AOS-CX IPv6 static route. */
public final class AosCxStaticRoute6
    implements Serializable {

  public static final long DEFAULT_ADMINISTRATIVE_DISTANCE = 1L;

  public AosCxStaticRoute6(
      Prefix6 prefix,
      NextHopType nextHopType,
      String nextHop,
      @Nullable String vrfName) {
    this(
        prefix,
        nextHopType,
        nextHop,
        vrfName,
        DEFAULT_ADMINISTRATIVE_DISTANCE,
        Route.UNSET_ROUTE_TAG);
  }

  public AosCxStaticRoute6(
      Prefix6 prefix,
      NextHopType nextHopType,
      String nextHop,
      @Nullable String vrfName,
      long administrativeDistance,
      long tag) {
    _prefix = prefix;
    _nextHopType = nextHopType;
    _nextHop = nextHop;
    _vrfName = vrfName;
    _administrativeDistance = administrativeDistance;
    _tag = tag;
  }

  public long getAdministrativeDistance() {
    return _administrativeDistance;
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

  public long getTag() {
    return _tag;
  }

  public @Nullable String getVrfName() {
    return _vrfName;
  }

  private final long _administrativeDistance;
  private final @Nonnull String _nextHop;
  private final @Nonnull NextHopType _nextHopType;
  private final @Nonnull Prefix6 _prefix;
  private final long _tag;
  private final @Nullable String _vrfName;
}
