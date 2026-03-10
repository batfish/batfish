package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.questions.BgpRouteStatus;

/**
 * Contains the non-key attributes of {@link BgpRoute}s and {@link AbstractRoute}s and defines a
 * sorting order for these attributes.
 */
@ParametersAreNullableByDefault
public class RouteRowAttribute implements Comparable<RouteRowAttribute> {
  private final @Nullable String _nextHopInterface;
  private final @Nullable AsPath _asPath;
  private final @Nullable Long _adminDistance;
  private final @Nonnull Set<Long> _clusterList;
  private final @Nonnull List<String> _communities;
  private final @Nullable Long _localPreference;
  private final @Nullable Long _metric;
  private final @Nullable String _originProtocol;
  private final @Nullable OriginMechanism _originMechanism;
  private final @Nullable OriginType _originType;
  private final @Nullable Ip _originatorIp;
  private final @Nullable Long _tag;
  private final @Nullable BgpRouteStatus _status;
  private final @Nullable TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;
  private final @Nullable Integer _weight;

  private RouteRowAttribute(
      String nextHopInterface,
      Long adminDistance,
      Long metric,
      AsPath asPath,
      Long localPreference,
      Set<Long> clusterList,
      List<String> communities,
      String originalProtocol,
      OriginMechanism originMechanism,
      OriginType originType,
      Ip originatorIp,
      Long tag,
      BgpRouteStatus status,
      @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute,
      Integer weight) {
    _nextHopInterface = nextHopInterface;
    _adminDistance = adminDistance;
    _metric = metric;
    _asPath = asPath;
    _localPreference = localPreference;
    _clusterList = firstNonNull(clusterList, ImmutableSet.of());
    _communities = firstNonNull(communities, ImmutableList.of());
    _originProtocol = originalProtocol;
    _originMechanism = originMechanism;
    _originType = originType;
    _originatorIp = originatorIp;
    _tag = tag;
    _status = status;
    _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
    _weight = weight;
  }

  public @Nullable Long getAdminDistance() {
    return _adminDistance;
  }

  public @Nullable Long getMetric() {
    return _metric;
  }

  public @Nullable AsPath getAsPath() {
    return _asPath;
  }

  public @Nullable Long getLocalPreference() {
    return _localPreference;
  }

  public @Nonnull Set<Long> getClusterList() {
    return _clusterList;
  }

  public @Nonnull List<String> getCommunities() {
    return _communities;
  }

  public @Nullable String getNextHopInterface() {
    return _nextHopInterface;
  }

  public @Nullable String getOriginProtocol() {
    return _originProtocol;
  }

  public @Nullable OriginMechanism getOriginMechanism() {
    return _originMechanism;
  }

  public @Nullable OriginType getOriginType() {
    return _originType;
  }

  public @Nullable Ip getOriginatorIp() {
    return _originatorIp;
  }

  public @Nullable Long getTag() {
    return _tag;
  }

  public @Nullable BgpRouteStatus getStatus() {
    return _status;
  }

  public @Nullable TunnelEncapsulationAttribute getTunnelEncapsulationAttribute() {
    return _tunnelEncapsulationAttribute;
  }

