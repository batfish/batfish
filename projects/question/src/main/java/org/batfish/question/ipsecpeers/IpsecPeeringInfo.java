package org.batfish.question.ipsecpeers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.MISSING_END_POINT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Captures the configuration state of an IPSec peering */
public class IpsecPeeringInfo {

  public enum IpsecPeeringStatus {
    IPSEC_SESSION_ESTABLISHED,
    IKE_PHASE1_FAILED,
    IKE_PHASE1_KEY_MISMATCH,
    IPSEC_PHASE2_FAILED,
    MISSING_END_POINT
  }

  @Nonnull private String _initiatorHostname;

  @Nonnull private String _initiatorInterface;

  @Nonnull private Ip _initiatorIp;

  @Nullable private String _initiatorTunnelInterface;

  @Nullable private String _responderHostname;

  private String _responderInterface;

  private Ip _responderIp;

  private String _responderTunnelInterface;

  @Nonnull private IpsecPeeringStatus _ipsecPeeringStatus;

  private IpsecPeeringInfo(
      @Nonnull String initiatorHostname,
      @Nonnull String initiatorInterface,
      @Nonnull Ip initiatorIp,
      @Nullable String initiatorTunnelInterface,
      @Nullable String responderHostname,
      String responderInterface,
      Ip responderIp,
      String responderTunnelInterface,
      IpsecPeeringStatus ipsecPeeringStatus) {
    _initiatorHostname = initiatorHostname;
    _initiatorInterface = initiatorInterface;
    _initiatorIp = initiatorIp;
    _initiatorTunnelInterface = initiatorTunnelInterface;
    _responderHostname = responderHostname;
    _responderInterface = responderInterface;
    _responderIp = responderIp;
    _responderTunnelInterface = responderTunnelInterface;
    _ipsecPeeringStatus = firstNonNull(ipsecPeeringStatus, MISSING_END_POINT);
  }

  @Nonnull
  public String getInitiatorHostname() {
    return _initiatorHostname;
  }

  @Nonnull
  public String getInitiatorInterface() {
    return _initiatorInterface;
  }

  @Nonnull
  public Ip getInitiatorIp() {
    return _initiatorIp;
  }

  public String getInitiatorTunnelInterface() {
    return _initiatorTunnelInterface;
  }

  public String getResponderHostname() {
    return _responderHostname;
  }

  public String getResponderInterface() {
    return _responderInterface;
  }

  @Nullable
  public String getResponderTunnelInterface() {
    return _responderTunnelInterface;
  }

  public Ip getResponderIp() {
    return _responderIp;
  }

  @Nonnull
  public IpsecPeeringStatus getIpsecPeeringStatus() {
    return _ipsecPeeringStatus;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String _initiatorHostname;

    private String _initiatorInterface;

    private Ip _initiatorIp;

    private String _initiatorTunnelInterface;

    private String _responderHostname;

    private String _responderInterface;

    private Ip _responderIp;

    private String _responderTunnelInterface;

    private IpsecPeeringStatus _ipsecPeeringStatus;

    public Builder setInitiatorHostname(String initiatorHostname) {
      _initiatorHostname = initiatorHostname;
      return this;
    }

    public Builder setInitiatorInterface(String initiatorInterface) {
      _initiatorInterface = initiatorInterface;
      return this;
    }

    public Builder setInitiatorIp(Ip initiatorIp) {
      _initiatorIp = initiatorIp;
      return this;
    }

    public Builder setInitiatorTunnelInterface(@Nullable String initiatorTunnelInterface) {
      _initiatorTunnelInterface = initiatorTunnelInterface;
      return this;
    }

    public Builder setResponderHostname(String responderHostname) {
      _responderHostname = responderHostname;
      return this;
    }

    public Builder setResponderInterface(String responderInterface) {
      _responderInterface = responderInterface;
      return this;
    }

    public Builder setResponderIp(Ip responderIp) {
      _responderIp = responderIp;
      return this;
    }

    public Builder setResponderTunnelInterface(@Nullable String responderTunnelInterface) {
      _responderTunnelInterface = responderTunnelInterface;
      return this;
    }

    public Builder setIpsecPeeringStatus(IpsecPeeringStatus ipsecPeeringStatus) {
      _ipsecPeeringStatus = ipsecPeeringStatus;
      return this;
    }

    public IpsecPeeringInfo build() {
      return new IpsecPeeringInfo(
          _initiatorHostname,
          _initiatorInterface,
          _initiatorIp,
          _initiatorTunnelInterface,
          _responderHostname,
          _responderInterface,
          _responderIp,
          _responderTunnelInterface,
          _ipsecPeeringStatus);
    }
  }
}
