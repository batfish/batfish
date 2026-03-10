package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * This class is used by route-map questions to represent a concrete next-hop in a route, i.e. a
 * next-hop that a real route could have. That contrasts with other kinds of next-hop results from
 * some questions, which are purely symbolic, such as {@link NextHopSelf}.
 */
public final class NextHopConcrete implements NextHopResult {

  private static final String NEXT_HOP = "nextHop";

  private final NextHop _nextHop;

  @JsonCreator
  public NextHopConcrete(@JsonProperty(NEXT_HOP) NextHop nh) {
    _nextHop = nh;
  }

  @JsonProperty(NEXT_HOP)
  public NextHop getNextHop() {
    return _nextHop;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof NextHopConcrete)) {
      return false;
    }
    return _nextHop.equals(((NextHopConcrete) obj)._nextHop);
  }

  @Override
  public int hashCode() {
    return _nextHop.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NextHopConcrete.class).add("nextHop", _nextHop).toString();
  }
}
