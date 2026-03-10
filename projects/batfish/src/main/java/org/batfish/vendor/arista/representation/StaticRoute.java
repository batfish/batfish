package org.batfish.vendor.arista.representation;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

@ParametersAreNonnullByDefault
public final class StaticRoute implements Serializable {
  public static final class NextHop implements Serializable {
    public NextHop(@Nullable String nhint, @Nullable Ip nhip, boolean null0) {
      checkArgument(null0 || nhint != null || nhip != null, "Static route must have some next hop");
      checkArgument(
          null0 ^ (nhint != null || nhip != null),
          "Static route cannot both be null routed and have a next hop interface or ip");
      _nextHopInterface = nhint;
      _nextHopIp = nhip;
      _nullRouted = null0;
    }

    public @Nullable String getNextHopInterface() {
      return _nextHopInterface;
    }

    public @Nullable Ip getNextHopIp() {
      return _nextHopIp;
    }

    public boolean getNullRouted() {
      return _nullRouted;
    }

    // internals

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof NextHop)) {
        return false;
      }
      NextHop nextHop = (NextHop) o;
      return _nullRouted == nextHop._nullRouted
          && Objects.equals(_nextHopInterface, nextHop._nextHopInterface)
          && Objects.equals(_nextHopIp, nextHop._nextHopIp);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_nextHopInterface, _nextHopIp, _nullRouted);
    }

    private final @Nullable String _nextHopInterface;
    private final @Nullable Ip _nextHopIp;
    private final boolean _nullRouted;
  }

  public StaticRoute(NextHop nextHop, @Nullable Integer distance, boolean track) {
    _distance = distance;
    _nextHop = nextHop;
    _track = track;
  }

  public @Nullable Integer getDistance() {
    return _distance;
  }

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  public boolean getTrack() {
    return _track;
  }

  // internals

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute that = (StaticRoute) o;
    return Objects.equals(_distance, that._distance)
        && _nextHop.equals(that._nextHop)
        && _track == that._track;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_distance, _nextHop, _track);
  }

  private final @Nullable Integer _distance;
  private final @Nonnull NextHop _nextHop;
  private final boolean _track;
}
