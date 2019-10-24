package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;

/** Centralized management device */
public final class Device implements Serializable {

  public Device(String name) {
    _name = name;
    _unicastAddresses = new LinkedList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable MacAddress getBaseMac() {
    return _baseMac;
  }

  public void setBaseMac(@Nullable MacAddress baseMac) {
    _baseMac = baseMac;
  }

  public @Nullable Ip getConfigSyncIp() {
    return _configSyncIp;
  }

  public void setConfigSyncIp(@Nullable Ip configSyncIp) {
    _configSyncIp = configSyncIp;
  }

  public @Nullable String getHostname() {
    return _hostname;
  }

  public void setHostname(@Nullable String hostname) {
    _hostname = hostname;
  }

  public @Nullable Ip getManagementIp() {
    return _managementIp;
  }

  public void setManagementIp(@Nullable Ip managementIp) {
    _managementIp = managementIp;
  }

  public @Nullable Boolean getSelfDevice() {
    return _selfDevice;
  }

  public void setSelfDevice(@Nullable Boolean selfDevice) {
    _selfDevice = selfDevice;
  }

  public @Nonnull List<UnicastAddress> getUnicastAddresses() {
    return _unicastAddresses;
  }

  private @Nullable MacAddress _baseMac;
  private @Nullable Ip _configSyncIp;
  private @Nullable String _hostname;
  private @Nullable Ip _managementIp;
  private final @Nonnull String _name;
  private @Nullable Boolean _selfDevice;
  private final @Nonnull List<UnicastAddress> _unicastAddresses;
}