  public @Nullable Integer getWeight() {
    return _weight;
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final Comparator<RouteRowAttribute> COMPARATOR =
      comparing(RouteRowAttribute::getNextHopInterface, nullsLast(String::compareTo))
          .thenComparing(RouteRowAttribute::getAdminDistance, nullsLast(Long::compareTo))
          .thenComparing(RouteRowAttribute::getMetric, nullsLast(Long::compareTo))
          .thenComparing(RouteRowAttribute::getAsPath, nullsLast(AsPath::compareTo))
          .thenComparing(RouteRowAttribute::getLocalPreference, nullsLast(Long::compareTo))
          .thenComparing(RouteRowAttribute::getOriginProtocol, nullsLast(String::compareTo))
          .thenComparing(
              RouteRowAttribute::getOriginMechanism, nullsLast(OriginMechanism::compareTo))
          .thenComparing(RouteRowAttribute::getOriginType, nullsLast(OriginType::compareTo))
          .thenComparing(RouteRowAttribute::getOriginatorIp, nullsLast(Ip::compareTo))
          .thenComparing(RouteRowAttribute::getTag, nullsLast(Long::compareTo))
          .thenComparing(RouteRowAttribute::getStatus, nullsLast(BgpRouteStatus::compareTo))
          .thenComparing(
              routeRowAttribute -> routeRowAttribute.getClusterList().toString(), String::compareTo)
          .thenComparing(
              routeRowAttribute -> routeRowAttribute.getCommunities().toString(), String::compareTo)
          .thenComparing(
              routeRowAttribute ->
                  Optional.ofNullable(routeRowAttribute.getTunnelEncapsulationAttribute())
                      .map(TunnelEncapsulationAttribute::toString)
                      .orElse(null),
              nullsLast(String::compareTo))
          .thenComparing(RouteRowAttribute::getWeight, Comparator.nullsLast(Integer::compareTo));

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
    return Objects.equals(_nextHopInterface, that._nextHopInterface)
        && Objects.equals(_adminDistance, that._adminDistance)
        && Objects.equals(_metric, that._metric)
        && Objects.equals(_asPath, that._asPath)
        && Objects.equals(_localPreference, that._localPreference)
        && _clusterList.equals(that._clusterList)
        && _communities.equals(that._communities)
        && Objects.equals(_originProtocol, that._originProtocol)
        && Objects.equals(_originMechanism, that._originMechanism)
        && Objects.equals(_originType, that._originType)
        && Objects.equals(_originatorIp, that._originatorIp)
        && Objects.equals(_tag, that._tag)
        && Objects.equals(_status, that._status)
        && Objects.equals(_tunnelEncapsulationAttribute, that._tunnelEncapsulationAttribute)
        && Objects.equals(_weight, that._weight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nextHopInterface,
        _adminDistance,
        _metric,
        _asPath,
        _localPreference,
        _clusterList,
        _communities,
        _originMechanism,
        _originProtocol,
        _originType == null ? 0 : _originType.ordinal(),
        _originatorIp,
        _tag,
        _status == null ? 0 : _status.ordinal(),
        _tunnelEncapsulationAttribute,
        _weight);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add("nextHopInterface", _nextHopInterface)
        .add("adminDistance", _adminDistance)
        .add("asPath", _asPath)
        .add("clusterList", _clusterList)
        .add("communities", _communities)
        .add("localPreference", _localPreference)
        .add("metric", _metric)
        .add("originMechanism", _originMechanism)
        .add("originProtocol", _originProtocol)
        .add("originType", _originType)
        .add("originatorIp", _originatorIp)
        .add("tag", _tag)
        .add("tunnelEncapsulationAttribute", _tunnelEncapsulationAttribute)
        .add("weight", _weight)
        .add("status", _status)
        .toString();
  }

  /** Builder for {@link RouteRowAttribute} */
  public static final class Builder {
    private @Nullable String _nextHopInterface;
    private @Nullable Long _adminDistance;
    private @Nullable Long _metric;
    private @Nullable AsPath _asPath;
    private @Nullable Long _localPreference;
    private @Nullable Set<Long> _clusterList;
    private @Nullable List<String> _communities;
    private @Nullable String _originProtocol;
    private @Nullable OriginMechanism _originMechanism;
    private @Nullable OriginType _originType;
    private @Nullable Ip _originatorIp;
    private @Nullable Long _tag;
    private @Nullable BgpRouteStatus _status;
    private @Nullable TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;
    private @Nullable Integer _weight;

    public RouteRowAttribute build() {
      if (_tag != null && _tag == Route.UNSET_ROUTE_TAG) {
        _tag = null;
      }
      return new RouteRowAttribute(
          _nextHopInterface,
          _adminDistance,
          _metric,
          _asPath,
          _localPreference,
          _clusterList,
          _communities,
          _originProtocol,
          _originMechanism,
          _originType,
          _originatorIp,
          _tag,
          _status,
          _tunnelEncapsulationAttribute,
          _weight);
    }

    public Builder setAdminDistance(Long adminDistance) {
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

    public Builder setClusterList(Set<Long> clusterList) {
      _clusterList = clusterList;
      return this;
    }

    public Builder setCommunities(List<String> communities) {
      _communities = communities;
      return this;
    }

    public Builder setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return this;
    }

    public Builder setOriginProtocol(String originProtocol) {
      _originProtocol = originProtocol;
      return this;
    }

    public Builder setOriginMechanism(OriginMechanism originMechanism) {
      _originMechanism = originMechanism;
      return this;
    }

    public Builder setOriginType(OriginType originType) {
      _originType = originType;
      return this;
    }

    public Builder setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
      return this;
    }

    public Builder setTag(Long tag) {
      _tag = tag;
      return this;
    }

    public Builder setStatus(BgpRouteStatus status) {
      _status = status;
      return this;
    }

    public Builder setTunnelEncapsulationAttribute(
        @Nullable TunnelEncapsulationAttribute tunnelEncapsulationAttribute) {
      _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
      return this;
    }

    public Builder setWeight(Integer weight) {
      _weight = weight;
      return this;
    }
  }
}
