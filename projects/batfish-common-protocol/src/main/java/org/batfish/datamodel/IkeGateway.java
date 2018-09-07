package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.Objects;

public class IkeGateway implements Serializable {

  private static final String PROP_EXTERNAL_INTERFACE = "externalInterface";

  private static final String PROP_IKE_POLICY = "ikePolicy";

  private static final String PROP_LOCAL_ADDRESS = "localAddress";

  private static final String PROP_NAME = "name";

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _address;

  private Interface _externalInterface;

  private transient String _externalInterfaceName;

  private IkePolicy _ikePolicy;

  private transient String _ikePolicyName;

  private String _localId;

  private Ip _localIp;

  private final String _name;

  private String _remoteId;

  @JsonCreator
  public IkeGateway(@JsonProperty(PROP_NAME) String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IkeGateway)) {
      return false;
    }
    IkeGateway other = (IkeGateway) o;
    return Objects.equals(_address, other._address)
        && Objects.equals(_externalInterface, other._externalInterface)
        && Objects.equals(_ikePolicy, other._ikePolicy)
        && Objects.equals(_localId, other._localId)
        && Objects.equals(_localIp, other._localIp)
        && Objects.equals(_name, other._name)
        && Objects.equals(_remoteId, other._remoteId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _address, _externalInterface, _ikePolicy, _localId, _localIp, _name, _remoteId);
  }

  @JsonPropertyDescription("Remote IP address of IKE gateway")
  public Ip getAddress() {
    return _address;
  }

  @JsonIgnore
  public Interface getExternalInterface() {
    return _externalInterface;
  }

  @JsonProperty(PROP_EXTERNAL_INTERFACE)
  @JsonPropertyDescription(
      "Logical (non-VPN) interface from which to connect to IKE gateway. This interface is used to"
          + " determine source-address for the connection.")
  public String getExternalInterfaceName() {
    if (_externalInterface != null) {
      return _externalInterface.getName();
    } else {
      return _externalInterfaceName;
    }
  }

  @JsonIgnore
  public IkePolicy getIkePolicy() {
    return _ikePolicy;
  }

  @JsonProperty(PROP_IKE_POLICY)
  @JsonPropertyDescription("IKE policy to be used with this IKE gateway.")
  public String getIkePolicyName() {
    if (_ikePolicy != null) {
      return _ikePolicy.getName();
    } else {
      return _ikePolicyName;
    }
  }

  @JsonPropertyDescription("Local IKE ID used in connection to IKE gateway.")
  public String getLocalId() {
    return _localId;
  }

  @JsonProperty(PROP_LOCAL_ADDRESS)
  @JsonPropertyDescription(
      "Local IP address from which to connect to IKE gateway. Used instead of external interface.")
  public Ip getLocalIp() {
    return _localIp;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonPropertyDescription("Remote IKE ID of IKE gateway.")
  public String getRemoteId() {
    return _remoteId;
  }

  public void resolveReferences(Configuration owner) {
    if (_externalInterfaceName != null) {
      _externalInterface = owner.getAllInterfaces().get(_externalInterfaceName);
    }
    if (_ikePolicyName != null) {
      _ikePolicy = owner.getIkePolicies().get(_ikePolicyName);
    }
  }

  public void setAddress(Ip address) {
    _address = address;
  }

  @JsonIgnore
  public void setExternalInterface(Interface externalInterface) {
    _externalInterface = externalInterface;
  }

  @JsonProperty(PROP_EXTERNAL_INTERFACE)
  public void setExternalInterfaceName(String externalInterfaceName) {
    _externalInterfaceName = externalInterfaceName;
  }

  @JsonIgnore
  public void setIkePolicy(IkePolicy ikePolicy) {
    _ikePolicy = ikePolicy;
  }

  @JsonProperty(PROP_IKE_POLICY)
  public void setIkePolicyName(String ikePolicyName) {
    _ikePolicyName = ikePolicyName;
  }

  public void setLocalId(String localId) {
    _localId = localId;
  }

  @JsonProperty(PROP_LOCAL_ADDRESS)
  public void setLocalIp(Ip localIp) {
    _localIp = localIp;
  }

  public void setRemoteId(String remoteId) {
    _remoteId = remoteId;
  }
}
