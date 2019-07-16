package org.batfish.representation.cisco_nxos;

/** A {@link RouteMapMatch} that matches routes based on the tag. */
public final class RouteMapMatchTag implements RouteMapMatch {

  private final long _tag;

  public RouteMapMatchTag(long tag) {
    _tag = tag;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchTag(this);
  }

  public long getTag() {
    return _tag;
  }
}
