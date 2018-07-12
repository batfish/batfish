package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a configured static (having a specified remote peer) IPSec peer */
public final class IpsecStaticPeerConfig extends IpsecPeerConfig implements Serializable {

  private static final String PROP_DESTINATION_ADDRESS = "destinationAddress";

  private static final String PROP_IKE_PHASE1_POLICY = "ikePhase1Policy";

  private static final String PROP_PEER_CONFIG = "peerConfig";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private Ip _destinationAddress;

  @Nullable private String _ikePhase1Policy;

  @Nullable private IpsecPeerConfig _peerConfig;

  @JsonCreator
  private IpsecStaticPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) @Nullable String ipsecPolicy,
      @JsonProperty(PROP_PHYSICAL_INTERFACE) @Nullable String physicalInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) @Nullable IpAccessList policyAccessList,
      @JsonProperty(PROP_LOCAL_ADDRESS) @Nullable Ip localAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) @Nullable String tunnelInterface,
      @JsonProperty(PROP_DESTINATION_ADDRESS) @Nullable Ip destinationAddress,
      @JsonProperty(PROP_IKE_PHASE1_POLICY) @Nullable String ikePhasePolicy,
      @JsonProperty(PROP_PEER_CONFIG) @Nullable IpsecPeerConfig peerConfig) {
    super(ipsecPolicy, physicalInterface, policyAccessList, localAddress, tunnelInterface);
    _destinationAddress = firstNonNull(destinationAddress, Ip.AUTO);
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

  public static final class Builder
      extends IpsecPeerConfig.Builder<Builder, IpsecStaticPeerConfig> {
    private Ip _destinationAddress;

    private String _ikePhase1Policy;

    private IpsecPeerConfig _peerConfig;

    @Override
    public IpsecStaticPeerConfig build() {
      return new IpsecStaticPeerConfig(
          _ipsecPolicy,
          _physicalInterface,
          _policyAccessList,
          _localAddress,
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
