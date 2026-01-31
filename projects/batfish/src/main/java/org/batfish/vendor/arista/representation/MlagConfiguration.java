package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import org.batfish.datamodel.Ip;

/** Arista-specific MLAG configuration on a single device */
@ParametersAreNullableByDefault
public final class MlagConfiguration implements Serializable {

  private @Nullable Ip _peerAddress;
  private @Nullable Ip _peerAddressHeartbeat;
  private @Nullable String _peerAddressHeartbeatVrfName;
  private @Nullable String _peerLink;
  private @Nullable String _localInterface;
  private @Nullable String _domainId;
  private @Nullable Integer _reloadDelayMlag;
  private @Nullable Integer _reloadDelayNonMlag;
  private boolean _reloadDelayStandbyMode;
  private boolean _shutdown;

  public @Nullable Ip getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(Ip peerAddress) {
    _peerAddress = peerAddress;
  }

  public @Nullable Ip getPeerAddressHeartbeat() {
    return _peerAddressHeartbeat;
  }

  public void setPeerAddressHeartbeat(Ip address) {
    _peerAddressHeartbeat = address;
  }

  public @Nullable String getPeerAddressHeartbeatVrfName() {
    return _peerAddressHeartbeatVrfName;
  }

  public void setPeerAddressHeartbeatVrf(String vrfName) {
    _peerAddressHeartbeatVrfName = vrfName;
  }

  public @Nullable String getPeerLink() {
    return _peerLink;
  }

  public void setPeerLink(String peerLink) {
    _peerLink = peerLink;
  }

  public @Nullable String getLocalInterface() {
    return _localInterface;
  }

  public void setLocalInterface(String localInterface) {
    _localInterface = localInterface;
  }

  public @Nullable String getDomainId() {
    return _domainId;
  }

  public void setDomainId(String domainId) {
    _domainId = domainId;
  }

  public @Nullable Integer getReloadDelayMlag() {
    return _reloadDelayMlag;
  }

  public void setReloadDelayMlag(Integer reloadDelayMlag) {
    _reloadDelayMlag = reloadDelayMlag;
  }

  public @Nullable Integer getReloadDelayNonMlag() {
    return _reloadDelayNonMlag;
  }

  public void setReloadDelayNonMlag(Integer reloadDelayNonMlag) {
    _reloadDelayNonMlag = reloadDelayNonMlag;
  }

  public boolean isReloadDelayStandbyMode() {
    return _reloadDelayStandbyMode;
  }

  public void setReloadDelayStandbyMode(boolean reloadDelayStandbyMode) {
    _reloadDelayStandbyMode = reloadDelayStandbyMode;
  }

  public boolean isShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }
}
