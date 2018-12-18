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

/**
 * Contains the non-key attributes of {@link org.batfish.datamodel.BgpRoute}s and {@link
 * AbstractRoute}s and defines a sorting order for these attributes
 */
@ParametersAreNullableByDefault
public class RouteRowAttribute implements Comparable<RouteRowAttribute> {
  @Nullable private final String _nextHop;

  @Nullable private final AsPath _asPath;

  @Nullable private final Integer _adminDistance;

  @Nonnull private final List<String> _communities;

  @Nullable private final Long _localPreference;

  @Nullable private final Long _metric;

  @Nullable private final String _originProtocol;

  @Nullable private final Integer _tag;

  private RouteRowAttribute(
      String nextHop,
      Integer adminDistance,
      Long metric,
      AsPath asPath,
      Long localPreference,
      List<String> communities,
      String originalProtocol,
      Integer tag) {
    _nextHop = nextHop;
    _adminDistance = adminDistance;
    _metric = metric;
    _asPath = asPath;
    _localPreference = localPreference;
    _communities = firstNonNull(communities, ImmutableList.of());
    _originProtocol = originalProtocol;
    _tag = tag;
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
  public String getNextHop() {
    return _nextHop;
  }

  @Nullable
  public String getOriginProtocol() {
    return _originProtocol;
  }

  @Nullable
  public Integer getTag() {
    return _tag;
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final Comparator<RouteRowAttribute> COMPARATOR =
      comparing(RouteRowAttribute::getNextHop, nullsLast(String::compareTo))
          .thenComparing(RouteRowAttribute::getAdminDistance, nullsLast(Integer::compareTo))
          .thenComparing(RouteRowAttribute::getMetric, nullsLast(Long::compareTo))
          .thenComparing(RouteRowAttribute::getAsPath, nullsLast(AsPath::compareTo))
          .thenComparing(RouteRowAttribute::getLocalPreference, nullsLast(Long::compareTo))
          .thenComparing(RouteRowAttribute::getOriginProtocol, nullsLast(String::compareTo))
          .thenComparing(RouteRowAttribute::getTag, nullsLast(Integer::compareTo))
          .thenComparing(
              routeRowAttribute -> routeRowAttribute.getCommunities().toString(),
              nullsLast(String::compareTo));

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
    return Objects.equals(_nextHop, that._nextHop)
        && Objects.equals(_adminDistance, that._adminDistance)
        && Objects.equals(_metric, that._metric)
        && Objects.equals(_asPath, that._asPath)
        && Objects.equals(_localPreference, that._localPreference)
        && Objects.equals(_communities, that._communities)
        && Objects.equals(_originProtocol, that._originProtocol)
        && Objects.equals(_tag, that._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nextHop,
        _adminDistance,
        _metric,
        _asPath,
        _localPreference,
        _communities,
        _originProtocol,
        _tag);
  }

  /** Builder for {@link RouteRowAttribute} */
  public static final class Builder {
    @Nullable private String _nextHop;

    @Nullable private Integer _adminDistance;

    @Nullable private Long _metric;

    @Nullable private AsPath _asPath;

    @Nullable private Long _localPreference;

    @Nullable private List<String> _communities;

    @Nullable private String _originProtocol;

    @Nullable private Integer _tag;

    public RouteRowAttribute build() {
      if (_tag != null && _tag == AbstractRoute.NO_TAG) {
        _tag = null;
      }
      return new RouteRowAttribute(
          _nextHop,
          _adminDistance,
          _metric,
          _asPath,
          _localPreference,
          _communities,
          _originProtocol,
          _tag);
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

    public Builder setNextHop(String nextHop) {
      _nextHop = nextHop;
      return this;
    }

    public Builder setTag(Integer tag) {
      _tag = tag;
      return this;
    }
  }
}
