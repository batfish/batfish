package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;

/** A physical or logical interface */
public class Interface implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Ip _clagBackupIp;
  private @Nullable String _clagBackupIpVrf;
  private @Nullable Ip _clagPeerIp;
  private @Nullable Integer _clagPriority;
  private @Nullable MacAddress _clagSysMac;
  private @Nullable Integer _encapsulationVlan;
  private @Nullable List<InterfaceAddress> _ipAddresses;
  private final @Nonnull String _name;
  private final @Nonnull CumulusInterfaceType _type;
  private @Nullable String _vrf;

  public Interface(String name, CumulusInterfaceType type) {
    _name = name;
    _ipAddresses = new LinkedList<>();
    _type = type;
  }

  public @Nullable Ip getClagBackupIp() {
    return _clagBackupIp;
  }

  public @Nullable String getClagBackupIpVrf() {
    return _clagBackupIpVrf;
  }

  public @Nullable Ip getClagPeerIp() {
    return _clagPeerIp;
  }

  public @Nullable Integer getClagPriority() {
    return _clagPriority;
  }

  public MacAddress getClagSysMac() {
    return _clagSysMac;
  }

  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
  }

  public @Nonnull List<InterfaceAddress> getIpAddresses() {
    return _ipAddresses;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull CumulusInterfaceType getType() {
    return _type;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setClagBackupIp(@Nullable Ip clagBackupIp) {
    _clagBackupIp = clagBackupIp;
  }

  public void setClagBackupIpVrf(@Nullable String clagBackupIpVrf) {
    _clagBackupIpVrf = clagBackupIpVrf;
  }

  public void setClagPeerIp(@Nullable Ip clagPeerIp) {
    _clagPeerIp = clagPeerIp;
  }

  public void setClagPriority(@Nullable Integer clagPriority) {
    _clagPriority = clagPriority;
  }

  public void setClagSysMac(@Nullable MacAddress clagSysMac) {
    _clagSysMac = clagSysMac;
  }

  public void setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
    _encapsulationVlan = encapsulationVlan;
  }

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }
}
