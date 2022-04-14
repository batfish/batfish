package org.batfish.datamodel.topology;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L2Interface;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain;

/**
 * Data model for the bridging topology within a device.
 *
 * <p>See also {@link InterfaceTopology}
 */
public final class DeviceTopology {

  public static @Nonnull DeviceTopology of(
      Map<String, L1Interface> l1Interfaces,
      Map<String, L2Interface> l2Interfaces,
      Map<Integer, L2Vni> l2Vnis,
      Map<String, L3Interface> l3Interfaces,
      Map<String, VlanAwareBridgeDomain> vlanAwareBridgeDomains,
      Map<String, NonVlanAwareBridgeDomain> nonVlanAwareBridgeDomains) {
    return new DeviceTopology(
        ImmutableMap.copyOf(l1Interfaces),
        ImmutableMap.copyOf(l2Interfaces),
        ImmutableMap.copyOf(l2Vnis),
        ImmutableMap.copyOf(l3Interfaces),
        ImmutableMap.copyOf(vlanAwareBridgeDomains),
        ImmutableMap.copyOf(nonVlanAwareBridgeDomains));
  }

  public @Nonnull Map<String, L1Interface> getL1Interfaces() {
    return _l1Interfaces;
  }

  public @Nonnull Map<String, L2Interface> getL2Interfaces() {
    return _l2Interfaces;
  }

  public @Nonnull Map<Integer, L2Vni> getL2Vnis() {
    return _l2Vnis;
  }

  public @Nonnull Map<String, L3Interface> getL3Interfaces() {
    return _l3Interfaces;
  }

  public @Nonnull Map<String, VlanAwareBridgeDomain> getVlanAwareBridgeDomains() {
    return _vlanAwareBridgeDomains;
  }

  public @Nonnull Map<String, NonVlanAwareBridgeDomain> getNonVlanAwareBridgeDomains() {
    return _nonVlanAwareBridgeDomains;
  }

  private DeviceTopology(
      Map<String, L1Interface> l1Interfaces,
      Map<String, L2Interface> l2Interfaces,
      Map<Integer, L2Vni> l2Vnis,
      Map<String, L3Interface> l3Interfaces,
      Map<String, VlanAwareBridgeDomain> vlanAwareBridgeDomains,
      Map<String, NonVlanAwareBridgeDomain> nonVlanAwareBridgeDomains) {
    _l1Interfaces = l1Interfaces;
    _l2Interfaces = l2Interfaces;
    _l2Vnis = l2Vnis;
    _l3Interfaces = l3Interfaces;
    _vlanAwareBridgeDomains = vlanAwareBridgeDomains;
    _nonVlanAwareBridgeDomains = nonVlanAwareBridgeDomains;
  }

  private final @Nonnull Map<String, L1Interface> _l1Interfaces;
  private final @Nonnull Map<String, L2Interface> _l2Interfaces;
  private final @Nonnull Map<Integer, L2Vni> _l2Vnis;
  private final @Nonnull Map<String, L3Interface> _l3Interfaces;
  private final @Nonnull Map<String, VlanAwareBridgeDomain> _vlanAwareBridgeDomains;
  private final @Nonnull Map<String, NonVlanAwareBridgeDomain> _nonVlanAwareBridgeDomains;
}
