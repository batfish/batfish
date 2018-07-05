package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;

public class IpsecPeerConfig implements Serializable {

  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";

  private static final String PROP_TUNNEL_INTERFACE = "tunnelInterface";

  private static final String PROP_PHYSICAL_INTERFACE = "physicalInterface";

  private static final String PROP_IPSEC_POLICY = "ipsecPolicy";

  private static final String PROP_POLICY_ACCESS_LIST = "policyAccessList";

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _sourceAddress;

  private String _physicalInterface;

  private String _tunnelInterface;

  private String _ipsecPolicy;

  private IpAccessList _policyAccessList;

  @JsonCreator
  public IpsecPeerConfig() {}

  @JsonPropertyDescription("Source address for IPSec peer")
  @JsonProperty(PROP_SOURCE_ADDRESS)
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @JsonPropertyDescription("Physical interface for IPSec peer")
  @JsonProperty(PROP_PHYSICAL_INTERFACE)
  public String getPhysicalInterface() {
    return _physicalInterface;
  }

  @JsonPropertyDescription("Tunnel interface for IPSec peer")
  @JsonProperty(PROP_TUNNEL_INTERFACE)
  public String getTunnelInterface() {
    return _tunnelInterface;
  }

  @JsonPropertyDescription("IPSec policy for IPSec peer")
  @JsonProperty(PROP_IPSEC_POLICY)
  public String getIpsecPolicy() {
    return _ipsecPolicy;
  }

  @JsonPropertyDescription("Policy access list for IPSec peer")
  @JsonProperty(PROP_POLICY_ACCESS_LIST)
  public IpAccessList getPolicyAccessList() {
    return _policyAccessList;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS)
  public void setSourceAddress(Ip sourceAddress) {
    _sourceAddress = sourceAddress;
  }

  @JsonProperty(PROP_PHYSICAL_INTERFACE)
  public void setPhysicalInterface(String physicalInterface) {
    _physicalInterface = physicalInterface;
  }

  @JsonProperty(PROP_TUNNEL_INTERFACE)
  public void setTunnelInterface(String tunnelInterface) {
    _tunnelInterface = tunnelInterface;
  }

  @JsonProperty(PROP_IPSEC_POLICY)
  public void setIpsecPolicy(String ipsecPolicy) {
    _ipsecPolicy = ipsecPolicy;
  }

  @JsonProperty(PROP_POLICY_ACCESS_LIST)
  public void setPolicyAccessList(IpAccessList policyAccessList) {
    _policyAccessList = policyAccessList;
  }
}
