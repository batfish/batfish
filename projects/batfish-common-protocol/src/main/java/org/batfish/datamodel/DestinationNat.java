package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Representation of a dynamic destination NAT. If the {@link IpAccessList} matches the flow, then
 * the destination IP is replaced with one in the pool.
 */
@ParametersAreNonnullByDefault
public class DestinationNat implements Serializable {

  public static class Builder {

    private IpAccessList _acl;

    private Ip _poolIpFirst;

    private Ip _poolIpLast;

    private Builder() {}

    public DestinationNat build() {
      return new DestinationNat(_acl, _poolIpFirst, _poolIpLast);
    }

    public Builder setAcl(IpAccessList acl) {
      _acl = acl;
      return this;
    }

    public Builder setPoolIpFirst(Ip poolIpFirst) {
      _poolIpFirst = poolIpFirst;
      return this;
    }

    public Builder setPoolIpLast(Ip poolIpLast) {
      _poolIpLast = poolIpLast;
      return this;
    }
  }

  private static final String PROP_ACL = "acl";

  private static final String PROP_POOL_IP_FIRST = "poolIpFirst";

  private static final String PROP_POOL_IP_LAST = "poolIpLast";

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  // null ACL means permit everything
  private final @Nullable IpAccessList _acl;

  // null pool IPs are for non-natting rules
  private final @Nullable Ip _poolIpFirst;

  private final @Nullable Ip _poolIpLast;

  public DestinationNat(
      @Nullable IpAccessList acl, @Nullable Ip poolIpFirst, @Nullable Ip poolIpLast) {
    _acl = acl;
    _poolIpFirst = poolIpFirst;
    _poolIpLast = poolIpLast;
  }

  @JsonCreator
  private static DestinationNat jsonCreator(
      @JsonProperty(PROP_ACL) @Nullable IpAccessList acl,
      @JsonProperty(PROP_POOL_IP_FIRST) @Nullable Ip poolIpFirst,
      @JsonProperty(PROP_POOL_IP_LAST) @Nullable Ip poolIpLast) {
    return new DestinationNat(acl, poolIpFirst, poolIpLast);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DestinationNat)) {
      return false;
    }
    DestinationNat that = (DestinationNat) o;
    return Objects.equals(_acl, that._acl)
        && Objects.equals(_poolIpFirst, that._poolIpFirst)
        && Objects.equals(_poolIpLast, that._poolIpLast);
  }

  @JsonProperty(PROP_ACL)
  public @Nullable IpAccessList getAcl() {
    return _acl;
  }

  @JsonProperty(PROP_POOL_IP_FIRST)
  public @Nullable Ip getPoolIpFirst() {
    return _poolIpFirst;
  }

  @JsonProperty(PROP_POOL_IP_LAST)
  public @Nullable Ip getPoolIpLast() {
    return _poolIpLast;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_acl, _poolIpFirst, _poolIpLast);
  }

  @Override
  public String toString() {
    String name = _acl == null ? null : _acl.getName();
    return toStringHelper(DestinationNat.class)
        .add(PROP_ACL, name)
        .add(PROP_POOL_IP_FIRST, _poolIpFirst)
        .add(PROP_POOL_IP_LAST, _poolIpLast)
        .toString();
  }
}
