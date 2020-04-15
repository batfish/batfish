package org.batfish.representation.arista;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import org.batfish.datamodel.Ip;

/** Arista-specific MLAG configuration on a single device */
@ParametersAreNullableByDefault
public final class MlagConfiguration implements Serializable {

  @Nullable private Ip _peerAddress;
  @Nullable private Ip _peerAddressHeartbeat;
  @Nullable private String _peerLink;
  @Nullable private String _localInterface;
  @Nullable private String _domainId;
  @Nullable private Integer _reloadDelayMlag;
  @Nullable private Integer _reloadDelayNonMlag;
  private boolean _reloadDelayStandbyMode;
  private boolean _shutdown;

  @Nullable
  public Ip getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(Ip peerAddress) {
    _peerAddress = peerAddress;
  }

  @Nullable
  public Ip getPeerAddressHeartbeat() {
    return _peerAddressHeartbeat;
  }

  public void setPeerAddressHeartbeat(Ip address) {
    _peerAddressHeartbeat = address;
  }

  @Nullable
  public String getPeerLink() {
    return _peerLink;
  }

  public void setPeerLink(String peerLink) {
    _peerLink = peerLink;
  }

  @Nullable
  public String getLocalInterface() {
    return _localInterface;
  }

  public void setLocalInterface(String localInterface) {
    _localInterface = localInterface;
  }

  @Nullable
  public String getDomainId() {
    return _domainId;
  }

  public void setDomainId(String domainId) {
    _domainId = domainId;
  }

  @Nullable
  public Integer getReloadDelayMlag() {
    return _reloadDelayMlag;
  }

  public void setReloadDelayMlag(Integer reloadDelayMlag) {
    _reloadDelayMlag = reloadDelayMlag;
  }

  @Nullable
  public Integer getReloadDelayNonMlag() {
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
