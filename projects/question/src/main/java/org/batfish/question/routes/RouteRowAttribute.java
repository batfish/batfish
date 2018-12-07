package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.batfish.datamodel.AbstractRoute.NO_TAG;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;

// split for bgp and abstract route...
public class RouteRowAttribute implements Comparable<RouteRowAttribute> {
  @Nullable private String _asPath;

  @Nullable private Integer _adminDistance;

  @Nonnull private List<String> _communities;

  @Nullable private Integer _localPreference;

  @Nullable private Long _metric;

  @Nonnull private Ip _nextHopIp;

  @Nullable private String _nextHop;

  @Nullable private String _originProtocol;

  @Nonnull private String _protocol;

  private int _tag;

  public RouteRowAttribute(
      @Nonnull Ip nextHopIp,
      @Nullable String nextHop,
      @Nullable Integer adminDistance,
      @Nullable Long metric,
      @Nullable String asPath,
      @Nullable Integer localPreference,
      @Nonnull List<String> communities,
      @Nullable String originalProtocol,
      @Nonnull String protocol,
      int tag) {
    _nextHopIp = nextHopIp;
    _nextHop = nextHop;
    _adminDistance = adminDistance;
    _metric = metric;
    _asPath = asPath;
    _localPreference = localPreference;
    _communities = communities;
    _originProtocol = originalProtocol;
    _protocol = protocol;
    _tag = tag;
  }

  @Nonnull
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nullable
  public String getNextHop() {
    return _nextHop;
  }

  @Nullable
  public Integer getAdminDistance() {
    return _adminDistance;
  }

  @Nullable
  public Long getMetric() {
    return _metric;
  }

  @Nullable
  public String getAsPath() {
    return _asPath;
  }

  @Nullable
  public Integer getLocalPreference() {
    return _localPreference;
  }

  @Nonnull
  public List<String> getCommunities() {
    return _communities;
  }

  @Nullable
  public String getOriginProtocol() {
    return _originProtocol;
  }

  @Nonnull
  public String getProtocol() {
    return _protocol;
  }

  public int getTag() {
    return _tag;
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final Comparator<RouteRowAttribute> COMPARATOR =
      comparing(RouteRowAttribute::getNextHop, nullsLast(String::compareToIgnoreCase))
          .thenComparing(RouteRowAttribute::getNextHopIp, nullsLast(Ip::compareTo));

  @Override
  public int compareTo(RouteRowAttribute o) {
    return COMPARATOR.compare(this, o);
  }

  @ParametersAreNullableByDefault
  public static final class Builder {
    @Nullable private Ip _nextHopIp;

    @Nullable private String _nextHop;

    @Nullable private Integer _adminDistance;

    @Nullable private Long _metric;

    @Nullable private String _asPath;

    @Nullable private Integer _localPreference;

    @Nullable private List<String> _communities;

    @Nullable private String _originProtocol;

    @Nullable private String _protocol;

    @Nullable private Integer _tag;

    public RouteRowAttribute build() {
      if (_nextHopIp == null) {
        throw new BatfishException("Route is missing the next hop IP");
      }
      if (_protocol == null) {
        throw new BatfishException("Route is missing the Protocol");
      }
      if (_tag == null) {
        _tag = NO_TAG;
      }

      return new RouteRowAttribute(
          _nextHopIp,
          _nextHop,
          _adminDistance,
          _metric,
          _asPath,
          _localPreference,
          firstNonNull(_communities, new ArrayList<>()),
          _originProtocol,
          _protocol,
          _tag);
    }

    public Builder setNextHopIp(@Nonnull Ip nextHopIp) {
      _nextHopIp = nextHopIp;
      return this;
    }

    public Builder setNextHop(String nextHop) {
      _nextHop = nextHop;
      return this;
    }

    public Builder setAdminDistance(Integer adminDistance) {
      _adminDistance = adminDistance;
      return this;
    }

    public Builder setMetric(Long metric) {
      _metric = metric;
      return this;
    }

    public Builder setAsPath(String asPath) {
      _asPath = asPath;
      return this;
    }

    public Builder setLocalPreference(Integer localPreference) {
      _localPreference = localPreference;
      return this;
    }

    public Builder setCommunities(List<String> communities) {
      _communities = communities;
      return this;
    }

    public Builder setOriginProtocol(String originProtocol) {
      _originProtocol = originProtocol;
      return this;
    }

    public Builder setProtocol(String protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setTag(Integer tag) {
      _tag = tag;
      return this;
    }
  }
}
