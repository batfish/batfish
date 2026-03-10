package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a configured dynamic (remote peer not specified) IPSec peer */
public final class IpsecDynamicPeerConfig extends IpsecPeerConfig implements Serializable {
  private static final String PROP_IKE_PHASE1_POLICIES = "ikePhase1Policies";

  private @Nonnull List<String> _ikePhase1Policies;

  @JsonCreator
  private IpsecDynamicPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) @Nullable String ipsecPolicy,
      @JsonProperty(PROP_SOURCE_INTERFACE) @Nullable String physicalInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) @Nullable IpAccessList policyAccessList,
      @JsonProperty(PROP_LOCAL_ADDRESS) @Nullable Ip localAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) @Nullable String tunnelInterface,
      @JsonProperty(PROP_IKE_PHASE1_POLICIES) @Nullable List<String> ikePhase1Policies) {
    super(ipsecPolicy, physicalInterface, policyAccessList, localAddress, tunnelInterface);
    _ikePhase1Policies =
        ikePhase1Policies == null ? ImmutableList.of() : ImmutableList.copyOf(ikePhase1Policies);
  }

  /** IKE phase 1 policies which can be used with this IPSec peer. */
  @JsonProperty(PROP_IKE_PHASE1_POLICIES)
  public List<String> getIkePhase1Poliies() {
    return _ikePhase1Policies;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends IpsecPeerConfig.Builder<IpsecDynamicPeerConfig.Builder, IpsecDynamicPeerConfig> {
    private List<String> _ikePhase1Polcies;

    @Override
    public IpsecDynamicPeerConfig build() {
      return new IpsecDynamicPeerConfig(
          _ipsecPolicy,
          _sourceInterface,
          _policyAccessList,
          _localAddress,
          _tunnelInterface,
          _ikePhase1Polcies);
    }

    @Override
    protected IpsecDynamicPeerConfig.Builder getThis() {
      return this;
    }

    public IpsecDynamicPeerConfig.Builder setIkePhase1Policies(List<String> ikePhase1Policies) {
      _ikePhase1Polcies = ikePhase1Policies;
      return this;
    }
  }
}
