package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;

import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;

/** Represents the attributes of the session established between two {@link IpsecPeerConfig}s */
@ParametersAreNullableByDefault
public class IpsecSession {
  public static final Set<ConfigurationFormat> CLOUD_CONFIGURATION_FORMATS =
      ImmutableSet.of(ConfigurationFormat.AWS);

  /** Port on which IKE (Phase 1) and IPsec(Phase 2) parameters are exchanged through UDP */
  public static final int IPSEC_UDP_PORT = 500;

  private final @Nullable IkePhase1Policy _initiatorIkeP1Policy;

  private final @Nullable IpsecPhase2Policy _initiatorIpsecP2Policy;

  private final @Nullable IkePhase1Proposal _negotiatedIkeP1Proposal;

  private final @Nullable IkePhase1Key _negotiatedIkeP1Key;

  private final @Nullable IpsecPhase2Proposal _negotiatedIpsecP2Proposal;

  private final @Nullable IkePhase1Policy _responderIkeP1Policy;

  private final @Nullable IpsecPhase2Policy _responderIpsecP2Policy;

  /**
   * Is true when at least one of the peers for this IPsec session is a cloud type configuration
   * (like AWS)
   */
  private final boolean _cloud;

  private IpsecSession(
      boolean cloud,
      IkePhase1Policy initiatorIkeP1Policy,
      IpsecPhase2Policy initiatorIpsecP2Policy,
      IkePhase1Proposal negotiatedIkeP1Proposal,
      IkePhase1Key negotiatedIkeP1Key,
      IpsecPhase2Proposal negotiatedIpsecP2Proposal,
      IkePhase1Policy responderIkeP1Policy,
      IpsecPhase2Policy responderIpsecP2Policy) {
    _cloud = cloud;
    _initiatorIkeP1Policy = initiatorIkeP1Policy;
    _initiatorIpsecP2Policy = initiatorIpsecP2Policy;
    _negotiatedIkeP1Proposal = negotiatedIkeP1Proposal;
    _negotiatedIkeP1Key = negotiatedIkeP1Key;
    _negotiatedIpsecP2Proposal = negotiatedIpsecP2Proposal;
    _responderIkeP1Policy = responderIkeP1Policy;
    _responderIpsecP2Policy = responderIpsecP2Policy;
  }

  /**
   * Is true when at least one of the peers for this IPsec session is a cloud type configuration
   * (like AWS)
   *
   * @return true for a cloud type {@link IpsecSession}
   */
  public boolean isCloud() {
    return _cloud;
  }

  public @Nullable IkePhase1Policy getInitiatorIkeP1Policy() {
    return _initiatorIkeP1Policy;
  }

  public @Nullable IpsecPhase2Policy getInitiatorIpsecP2Policy() {
    return _initiatorIpsecP2Policy;
  }

  public @Nullable IkePhase1Policy getResponderIkeP1Policy() {
    return _responderIkeP1Policy;
  }

  public @Nullable IpsecPhase2Policy getResponderIpsecP2Policy() {
    return _responderIpsecP2Policy;
  }

  public @Nullable IkePhase1Proposal getNegotiatedIkeP1Proposal() {
    return _negotiatedIkeP1Proposal;
  }

  public @Nullable IkePhase1Key getNegotiatedIkeP1Key() {
    return _negotiatedIkeP1Key;
  }

  public @Nullable IpsecPhase2Proposal getNegotiatedIpsecP2Proposal() {
    return _negotiatedIpsecP2Proposal;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean _cloud;

    private IkePhase1Policy _initiatorIkeP1Policy;

    private IpsecPhase2Policy _initiatorIpsecP2Policy;

    private IkePhase1Proposal _negotiatedIkeP1Proposal;

    private IkePhase1Key _negotiatedIkeP1Key;

    private IpsecPhase2Proposal _negotiatedIpsecP2Proposal;

    private IkePhase1Policy _responderIkeP1Policy;

    private IpsecPhase2Policy _responderIpsecP2Policy;

    public IpsecSession build() {
      return new IpsecSession(
          _cloud,
          _initiatorIkeP1Policy,
          _initiatorIpsecP2Policy,
          _negotiatedIkeP1Proposal,
          _negotiatedIkeP1Key,
          _negotiatedIpsecP2Proposal,
          _responderIkeP1Policy,
          _responderIpsecP2Policy);
    }

    public boolean isCloud() {
      return _cloud;
    }

    public @Nullable IkePhase1Policy getInitiatorIkeP1Policy() {
      return _initiatorIkeP1Policy;
    }

    public @Nullable IpsecPhase2Policy getInitiatorIpsecP2Policy() {
      return _initiatorIpsecP2Policy;
    }

    public @Nullable IkePhase1Policy getResponderIkeP1Policy() {
      return _responderIkeP1Policy;
    }

    public @Nullable IpsecPhase2Policy getResponderIpsecP2Policy() {
      return _responderIpsecP2Policy;
    }

    public @Nullable IkePhase1Proposal getNegotiatedIkeP1Proposal() {
      return _negotiatedIkeP1Proposal;
    }

    public @Nullable IkePhase1Key getNegotiatedIkeP1Key() {
      return _negotiatedIkeP1Key;
    }

    public @Nullable IpsecPhase2Proposal getNegotiatedIpsecP2Proposal() {
      return _negotiatedIpsecP2Proposal;
    }

    public Builder setCloud(boolean cloud) {
      _cloud = cloud;
      return this;
    }

    public Builder setInitiatorIkeP1Policy(IkePhase1Policy initiatorIkeP1Policy) {
      _initiatorIkeP1Policy = initiatorIkeP1Policy;
      return this;
    }

    public Builder setInitiatorIpsecP2Policy(IpsecPhase2Policy initiatorIpsecP2Policy) {
      _initiatorIpsecP2Policy = initiatorIpsecP2Policy;
      return this;
    }

    public Builder setResponderIkeP1Policy(IkePhase1Policy responderIkeP1Policy) {
      _responderIkeP1Policy = responderIkeP1Policy;
      return this;
    }

    public Builder setResponderIpsecP2Policy(IpsecPhase2Policy responderIpsecP2Policy) {
      _responderIpsecP2Policy = responderIpsecP2Policy;
      return this;
    }

    public Builder setNegotiatedIkeP1Proposal(IkePhase1Proposal ikePhase1Proposal) {
      _negotiatedIkeP1Proposal = ikePhase1Proposal;
      return this;
    }

    public Builder setNegotiatedIkeP1Key(IkePhase1Key negotiatedIkePhase1Key) {
      _negotiatedIkeP1Key = negotiatedIkePhase1Key;
      return this;
    }

    public Builder setNegotiatedIpsecP2Proposal(IpsecPhase2Proposal ipsecPhase2Proposal) {
      _negotiatedIpsecP2Proposal = ipsecPhase2Proposal;
      return this;
    }
  }

  public enum IpsecSessionType {
    STATIC,
    DYNAMIC
  }

  /**
   * Returns true if the given {@link Configuration} has a cloud type {@link ConfigurationFormat}
   *
   * @param configuration {@link Configuration}
   * @return true if {@link Configuration} is a cloud type node
   */
  public static boolean isCloudConfig(Configuration configuration) {
    return CLOUD_CONFIGURATION_FORMATS.contains(configuration.getConfigurationFormat());
  }
}
