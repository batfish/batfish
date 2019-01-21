package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a configured IPSec peer */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IpsecPeerConfig implements Serializable {

  static final String PROP_IPSEC_POLICY = "ipsecPolicy";

  static final String PROP_SOURCE_INTERFACE = "sourceInterface";

  static final String PROP_POLICY_ACCESS_LIST = "policyAccessList";

  static final String PROP_LOCAL_ADDRESS = "localAddress";

  static final String PROP_TUNNEL_INTERFACE = "tunnelInterface";

  /** */
  private static final long serialVersionUID = 1L;

  @Nullable private String _ipsecPolicy;

  @Nonnull private String _sourceInterface;

  @Nullable private IpAccessList _policyAccessList;

  @Nonnull private Ip _localAddress;

  @Nullable private String _tunnelInterface;

  @JsonCreator
  protected IpsecPeerConfig(
      @JsonProperty(PROP_IPSEC_POLICY) @Nullable String ipsecPolicy,
      @JsonProperty(PROP_SOURCE_INTERFACE) @Nullable String sourceInterface,
      @JsonProperty(PROP_POLICY_ACCESS_LIST) @Nullable IpAccessList policyAccessList,
      @JsonProperty(PROP_LOCAL_ADDRESS) @Nullable Ip localAddress,
      @JsonProperty(PROP_TUNNEL_INTERFACE) @Nullable String tunnelInterface) {
    _ipsecPolicy = ipsecPolicy;
    _sourceInterface = firstNonNull(sourceInterface, UNSET_LOCAL_INTERFACE);
    _policyAccessList = policyAccessList;
    _localAddress = firstNonNull(localAddress, Ip.AUTO);
    _tunnelInterface = tunnelInterface;
  }

  @JsonPropertyDescription("Local address for IPSec peer")
  @JsonProperty(PROP_LOCAL_ADDRESS)
  public Ip getLocalAddress() {
    return _localAddress;
  }

  @JsonPropertyDescription("Source interface for IPSec peer")
  @JsonProperty(PROP_SOURCE_INTERFACE)
  public String getSourceInterface() {
    return _sourceInterface;
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
    Ip _localAddress;

    String _sourceInterface;

    String _tunnelInterface;

    String _ipsecPolicy;

    IpAccessList _policyAccessList;

    public abstract T build();

    protected abstract S getThis();

    public final S setLocalAddress(Ip localAddress) {
      _localAddress = localAddress;
      return getThis();
    }

    public final S setSourceInterface(String sourceInterface) {
      _sourceInterface = sourceInterface;
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
