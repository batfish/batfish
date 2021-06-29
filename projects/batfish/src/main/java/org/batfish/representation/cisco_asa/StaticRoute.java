package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHop;

@ParametersAreNonnullByDefault
public class StaticRoute implements Serializable {

  private int _distance;

  private @Nonnull NextHop _nextHop;

  private @Nonnull Prefix _prefix;

  @Nullable private Integer _track;

  public StaticRoute(Prefix prefix, NextHop nextHop, int distance, @Nullable Integer track) {
    _prefix = prefix;
    _nextHop = nextHop;
    _distance = distance;
    _track = track;
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

  public @Nullable Integer getTrack() {
    return _track;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute that = (StaticRoute) o;
    return _distance == that._distance
        && _nextHop.equals(that._nextHop)
        && _prefix.equals(that._prefix)
        && Objects.equals(_track, that._track);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_distance, _nextHop, _prefix, _track);
  }
}
