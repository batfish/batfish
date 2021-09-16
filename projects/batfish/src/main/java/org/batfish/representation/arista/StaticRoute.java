package org.batfish.representation.arista;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHop;

@ParametersAreNonnullByDefault
public class StaticRoute implements Serializable {

  private final int _distance;

  private final @Nonnull NextHop _nextHop;

  private final @Nonnull Prefix _prefix;

  private final @Nullable Long _tag;

  private final @Nullable Integer _track;

  public StaticRoute(
      Prefix prefix, NextHop nextHop, int distance, @Nullable Long tag, @Nullable Integer track) {
    _prefix = prefix;
    _nextHop = nextHop;
    _distance = distance;
    _tag = tag;
    _track = track;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute rhs = (StaticRoute) o;
    return _prefix.equals(rhs._prefix) && _nextHop.equals(rhs._nextHop);
  }

  public int getDistance() {
    return _distance;
  }

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public @Nullable Long getTag() {
    return _tag;
  }

  public @Nullable Integer getTrack() {
    return _track;
  }

  @Override
  public int hashCode() {
    return _prefix.hashCode() * 31 + _nextHop.hashCode();
  }
}
