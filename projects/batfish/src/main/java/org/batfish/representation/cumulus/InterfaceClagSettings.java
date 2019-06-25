package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;

/** CLAG settings for a layer-3 interface */
public final class InterfaceClagSettings implements Serializable {

  private @Nullable Ip _backupIp;
  private @Nullable String _backupIpVrf;
  private @Nullable Ip _peerIp;
  private boolean _peerIpLinkLocal;
  private @Nullable Integer _priority;
  private @Nullable MacAddress _sysMac;

  public @Nullable Ip getBackupIp() {
    return _backupIp;
  }

  public @Nullable String getBackupIpVrf() {
    return _backupIpVrf;
  }

  public @Nullable Ip getPeerIp() {
    return _peerIp;
  }

  public boolean isPeerIpLinkLocal() {
    return _peerIpLinkLocal;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public @Nullable MacAddress getSysMac() {
    return _sysMac;
  }

  public void setBackupIp(@Nullable Ip backupIp) {
    _backupIp = backupIp;
  }

  public void setBackupIpVrf(@Nullable String backupIpVrf) {
    _backupIpVrf = backupIpVrf;
  }

  public void setPeerIp(@Nullable Ip peerIp) {
    _peerIp = peerIp;
  }

  public void setPeerIpLinkLocal(boolean peerIpLinkLocal) {
    _peerIpLinkLocal = peerIpLinkLocal;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  public void setSysMac(@Nullable MacAddress sysMac) {
    _sysMac = sysMac;
  }

  public @Nonnull org.batfish.datamodel.vendor_family.cumulus.InterfaceClagSettings toDataModel() {
    return org.batfish.datamodel.vendor_family.cumulus.InterfaceClagSettings.builder()
        .setBackupIp(_backupIp)
        .setBackupIpVrf(_backupIpVrf)
        .setPeerIp(_peerIp)
        .setPeerLinkLocal(_peerIpLinkLocal)
        .setPriority(_priority)
        .setSysMac(_sysMac)
        .build();
  }
}
