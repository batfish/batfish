package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** A {@link RouteMapMatch} that matches routes based on the tag. */
public final class RouteMapMatchTag implements RouteMapMatch {

  private final Set<Long> _tags;

  public RouteMapMatchTag(Iterable<Long> tags) {
    _tags = ImmutableSet.copyOf(tags);
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchTag(this);
  }

  public Set<Long> getTags() {
    return _tags;
  }
}
