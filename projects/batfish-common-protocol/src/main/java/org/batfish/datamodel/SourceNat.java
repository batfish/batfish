package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class SourceNat implements Serializable {

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

  private IpAccessList _acl;

  private Ip _poolIpFirst;

  private Ip _poolIpLast;

  public SourceNat() {}

  private SourceNat(IpAccessList acl, Ip poolIpFirst, Ip poolIpLast) {
    _acl = acl;
    _poolIpFirst = poolIpFirst;
    _poolIpLast = poolIpLast;
  }

  @JsonProperty(PROP_ACL)
  public IpAccessList getAcl() {
    return _acl;
  }

  @JsonProperty(PROP_POOL_IP_FIRST)
  public Ip getPoolIpFirst() {
    return _poolIpFirst;
  }

  @JsonProperty(PROP_POOL_IP_LAST)
  public Ip getPoolIpLast() {
    return _poolIpLast;
  }

  @JsonProperty(PROP_ACL)
  public void setAcl(IpAccessList acl) {
    _acl = acl;
  }

  @JsonProperty(PROP_POOL_IP_FIRST)
  public void setPoolIpFirst(Ip poolIpFirst) {
    _poolIpFirst = poolIpFirst;
  }

  @JsonProperty(PROP_POOL_IP_LAST)
  public void setPoolIpLast(Ip poolIpLast) {
    _poolIpLast = poolIpLast;
  }

  @Override
  public String toString() {
    return toStringHelper(SourceNat.class)
        .add(PROP_ACL, _acl.getName())
        .add(PROP_POOL_IP_FIRST, _poolIpFirst)
        .add(PROP_POOL_IP_LAST, _poolIpLast)
        .toString();
  }
}
