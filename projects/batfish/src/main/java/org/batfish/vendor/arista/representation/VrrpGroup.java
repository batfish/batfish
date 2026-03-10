package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import org.batfish.datamodel.Ip;

public class VrrpGroup implements Serializable {

  private String _authenticationTextHash;

  private final Integer _name;

  private boolean _preempt;

  private int _priority;

  private Ip _virtualAddress;

  public VrrpGroup(Integer name) {
    _name = name;
    _preempt = AristaConfiguration.DEFAULT_VRRP_PREEMPT;
    _priority = AristaConfiguration.DEFAULT_VRRP_PRIORITY;
  }

  public String getAuthenticationTextHash() {
    return _authenticationTextHash;
  }

  public Integer getName() {
    return _name;
  }

  public boolean getPreempt() {
    return _preempt;
  }

  public int getPriority() {
    return _priority;
  }

  public Ip getVirtualAddress() {
    return _virtualAddress;
  }

  public void setAuthenticationTextHash(String authenticationTextHash) {
    _authenticationTextHash = authenticationTextHash;
  }

  public void setPreempt(boolean preempt) {
    _preempt = preempt;
  }

  public void setPriority(int priority) {
    _priority = priority;
  }

  public void setVirtualAddress(Ip virtualAddress) {
    _virtualAddress = virtualAddress;
  }
}
