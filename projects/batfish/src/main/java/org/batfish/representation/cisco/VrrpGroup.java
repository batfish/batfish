package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class VrrpGroup extends ComparableStructure<Integer> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _authenticationTextHash;

  private boolean _preempt;

  private int _priority;

  private Ip _virtualAddress;

  public VrrpGroup(Integer name) {
    super(name);
    _preempt = CiscoConfiguration.DEFAULT_VRRP_PREEMPT;
    _priority = CiscoConfiguration.DEFAULT_VRRP_PRIORITY;
  }

  public String getAuthenticationTextHash() {
    return _authenticationTextHash;
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
