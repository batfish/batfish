package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;

public class IpsecDynamicPeerConfig extends IpsecPeerConfig implements Serializable {

  private static final String PROP_IKE_PHASE1_POLICIES = "ikePhase1Policies";

  private static final String PROP_PEER_CONFIGS = "peerConfigs";

  /** */
  private static final long serialVersionUID = 1L;

  private List<String> _ikePhase1Policies;

  private List<IpsecPeerConfig> _peerConfigs;

  @JsonCreator
  public IpsecDynamicPeerConfig() {
    _ikePhase1Policies = ImmutableList.of();
    _peerConfigs = ImmutableList.of();
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

  @JsonProperty(PROP_IKE_PHASE1_POLICIES)
  public void setIkePhase1Policies(List<String> ikePhase1Policies) {
    _ikePhase1Policies =
        ikePhase1Policies == null ? ImmutableList.of() : ImmutableList.copyOf(ikePhase1Policies);
  }

  @JsonProperty(PROP_PEER_CONFIGS)
  public void setPeerConfigs(List<IpsecPeerConfig> peerConfigs) {
    _peerConfigs = peerConfigs == null ? ImmutableList.of() : ImmutableList.copyOf(peerConfigs);
  }
}
