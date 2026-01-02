package org.batfish.datamodel.eigrp;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Uniquely identifies an {@link EigrpNeighborConfig EIGRP neighbor configuration} in the network.
 */
@ParametersAreNonnullByDefault
public class EigrpNeighborConfigId implements Serializable, Comparable<EigrpNeighborConfigId> {

  private static final String PROP_ASN = "asn";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_VRF = "vrf";

  private final long _asn;
  private final @Nonnull String _hostname;
  private final @Nonnull String _interfaceName;
  private final @Nonnull String _vrfName;

  public EigrpNeighborConfigId(long asn, String hostname, String interfaceName, String vrfName) {
    _asn = asn;
    _hostname = hostname;
    _interfaceName = interfaceName;
    _vrfName = vrfName;
  }

  @JsonCreator
  private static EigrpNeighborConfigId create(
      @JsonProperty(PROP_ASN) @Nullable Long asn,
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_INTERFACE) @Nullable String iface,
      @JsonProperty(PROP_VRF) @Nullable String vrf) {
    checkArgument(asn != null, "Missing %s", PROP_ASN);
    checkArgument(hostname != null, "Missing %s", PROP_HOSTNAME);
    checkArgument(iface != null, "Missing %s", PROP_INTERFACE);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    return new EigrpNeighborConfigId(asn, hostname, iface, vrf);
  }

  public EigrpNeighborConfigId(long asn, String hostname, Interface iface) {
    this(asn, hostname, iface.getName(), iface.getVrfName());
  }

  @Override
  public int compareTo(EigrpNeighborConfigId o) {
    return Comparator.comparing(EigrpNeighborConfigId::getHostname)
        .thenComparing(EigrpNeighborConfigId::getInterfaceName)
        .thenComparing(EigrpNeighborConfigId::getVrf)
        .thenComparing(EigrpNeighborConfigId::getAsn)
        .compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpNeighborConfigId)) {
      return false;
    }
    EigrpNeighborConfigId rhs = (EigrpNeighborConfigId) o;
    return _asn == rhs._asn
        && _hostname.equals(rhs._hostname)
        && _interfaceName.equals(rhs._interfaceName)
        && _vrfName.equals(rhs._vrfName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _hostname, _interfaceName, _vrfName);
  }

  @JsonProperty(PROP_ASN)
  public long getAsn() {
    return _asn;
  }

  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @JsonProperty(PROP_VRF)
  public @Nonnull String getVrf() {
    return _vrfName;
  }

  @JsonIgnore
  public @Nonnull Interface getInterface(NetworkConfigurations nc) {
    return nc.getInterface(_hostname, _interfaceName).get();
  }

  @JsonIgnore
  public @Nonnull NodeInterfacePair getNodeInterfacePair() {
    return NodeInterfacePair.of(getHostname(), getInterfaceName());
  }

  @JsonIgnore
  public @Nonnull EigrpInterfaceSettings getInterfaceSettings(NetworkConfigurations nc) {
    return requireNonNull(getInterface(nc).getEigrp());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("hostname", _hostname)
        .add("vrfName", _vrfName)
        .add("asn", _asn)
        .add("interfaceName", _interfaceName)
        .toString();
  }
}
