package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Representation of a dynamic source NAT. If the {@link IpAccessList} matches the flow, then the
 * source IP is replaced with one in the pool.
 */
@ParametersAreNonnullByDefault
public final class SourceNat implements Serializable {

  public static class Builder {

    private IpAccessList _acl;

    private Ip _poolIpFirst;

    private Ip _poolIpLast;

    private Builder() {}

    public SourceNat build() {
      return new SourceNat(_acl, _poolIpFirst, _poolIpLast);
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

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  // null ACL means permit everything
  private @Nullable IpAccessList _acl;

  // null pool IPs are for non-natting rules
  private @Nullable Ip _poolIpFirst;

  private @Nullable Ip _poolIpLast;

  public SourceNat() {}

  public SourceNat(@Nullable IpAccessList acl, @Nullable Ip poolIpFirst, @Nullable Ip poolIpLast) {
    _acl = acl;
    _poolIpFirst = poolIpFirst;
    _poolIpLast = poolIpLast;
  }

  @JsonCreator
  private static SourceNat jsonCreator(
      @JsonProperty(PROP_ACL) @Nullable IpAccessList acl,
      @JsonProperty(PROP_POOL_IP_FIRST) @Nullable Ip poolIpFirst,
      @JsonProperty(PROP_POOL_IP_LAST) @Nullable Ip poolIpLast) {
    return new SourceNat(acl, poolIpFirst, poolIpLast);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SourceNat)) {
      return false;
    }
    SourceNat sourceNat = (SourceNat) o;
    return Objects.equals(_acl, sourceNat._acl)
        && Objects.equals(_poolIpFirst, sourceNat._poolIpFirst)
        && Objects.equals(_poolIpLast, sourceNat._poolIpLast);
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

  @JsonProperty(PROP_ACL)
  public void setAcl(IpAccessList acl) {
    _acl = acl;
  }

  @JsonProperty(PROP_POOL_IP_FIRST)
  public void setPoolIpFirst(@Nonnull Ip poolIpFirst) {
    _poolIpFirst = poolIpFirst;
  }

  @JsonProperty(PROP_POOL_IP_LAST)
  public void setPoolIpLast(@Nonnull Ip poolIpLast) {
    _poolIpLast = poolIpLast;
  }

  @Override
  public String toString() {
    String name = _acl == null ? null : _acl.getName();
    return toStringHelper(SourceNat.class)
        .add(PROP_ACL, name)
        .add(PROP_POOL_IP_FIRST, _poolIpFirst)
        .add(PROP_POOL_IP_LAST, _poolIpLast)
        .toString();
  }
}
