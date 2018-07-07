package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import javax.annotation.Nullable;

public class IpsecStaticPeerConfig extends IpsecPeerConfig implements Serializable {

  private static final String PROP_DESTINATION_ADDRESS = "destinationAddress";

  private static final String PROP_IKE_PHASE1_POLICY = "ikePhase1Policy";

  private static final String PROP_PEER_CONFIG = "peerConfig";

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _destinationAddress;

  private String _ikePhase1Policy;

  private IpsecPeerConfig _peerConfig;

  @JsonCreator
  public IpsecStaticPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) String ipsecPolicy,
      @JsonProperty(PROP_PHYSICAL_INTERFACE) String physicalInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) IpAccessList policyAccessList,
      @JsonProperty(PROP_SOURCE_ADDRESS) Ip sourceAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) String tunnelInterface,
      @JsonProperty(PROP_DESTINATION_ADDRESS) Ip destinationAddress,
      @JsonProperty(PROP_IKE_PHASE1_POLICY) String ikePhasePolicy,
      @JsonProperty(PROP_PEER_CONFIG) IpsecPeerConfig peerConfig) {
    super(ipsecPolicy, physicalInterface, policyAccessList, sourceAddress, tunnelInterface);
    _destinationAddress = destinationAddress;
    _ikePhase1Policy = ikePhasePolicy;
    _peerConfig = peerConfig;
  }

  @JsonPropertyDescription("Destination address for IPSec peer")
  @JsonProperty(PROP_DESTINATION_ADDRESS)
  public Ip getDestinationAddress() {
    return _destinationAddress;
  }

  @JsonPropertyDescription("IKE phase 1 policy for IPSec peer")
  @JsonProperty(PROP_IKE_PHASE1_POLICY)
  public String getIkePhase1Policy() {
    return _ikePhase1Policy;
  }

  @JsonPropertyDescription("IPSec peer configuration for IPSec peer")
  @JsonProperty(PROP_PEER_CONFIG)
  public IpsecPeerConfig getPeerConfig() {
    return _peerConfig;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_DESTINATION_ADDRESS)
  public void setDestinationAddress(@Nullable Ip destinationAddress) {
    _destinationAddress = destinationAddress;
  }

  @JsonProperty(PROP_IKE_PHASE1_POLICY)
  public void setIkePhase1Policy(@Nullable String ikePhase1Policy) {
    _ikePhase1Policy = ikePhase1Policy;
  }

  @JsonProperty(PROP_PEER_CONFIG)
  public void setPeerConfig(@Nullable IpsecPeerConfig peerConfig) {
    _peerConfig = peerConfig;
  }

  public static class Builder extends IpsecPeerConfig.Builder<Builder, IpsecStaticPeerConfig> {
    private Ip _destinationAddress;

    private String _ikePhase1Policy;

    private IpsecPeerConfig _peerConfig;

    @Override
    public IpsecStaticPeerConfig build() {
      return new IpsecStaticPeerConfig(
          _ipsecPolicy,
          _physicalInterface,
          _policyAccessList,
          _sourceAddress,
          _tunnelInterface,
          _destinationAddress,
          _ikePhase1Policy,
          _peerConfig);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setDestinationAddress(Ip destinationAddress) {
      _destinationAddress = destinationAddress;
      return this;
    }

    public Builder setIkePhase1Policy(String ikePhase1Policy) {
      _ikePhase1Policy = ikePhase1Policy;
      return this;
    }

    public Builder setPeerConfig(IpsecPeerConfig peerConfig) {
      _peerConfig = peerConfig;
      return this;
    }
  }
}
