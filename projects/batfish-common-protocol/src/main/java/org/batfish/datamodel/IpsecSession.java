package org.batfish.datamodel;

import javax.annotation.Nullable;

/** Represents the attributes of the session established between two {@link IpsecPeerConfig}s */
public class IpsecSession {

  @Nullable private final IkePhase1Policy _initiatorIkeP1Policy;

  @Nullable private final IpsecPhase2Policy _initiatorIpsecP2Policy;

  @Nullable private final IkePhase1Proposal _negotiatedIkeP1Proposal;

  @Nullable private final IkePhase1Key _negotiatedIkeP1Key;

  @Nullable private final IpsecPhase2Proposal _negotiatedIpsecP2Proposal;

  @Nullable private final IkePhase1Policy _responderIkeP1Policy;

  @Nullable private final IpsecPhase2Policy _responderIpsecP2Policy;

  private IpsecSession(
      @Nullable IkePhase1Policy initiatorIkeP1Policy,
      @Nullable IpsecPhase2Policy initiatorIpsecP2Policy,
      @Nullable IkePhase1Proposal negotiatedIkeP1Proposal,
      @Nullable IkePhase1Key negotiatedIkeP1Key,
      @Nullable IpsecPhase2Proposal negotiatedIpsecP2Proposal,
      @Nullable IkePhase1Policy responderIkeP1Policy,
      @Nullable IpsecPhase2Policy responderIpsecP2Policy) {
    _initiatorIkeP1Policy = initiatorIkeP1Policy;
    _initiatorIpsecP2Policy = initiatorIpsecP2Policy;
    _negotiatedIkeP1Proposal = negotiatedIkeP1Proposal;
    _negotiatedIkeP1Key = negotiatedIkeP1Key;
    _negotiatedIpsecP2Proposal = negotiatedIpsecP2Proposal;
    _responderIkeP1Policy = responderIkeP1Policy;
    _responderIpsecP2Policy = responderIpsecP2Policy;
  }

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
  public IkePhase1Key getNegotiatedIkeP1Key() {
    return _negotiatedIkeP1Key;
  }

  @Nullable
  public IpsecPhase2Proposal getNegotiatedIpsecP2Proposal() {
    return _negotiatedIpsecP2Proposal;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private IkePhase1Policy _initiatorIkeP1Policy;

    private IpsecPhase2Policy _initiatorIpsecP2Policy;

    private IkePhase1Proposal _negotiatedIkeP1Proposal;

    private IkePhase1Key _negotiatedIkeP1Key;

    private IpsecPhase2Proposal _negotiatedIpsecP2Proposal;

    private IkePhase1Policy _responderIkeP1Policy;

    private IpsecPhase2Policy _responderIpsecP2Policy;

    public IpsecSession build() {
      return new IpsecSession(
          _initiatorIkeP1Policy,
          _initiatorIpsecP2Policy,
          _negotiatedIkeP1Proposal,
          _negotiatedIkeP1Key,
          _negotiatedIpsecP2Proposal,
          _responderIkeP1Policy,
          _responderIpsecP2Policy);
    }

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
    public IkePhase1Key getNegotiatedIkeP1Key() {
      return _negotiatedIkeP1Key;
    }

    @Nullable
    public IpsecPhase2Proposal getNegotiatedIpsecP2Proposal() {
      return _negotiatedIpsecP2Proposal;
    }

    public void setInitiatorIkeP1Policy(IkePhase1Policy initiatorIkeP1Policy) {
      _initiatorIkeP1Policy = initiatorIkeP1Policy;
    }

    public void setInitiatorIpsecP2Policy(IpsecPhase2Policy initiatorIpsecP2Policy) {
      _initiatorIpsecP2Policy = initiatorIpsecP2Policy;
    }

    public void setResponderIkeP1Policy(IkePhase1Policy responderIkeP1Policy) {
      _responderIkeP1Policy = responderIkeP1Policy;
    }

    public void setResponderIpsecP2Policy(IpsecPhase2Policy responderIpsecP2Policy) {
      _responderIpsecP2Policy = responderIpsecP2Policy;
    }

    public void setNegotiatedIkeP1Proposal(IkePhase1Proposal ikePhase1Proposal) {
      _negotiatedIkeP1Proposal = ikePhase1Proposal;
    }

    public void setNegotiatedIkeP1Key(IkePhase1Key negotiatedIkePhase1Key) {
      _negotiatedIkeP1Key = negotiatedIkePhase1Key;
    }

    public void setNegotiatedIpsecP2Proposal(IpsecPhase2Proposal ipsecPhase2Proposal) {
      _negotiatedIpsecP2Proposal = ipsecPhase2Proposal;
    }
  }

  public enum IpsecSessionType {
    STATIC,
    DYNAMIC
  }
}
