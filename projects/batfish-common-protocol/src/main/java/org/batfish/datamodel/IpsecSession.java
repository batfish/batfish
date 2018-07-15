package org.batfish.datamodel;

import javax.annotation.Nullable;

public class IpsecSession {

  @Nullable private IkePhase1Policy _initiatorIkeP1Policy;

  @Nullable private IpsecPhase2Policy _initiatorIpsecP2Policy;

  @Nullable private IkePhase1Proposal _negotiatedIkeP1Proposal;

  @Nullable private IkePhase1Key _negotiatedIkePhase1Key;

  @Nullable private IpsecPhase2Proposal _negotiatedIpsecPhase2Proposal;

  @Nullable private IkePhase1Policy _responderIkeP1Policy;

  @Nullable private IpsecPhase2Policy _responderIpsecP2Policy;

  @Nullable
  public IkePhase1Policy getInitiatorIkeP1Policy() {
    return _initiatorIkeP1Policy;
  }

  @Nullable
  public IpsecPhase2Policy getInitiatorIpsecP2Policy() {
    return _initiatorIpsecP2Policy;
  }

  @Nullable
  public IkePhase1Policy getResponderIkeP1Policy() {
    return _responderIkeP1Policy;
  }

  @Nullable
  public IpsecPhase2Policy getResponderIpsecP2Policy() {
    return _responderIpsecP2Policy;
  }

  @Nullable
  public IkePhase1Proposal getNegotiatedIkeP1Proposal() {
    return _negotiatedIkeP1Proposal;
  }

  @Nullable
  public IkePhase1Key getNegotiatedIkePhase1Key() {
    return _negotiatedIkePhase1Key;
  }

  @Nullable
  public IpsecPhase2Proposal getNegotiatedIpsecPhase2Proposal() {
    return _negotiatedIpsecPhase2Proposal;
  }

  public void setInitiatorIkeP1Policy(@Nullable IkePhase1Policy initiatorIkeP1Policy) {
    _initiatorIkeP1Policy = initiatorIkeP1Policy;
  }

  public void setInitiatorIpsecP2Policy(@Nullable IpsecPhase2Policy initiatorIpsecP2Policy) {
    _initiatorIpsecP2Policy = initiatorIpsecP2Policy;
  }

  public void setResponderIkeP1Policy(@Nullable IkePhase1Policy responderIkeP1Policy) {
    _responderIkeP1Policy = responderIkeP1Policy;
  }

  public void setResponderIpsecP2Policy(@Nullable IpsecPhase2Policy responderIpsecP2Policy) {
    _responderIpsecP2Policy = responderIpsecP2Policy;
  }

  public void setNegotiatedIkeP1Proposal(@Nullable IkePhase1Proposal ikePhase1Proposal) {
    _negotiatedIkeP1Proposal = ikePhase1Proposal;
  }

  public void setNegotiatedIkePhase1Key(@Nullable IkePhase1Key negotiatedIkePhase1Key) {
    _negotiatedIkePhase1Key = negotiatedIkePhase1Key;
  }

  public void setNegotiatedIpsecPhase2Proposal(@Nullable IpsecPhase2Proposal ipsecPhase2Proposal) {
    _negotiatedIpsecPhase2Proposal = ipsecPhase2Proposal;
  }

  public enum IpsecSessionType {
    STATIC,
    DYNAMIC
  }
}
