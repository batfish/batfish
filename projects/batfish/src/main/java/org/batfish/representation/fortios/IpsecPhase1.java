package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** FortiOS datamodel component containing IPsec Phase 1 configuration */
public final class IpsecPhase1 implements Serializable {
  public static final int DEFAULT_KEYLIFE = 86400;

  public IpsecPhase1(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getInterface() {
    return _interface;
  }

  public @Nullable Ip getRemoteGateway() {
    return _remoteGateway;
  }

  public @Nullable String getProposal() {
    return _proposal;
  }

  public @Nullable String getPskSecret() {
    return _pskSecret;
  }

  public @Nullable String getDhGroups() {
    return _dhGroups;
  }

  public @Nullable Integer getKeylife() {
    return _keylife;
  }

  public int getKeylifeEffective() {
    return firstNonNull(_keylife, DEFAULT_KEYLIFE);
  }

  public void setInterface(String iface) {
    _interface = iface;
  }

  public void setRemoteGateway(Ip remoteGateway) {
    _remoteGateway = remoteGateway;
  }

  public void setProposal(String proposal) {
    _proposal = proposal;
  }

  public void setPskSecret(String pskSecret) {
    _pskSecret = pskSecret;
  }

  public void setDhGroups(String dhGroups) {
    _dhGroups = dhGroups;
  }

  public void setKeylife(Integer keylife) {
    _keylife = keylife;
  }

  private final @Nonnull String _name;
  private @Nullable String _interface;
  private @Nullable Ip _remoteGateway;
  private @Nullable String _proposal;
  private @Nullable String _pskSecret;
  private @Nullable String _dhGroups;
  private @Nullable Integer _keylife;
}
