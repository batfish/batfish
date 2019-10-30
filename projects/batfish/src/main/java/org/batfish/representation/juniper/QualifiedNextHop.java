package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Represents a qualified next-hop configured for a {@link StaticRoute} */
public class QualifiedNextHop implements Serializable {
  /**
   * {@link QualifiedNextHop} in a {@link StaticRoute} are keyed by either next hop IP or next hop
   * interface
   */
  public static class QualifiedNextHopKey implements Serializable {
    @Nullable private Ip _nextHopIp;
    @Nullable private String _nextHopInterface;

    public QualifiedNextHopKey(@Nonnull String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
    }

    public QualifiedNextHopKey(@Nonnull Ip nextHopIp) {
      _nextHopIp = nextHopIp;
    }

    @Nullable
    public String getNextHopInterface() {
      return _nextHopInterface;
    }

    @Nullable
    public Ip getNextHopIp() {
      return _nextHopIp;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof QualifiedNextHopKey)) {
        return false;
      }
      QualifiedNextHopKey that = (QualifiedNextHopKey) o;
      return Objects.equals(_nextHopIp, that._nextHopIp)
          && Objects.equals(_nextHopInterface, that._nextHopInterface);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_nextHopIp, _nextHopInterface);
    }
  }

  @Nullable private Integer _metric;
  @Nullable private String _nextHopInterface;
  @Nullable private Ip _nextHopIp;
  @Nullable private Integer _preference;
  @Nullable private Long _tag;

  private QualifiedNextHop(@Nullable String nextHopInterface, @Nullable Ip nextHopIp) {
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
  }

  public QualifiedNextHop(@Nonnull QualifiedNextHopKey qualifiedNextHopKey) {
    this(qualifiedNextHopKey._nextHopInterface, qualifiedNextHopKey._nextHopIp);
  }

  @Nullable
  public Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  @Nullable
  public Integer getPreference() {
    return _preference;
  }

  public void setPreference(@Nullable Integer preference) {
    _preference = preference;
  }

  @Nullable
  public Long getTag() {
    return _tag;
  }

  public void setTag(@Nullable Long tag) {
    _tag = tag;
  }

  @Nullable
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nullable
  public Ip getNextHopIp() {
    return _nextHopIp;
  }
}
