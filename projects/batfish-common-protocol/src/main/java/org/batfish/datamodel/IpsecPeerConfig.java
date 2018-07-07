package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;

/** Represents a configured IPSec peer */
@JsonSchemaDescription("A configured IPSec peering relationship")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IpsecPeerConfig implements Serializable {

  static final String PROP_IPSEC_POLICY = "ipsecPolicy";

  static final String PROP_PHYSICAL_INTERFACE = "physicalInterface";

  static final String PROP_POLICY_ACCESS_LIST = "policyAccessList";

  static final String PROP_SOURCE_ADDRESS = "sourceAddress";

  static final String PROP_TUNNEL_INTERFACE = "tunnelInterface";

  /** */
  private static final long serialVersionUID = 1L;

  private String _ipsecPolicy;

  private String _physicalInterface;

  private IpAccessList _policyAccessList;

  private Ip _sourceAddress;

  private String _tunnelInterface;

  @JsonCreator
  public IpsecPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) String ipsecPolicy,
      @JsonProperty(PROP_PHYSICAL_INTERFACE) String physicalInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) IpAccessList policyAccessList,
      @JsonProperty(PROP_SOURCE_ADDRESS) Ip sourceAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) String tunnelInterface) {
    _ipsecPolicy = ipsecPolicy;
    _physicalInterface = physicalInterface;
    _policyAccessList = policyAccessList;
    _sourceAddress = sourceAddress;
    _tunnelInterface = tunnelInterface;
  }

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

  public abstract static class Builder<S extends Builder<S, T>, T extends IpsecPeerConfig> {
    Ip _sourceAddress;

    String _physicalInterface;

    String _tunnelInterface;

    String _ipsecPolicy;

    IpAccessList _policyAccessList;

    public abstract T build();

    protected abstract S getThis();

    public final S setSourceAddress(Ip sourceAddress) {
      _sourceAddress = sourceAddress;
      return getThis();
    }

    public final S setPhysicalInterface(String physicalInterface) {
      _physicalInterface = physicalInterface;
      return getThis();
    }

    public final S setTunnelInterface(String tunnelInterface) {
      _tunnelInterface = tunnelInterface;
      return getThis();
    }

    public final S setIpsecPolicy(String ipsecPolicy) {
      _ipsecPolicy = ipsecPolicy;
      return getThis();
    }

    public final S setPolicyAccessList(IpAccessList policyAccessList) {
      _policyAccessList = policyAccessList;
      return getThis();
    }
  }
}
