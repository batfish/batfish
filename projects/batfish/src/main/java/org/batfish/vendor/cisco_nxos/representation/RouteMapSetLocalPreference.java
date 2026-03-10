package org.batfish.vendor.cisco_nxos.representation;

/** A {@link RouteMapSet} that sets the LOCAL_PREFERENCE attribute of a route. */
public final class RouteMapSetLocalPreference implements RouteMapSet {

  private final long _localPreference;

  public RouteMapSetLocalPreference(long localPreference) {
    _localPreference = localPreference;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetLocalPreference(this);
  }

  public long getLocalPreference() {
    return _localPreference;
  }
}
