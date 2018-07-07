package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.List;

/** Represents a configured dynamic(remote peer not specified) IPSec peer */
public class IpsecDynamicPeerConfig extends IpsecPeerConfig implements Serializable {

  private static final String PROP_IKE_PHASE1_POLICIES = "ikePhase1Policies";

  private static final String PROP_PEER_CONFIGS = "peerConfigs";

  /** */
  private static final long serialVersionUID = 1L;

  private List<String> _ikePhase1Policies;

  private List<IpsecPeerConfig> _peerConfigs;

  @JsonCreator
  public IpsecDynamicPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) String ipsecPolicy,
      @JsonProperty(PROP_PHYSICAL_INTERFACE) String physicalInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) IpAccessList policyAccessList,
      @JsonProperty(PROP_SOURCE_ADDRESS) Ip sourceAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) String tunnelInterface,
      @JsonProperty(PROP_IKE_PHASE1_POLICIES) List<String> ikePhase1Policies,
      @JsonProperty(PROP_PEER_CONFIGS) List<IpsecPeerConfig> peerConfigs) {
    super(ipsecPolicy, physicalInterface, policyAccessList, sourceAddress, tunnelInterface);
    _ikePhase1Policies = ikePhase1Policies;
    _peerConfigs = peerConfigs;
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

  public static class Builder
      extends IpsecPeerConfig.Builder<IpsecDynamicPeerConfig.Builder, IpsecDynamicPeerConfig> {
    private List<String> _ikePhase1Polcies;

    private List<IpsecPeerConfig> _peerConfigs;

    @Override
    public IpsecDynamicPeerConfig build() {
      return new IpsecDynamicPeerConfig(
          _ipsecPolicy,
          _physicalInterface,
          _policyAccessList,
          _sourceAddress,
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
