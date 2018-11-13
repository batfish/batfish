package org.batfish.datamodel.eigrp;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkConfigurations;

/**
 * Represents a configured EIGRP interface, at the control plane level. Hostname, prefix, and VRF
 * uniquely define the interface and process, even in the presence of misconfiguration.
 */
public class EigrpInterface implements Comparable<EigrpInterface> {

  private final String _hostname;
  private final String _interfaceName;
  private final String _vrfName;

  public EigrpInterface(
      @Nonnull String hostname, @Nonnull String interfaceName, @Nonnull String vrfName) {
    _hostname = hostname;
    _interfaceName = interfaceName;
    _vrfName = vrfName;
  }

  public EigrpInterface(@Nonnull String hostname, @Nonnull Interface iface) {
    this(hostname, iface.getName(), iface.getVrfName());
  }

  @Override
  public int compareTo(@Nonnull EigrpInterface o) {
    return Comparator.comparing(EigrpInterface::getHostname)
        .thenComparing(EigrpInterface::getInterfaceName)
        .thenComparing(EigrpInterface::getVrf)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpInterface)) {
      return false;
    }
    EigrpInterface rhs = (EigrpInterface) o;
    return _hostname.equals(rhs._hostname)
        && _interfaceName.equals(rhs._interfaceName)
        && _vrfName.equals(rhs._vrfName);
  }

  public @Nonnull String getHostname() {
    return _hostname;
  }

  public @Nonnull String getVrf() {
    return _vrfName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interfaceName, _vrfName);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("hostname", _hostname)
        .add("interfaceName", _interfaceName)
        .add("vrfName", _vrfName)
        .toString();
  }

  public @Nonnull Interface getInterface(@Nonnull NetworkConfigurations nc) {
    return nc.getInterface(_hostname, _interfaceName).get();
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nonnull EigrpInterfaceSettings getInterfaceSettings(@Nonnull NetworkConfigurations nc) {
    return requireNonNull(getInterface(nc).getEigrp());
  }
}
