package org.batfish.representation.cisco_asa;

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

  private Prefix _prefix;

  @Nullable private Integer _track;

  public StaticRoute(
      Prefix prefix,
      Ip nextHopIp,
      @Nullable String nextHopInterface,
      int distance,
      @Nullable Integer track) {
    _prefix = prefix;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
    _distance = distance;
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

  public Prefix getPrefix() {
    return _prefix;
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
