package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a configured dynamic (remote peer not specified) IPSec peer */
public final class IpsecDynamicPeerConfig extends IpsecPeerConfig implements Serializable {

  private static final String PROP_IKE_PHASE1_POLICIES = "ikePhase1Policies";

  private static final String PROP_PEER_CONFIGS = "peerConfigs";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private List<String> _ikePhase1Policies;

  @Nonnull private List<IpsecPeerConfig> _peerConfigs;

  @JsonCreator
  private IpsecDynamicPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) @Nullable String ipsecPolicy,
      @JsonProperty(PROP_PHYSICAL_INTERFACE) @Nullable String physicalInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) @Nullable IpAccessList policyAccessList,
      @JsonProperty(PROP_LOCAL_ADDRESS) @Nullable Ip localAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) @Nullable String tunnelInterface,
      @JsonProperty(PROP_IKE_PHASE1_POLICIES) @Nullable List<String> ikePhase1Policies,
      @JsonProperty(PROP_PEER_CONFIGS) @Nullable List<IpsecPeerConfig> peerConfigs) {
    super(ipsecPolicy, physicalInterface, policyAccessList, localAddress, tunnelInterface);
    _ikePhase1Policies =
        ikePhase1Policies == null ? ImmutableList.of() : ImmutableList.copyOf(ikePhase1Policies);
    _peerConfigs = peerConfigs == null ? ImmutableList.of() : ImmutableList.copyOf(peerConfigs);
  }

  @JsonPropertyDescription("IKE phase 1 policies which can be used with this IPSec peer")
  @JsonProperty(PROP_IKE_PHASE1_POLICIES)
  public List<String> getIkePhase1Poliies() {
    return _ikePhase1Policies;
  }

  @JsonPropertyDescription("IPSec peer configurations for this IPSec peer")
  @JsonProperty(PROP_PEER_CONFIGS)
  public List<IpsecPeerConfig> getPeerConfigs() {
    return _peerConfigs;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends IpsecPeerConfig.Builder<IpsecDynamicPeerConfig.Builder, IpsecDynamicPeerConfig> {
    private List<String> _ikePhase1Polcies;

    private List<IpsecPeerConfig> _peerConfigs;

    @Override
    public IpsecDynamicPeerConfig build() {
      return new IpsecDynamicPeerConfig(
          _ipsecPolicy,
          _physicalInterface,
          _policyAccessList,
          _localAddress,
          _tunnelInterface,
          _ikePhase1Polcies,
          _peerConfigs);
    }

    @Override
    protected IpsecDynamicPeerConfig.Builder getThis() {
      return this;
    }

    public IpsecDynamicPeerConfig.Builder setIkePhase1Policies(List<String> ikePhase1Policies) {
      _ikePhase1Polcies = ikePhase1Policies;
      return this;
    }

    public IpsecDynamicPeerConfig.Builder setPeerConfigs(List<IpsecPeerConfig> peerConfigs) {
      _peerConfigs = peerConfigs;
      return this;
    }
  }
}
