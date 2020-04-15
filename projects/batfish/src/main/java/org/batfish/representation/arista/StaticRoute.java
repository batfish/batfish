package org.batfish.representation.arista;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public class StaticRoute implements Serializable {

  private int _distance;

  @Nullable private String _nextHopInterface;

  private Ip _nextHopIp;

  private boolean _permanent;

  private Prefix _prefix;

  @Nullable private Long _tag;

  @Nullable private Integer _track;

  public StaticRoute(
      Prefix prefix,
      Ip nextHopIp,
      @Nullable String nextHopInterface,
      int distance,
      @Nullable Long tag,
      @Nullable Integer track,
      boolean permanent) {
    _prefix = prefix;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
    _distance = distance;
    _tag = tag;
    _track = track;
    _permanent = permanent;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute rhs = (StaticRoute) o;
    boolean res = _prefix.equals(rhs._prefix);
    if (_nextHopIp != null) {
      res = res && _nextHopIp.equals(rhs._nextHopIp);
    } else {
      res = res && rhs._nextHopIp == null;
    }
    if (_nextHopInterface != null) {
      return res && _nextHopInterface.equals(rhs._nextHopInterface);
    } else {
      return res && rhs._nextHopInterface == null;
    }
  }

  public int getDistance() {
    return _distance;
  }

  @Nullable
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  public boolean getPermanent() {
    return _permanent;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  @Nullable
  public Long getTag() {
    return _tag;
  }

  @Nullable
  public Integer getTrack() {
    return _track;
  }

  @Override
  public int hashCode() {
    int code = _prefix.hashCode();
    if (_nextHopInterface != null) {
      code = code * 31 + _nextHopInterface.hashCode();
    }
    if (_nextHopIp != null) {
      code = code * 31 + _nextHopIp.hashCode();
    }
    return code;
  }
}
