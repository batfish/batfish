package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;

/**
 * Contains the non-key attributes of {@link org.batfish.datamodel.BgpRoute}s and {@link
 * AbstractRoute}s and defines a sorting order for these attributes
 */
@ParametersAreNullableByDefault
public class RouteRowAttribute implements Comparable<RouteRowAttribute> {
  @Nullable private AsPath _asPath;

  @Nullable private Integer _adminDistance;

  @Nonnull private List<String> _communities;

  @Nullable private Long _localPreference;

  @Nullable private Long _metric;

  @Nullable private String _nextHop;

  @Nonnull private Ip _nextHopIp;

  @Nullable private String _originProtocol;

  @Nullable private String _protocol;

  @Nullable private Integer _tag;

  private RouteRowAttribute(
      @Nonnull Ip nextHopIp,
      String nextHop,
      Integer adminDistance,
      Long metric,
      AsPath asPath,
      Long localPreference,
      List<String> communities,
      String originalProtocol,
      String protocol,
      Integer tag) {
    _nextHopIp = nextHopIp;
    _nextHop = nextHop;
    _adminDistance = adminDistance;
    _metric = metric;
    _asPath = asPath;
    _localPreference = localPreference;
    _communities = firstNonNull(communities, ImmutableList.of());
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
  public AsPath getAsPath() {
    return _asPath;
  }

  @Nullable
  public Long getLocalPreference() {
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

  @Nullable
  public String getProtocol() {
    return _protocol;
  }

  @Nullable
  public Integer getTag() {
    return _tag;
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final Comparator<RouteRowAttribute> COMPARATOR =
      comparing(RouteRowAttribute::getNextHopIp, nullsLast(Ip::compareTo))
          .thenComparing(RouteRowAttribute::getProtocol, nullsLast(String::compareTo));

  @Override
  public int compareTo(RouteRowAttribute o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RouteRowAttribute that = (RouteRowAttribute) o;
    return Objects.equals(_nextHopIp, that._nextHopIp)
        && Objects.equals(_nextHop, that._nextHop)
        && Objects.equals(_adminDistance, that._adminDistance)
        && Objects.equals(_metric, that._metric)
        && Objects.equals(_asPath, that._asPath)
        && Objects.equals(_localPreference, that._localPreference)
        && Objects.equals(_communities, that._communities)
        && Objects.equals(_originProtocol, that._originProtocol)
        && Objects.equals(_protocol, that._protocol)
        && Objects.equals(_tag, that._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nextHopIp,
        _nextHop,
        _adminDistance,
        _metric,
        _asPath,
        _localPreference,
        _communities,
        _originProtocol,
        _protocol,
        _tag);
  }

  public static final class Builder {
    @Nullable private Ip _nextHopIp;

    @Nullable private String _nextHop;

    @Nullable private Integer _adminDistance;

    @Nullable private Long _metric;

    @Nullable private AsPath _asPath;

    @Nullable private Long _localPreference;

    @Nullable private List<String> _communities;

    @Nullable private String _originProtocol;

    @Nullable private String _protocol;

    @Nullable private Integer _tag;

    public RouteRowAttribute build() {
      _nextHopIp = firstNonNull(_nextHopIp, Ip.AUTO);
      if (_tag != null && _tag == AbstractRoute.NO_TAG) {
        _tag = null;
      }
      return new RouteRowAttribute(
          _nextHopIp,
          _nextHop,
          _adminDistance,
          _metric,
          _asPath,
          _localPreference,
          _communities,
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

    public Builder setAsPath(AsPath asPath) {
      _asPath = asPath;
      return this;
    }

    public Builder setLocalPreference(Long localPreference) {
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
