package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class IkeGateway implements Serializable {

  public enum LocalIdentityType {
    DISTINGUISHED_NAME,
    HOSTNAME,
    INET,
    INET6,
    KEY_ID,
    USER_AT_HOSTNAME
  }

  private Ip _address;

  private String _externalInterface;

  private boolean _generalIkeId;

  private String _ikePolicy;

  private @Nullable String _localIdentity;

  private @Nullable LocalIdentityType _localIdentityType;

  private Ip _localAddress;

  private final String _name;

  public IkeGateway(String name) {
    _name = name;
  }

  public Ip getAddress() {
    return _address;
  }

  public String getExternalInterface() {
    return _externalInterface;
  }

  public String getIkePolicy() {
    return _ikePolicy;
  }

  public boolean getGeneralIkeId() {
    return _generalIkeId;
  }

  public @Nullable String getLocalIdentity() {
    return _localIdentity;
  }

  public @Nullable LocalIdentityType getLocalIdentityType() {
    return _localIdentityType;
  }

  public Ip getLocalAddress() {
    return _localAddress;
  }

  public String getName() {
    return _name;
  }

  public void setAddress(Ip address) {
    _address = address;
  }

  public void setExternalInterface(String externalInterface) {
    _externalInterface = externalInterface;
  }

  public void setIkePolicy(String ikePolicy) {
    _ikePolicy = ikePolicy;
  }

  public void setGeneralIkeId() {
    _generalIkeId = true;
  }

  public void setLocalIdentity(
      LocalIdentityType localIdentityType, @Nullable String localIdentity) {
    _localIdentityType = localIdentityType;
    _localIdentity = localIdentity;
  }

  public void setLocalAddress(Ip localAddress) {
    _localAddress = localAddress;
  }
}
