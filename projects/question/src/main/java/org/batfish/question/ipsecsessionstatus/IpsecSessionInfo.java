package org.batfish.question.ipsecsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.questions.IpsecSessionStatus.MISSING_END_POINT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.questions.IpsecSessionStatus;

/** Captures the configuration state of an IPSec session */
public class IpsecSessionInfo {

  @Nonnull private String _initiatorHostname;

  @Nonnull private String _initiatorInterface;

  @Nonnull private Ip _initiatorIp;

  @Nullable private String _initiatorTunnelInterface;

  @Nullable private String _responderHostname;

  @Nullable private String _responderInterface;

  @Nullable private Ip _responderIp;

  @Nullable private String _responderTunnelInterface;

  @Nonnull private IpsecSessionStatus _ipsecSessionStatus;

  private IpsecSessionInfo(
      @Nonnull String initiatorHostname,
      @Nonnull String initiatorInterface,
      @Nonnull Ip initiatorIp,
      @Nullable String initiatorTunnelInterface,
      @Nullable String responderHostname,
      @Nullable String responderInterface,
      @Nullable Ip responderIp,
      @Nullable String responderTunnelInterface,
      IpsecSessionStatus ipsecSessionStatus) {
    _initiatorHostname = initiatorHostname;
    _initiatorInterface = initiatorInterface;
    _initiatorIp = initiatorIp;
    _initiatorTunnelInterface = initiatorTunnelInterface;
    _responderHostname = responderHostname;
    _responderInterface = responderInterface;
    _responderIp = responderIp;
    _responderTunnelInterface = responderTunnelInterface;
    _ipsecSessionStatus = firstNonNull(ipsecSessionStatus, MISSING_END_POINT);
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

  @Nullable
  public String getInitiatorTunnelInterface() {
    return _initiatorTunnelInterface;
  }

  @Nullable
  public String getResponderHostname() {
    return _responderHostname;
  }

  @Nullable
  public String getResponderInterface() {
    return _responderInterface;
  }

  @Nullable
  public String getResponderTunnelInterface() {
    return _responderTunnelInterface;
  }

  @Nullable
  public Ip getResponderIp() {
    return _responderIp;
  }

  @Nonnull
  public IpsecSessionStatus getIpsecSessionStatus() {
    return _ipsecSessionStatus;
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

    private IpsecSessionStatus _ipsecSessionStatus;

    private Builder() {}

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

    public Builder setIpsecSessionStatus(IpsecSessionStatus ipsecSessionStatus) {
      _ipsecSessionStatus = ipsecSessionStatus;
      return this;
    }

    public IpsecSessionInfo build() {
      return new IpsecSessionInfo(
          _initiatorHostname,
          _initiatorInterface,
          _initiatorIp,
          _initiatorTunnelInterface,
          _responderHostname,
          _responderInterface,
          _responderIp,
          _responderTunnelInterface,
          _ipsecSessionStatus);
    }
  }
}
