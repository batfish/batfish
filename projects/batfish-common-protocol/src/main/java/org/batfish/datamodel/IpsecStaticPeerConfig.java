package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;

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
  public IpsecStaticPeerConfig() {}

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

  @JsonProperty(PROP_DESTINATION_ADDRESS)
  public void setDestinationAddress(Ip destinationAddress) {
    _destinationAddress = destinationAddress;
  }

  @JsonProperty(PROP_IKE_PHASE1_POLICY)
  public void setIkePhase1Policy(String ikePhase1Policy) {
    _ikePhase1Policy = ikePhase1Policy;
  }

  @JsonProperty(PROP_PEER_CONFIG)
  public void setPeerConfig(IpsecPeerConfig peerConfig) {
    _peerConfig = peerConfig;
  }
}
