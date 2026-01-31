package org.batfish.vendor.cisco_nxos.representation;

/** A {@link RouteMapSet} that sets the tag of a route. */
public final class RouteMapSetTag implements RouteMapSet {

  private final long _tag;

  public RouteMapSetTag(long tag) {
    _tag = tag;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetTag(this);
  }

  public long getTag() {
    return _tag;
  }
}
