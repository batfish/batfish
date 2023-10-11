package org.batfish.question.ipsecsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.questions.IpsecSessionStatus.MISSING_END_POINT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.questions.IpsecSessionStatus;

/** Captures the configuration state of an IPSec session */
public class IpsecSessionInfo {

  private @Nonnull String _initiatorHostname;

  private @Nonnull String _initiatorInterface;

  private @Nonnull Ip _initiatorIp;

  private @Nullable String _initiatorTunnelInterface;

  private @Nullable String _responderHostname;

  private @Nullable String _responderInterface;

  private @Nullable Ip _responderIp;

  private @Nullable String _responderTunnelInterface;

  private @Nonnull IpsecSessionStatus _ipsecSessionStatus;

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

  public @Nonnull String getInitiatorHostname() {
    return _initiatorHostname;
  }

  public @Nonnull String getInitiatorInterface() {
    return _initiatorInterface;
  }

  public @Nonnull Ip getInitiatorIp() {
    return _initiatorIp;
  }

  public @Nullable String getInitiatorTunnelInterface() {
    return _initiatorTunnelInterface;
  }

  public @Nullable String getResponderHostname() {
    return _responderHostname;
  }

  public @Nullable String getResponderInterface() {
    return _responderInterface;
  }

  public @Nullable String getResponderTunnelInterface() {
    return _responderTunnelInterface;
  }

  public @Nullable Ip getResponderIp() {
    return _responderIp;
  }

  public @Nonnull IpsecSessionStatus getIpsecSessionStatus() {
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
