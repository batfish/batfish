package org.batfish.representation.vyos;

import java.io.Serializable;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.Ip;

public class IpsecPeer implements Serializable {

  private String _authenticationId;

  private IkeAuthenticationMethod _authenticationMode;

  private String _authenticationPreSharedSecretHash;

  private String _authenticationRemoteId;

  private String _bindInterface;

  private String _description;

  private String _espGroup;

  private String _ikeGroup;

  private boolean _initiate;

  private Ip _localAddress;

  private final Ip _name;

  public IpsecPeer(Ip name) {
    _name = name;
  }

  public String getAuthenticationId() {
    return _authenticationId;
  }

  public IkeAuthenticationMethod getAuthenticationMode() {
    return _authenticationMode;
  }

  public String getAuthenticationPreSharedSecretHash() {
    return _authenticationPreSharedSecretHash;
  }

  public String getAuthenticationRemoteId() {
    return _authenticationRemoteId;
  }

  public String getBindInterface() {
    return _bindInterface;
  }

  public String getDescription() {
    return _description;
  }

  public String getEspGroup() {
    return _espGroup;
  }

  public String getIkeGroup() {
    return _ikeGroup;
  }

  public boolean getInitiate() {
    return _initiate;
  }

  public Ip getLocalAddress() {
    return _localAddress;
  }

  public void setAuthenticationId(String authenticationId) {
    _authenticationId = authenticationId;
  }

  public void setAuthenticationMode(IkeAuthenticationMethod authenticationMode) {
    _authenticationMode = authenticationMode;
  }

  public void setAuthenticationPreSharedSecretHash(String authenticationPreSharedSecretHash) {
    _authenticationPreSharedSecretHash = authenticationPreSharedSecretHash;
  }

  public void setAuthenticationRemoteId(String authenticationRemoteId) {
    _authenticationRemoteId = authenticationRemoteId;
  }

  public void setBindInterface(String bindInterface) {
    _bindInterface = bindInterface;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setEspGroup(String espGroup) {
    _espGroup = espGroup;
  }

  public void setIkeGroup(String ikeGroup) {
    _ikeGroup = ikeGroup;
  }

  public void setInitiate(boolean initiate) {
    _initiate = initiate;
  }

  public void setLocalAddress(Ip localAddress) {
    _localAddress = localAddress;
  }

  public Ip getName() {
    return _name;
  }
}
