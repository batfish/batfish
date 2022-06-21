package org.batfish.datamodel.topology;

import static org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain.DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME;
import static org.batfish.datamodel.InterfaceType.AGGREGATED;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.InterfaceType.REDUNDANT;
import static org.batfish.datamodel.topology.Layer3NonBridgedSettings.encapsulation;
import static org.batfish.datamodel.topology.Layer3NonBridgedSettings.noEncapsulation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.bridge_domain.edge.L1ToL2;
import org.batfish.common.topology.bridge_domain.edge.L2ToL1;
import org.batfish.common.topology.bridge_domain.edge.L2ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L3ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL3;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility class with helper methods for constructing {@link InterfaceTopology}. */
public final class LegacyInterfaceTopologyUtils {

  private static final Logger LOGGER = LogManager.getLogger(LegacyInterfaceTopologyUtils.class);

  /**
   * Helper that populates all interfaces of {@code c} with a legacy {@link InterfaceTopology}.
   *
   * <p>No new calls to this method should be made.
   */
  public static void legacyPopulateInterfaceTopologies(Configuration c) {
    Map<String, Interface> allInterfaces = c.getAllInterfaces();
    for (Interface i : allInterfaces.values()) {
      i.setTopology(computeLegacyInterfaceTopology(i));
    }
  }

  /**
   * Helper that infers {@link InterfaceTopology} for {@code i} based on:
   *
   * <ul>
   *   <li>access vlan
   *   <li>allowed vlans
   *   <li>native vlan
   *   <li>encapuslation vlan
   *   <li>presence of an IP address
   *   <li>interface type
   *   <li>bind dependencies
   *   <li>interface type
   * </ul>
   *
   * <p>No new calls to this method should be made.
   */
  public static @Nonnull InterfaceTopology computeLegacyInterfaceTopology(Interface i) {
    InterfaceTopology.Builder builder =
        InterfaceTopology.builder().setLogicalLayer1(isLogicalL1Interface(i));
    Map<String, Interface> allInterfaces = i.getOwner().getAllInterfaces();
    legacyCreateLayer2Settings(i, allInterfaces).ifPresent(builder::setLayer2Settings);
    legacyCreateLayer3Settings(i, allInterfaces).ifPresent(builder::setLayer3Settings);
    return builder.build();
  }

  @VisibleForTesting
  static @Nonnull Optional<Layer2Settings> legacyCreateLayer2Settings(
      Interface i, Map<String, Interface> allInterfaces) {
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (!i.getSwitchport()) {
      LOGGER.debug("Skipping non-L2 interface {}: switchport is not set", nip);
      return Optional.empty();
    }

    // Identify the L1 interface corresponding to this L2 interface.
    Optional<Interface> maybeL1 = findCorrespondingLogicalL1Interface(i, allInterfaces);
    if (!maybeL1.isPresent()) {
      // Already warned/logged inside the prior function.
      return Optional.empty();
    }
    String l1Interface = maybeL1.get().getName();

    // Connect L2 interface to domain and physical interface based on L2 config.
    if (i.getSwitchportMode() == SwitchportMode.ACCESS) {
      Integer vlan = i.getAccessVlan();
      if (vlan == null) {
        LOGGER.warn("Skipping L2 connection for {}: access mode vlan is missing", nip);
        return Optional.empty();
      }
      return Optional.of(accessPortSettings(vlan, l1Interface));
    } else if (i.getSwitchportMode() == SwitchportMode.TRUNK) {
      assert i.getAllowedVlans() != null;
      return Optional.of(trunkPortSettings(i.getNativeVlan(), i.getAllowedVlans(), l1Interface));
    } else {
      // This is the end of legacy support. Other L2 methods (and even the above eventually) should
      // be handled directly in conversion.
      LOGGER.warn("Unsupported L2 interface {}: unsure how to connect", nip);
      return Optional.empty();
    }
  }

  /**
   * Returns the logical layer-1 interface corresponding to the given {@link Interface}, if one
   * exists. This function returns a present {@link Optional} if {@code i} is a logical layer-1
   * interface already, or if {@code i} is a correctly configured subinterface.
   *
   * <p>In other cases (virtual interface like Vlan, missing parent interface, etc.) the return
   * value will be {@link Optional#empty()}.
   */
  @VisibleForTesting
  static @Nonnull Optional<Interface> findCorrespondingLogicalL1Interface(
      Interface i, Map<String, Interface> allInterfaces) {
    if (isLogicalL1Interface(i)) {
      return Optional.of(i);
    }
    Optional<Dependency> parentDep =
        i.getDependencies().stream().filter(d -> d.getType() == DependencyType.BIND).findFirst();
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (!parentDep.isPresent()) {
      LOGGER.debug("No corresponding physical interface found for {}", nip);
      return Optional.empty();
    }
    String parentName = parentDep.get().getInterfaceName();
    NodeInterfacePair parentNip = NodeInterfacePair.of(nip.getHostname(), parentName);
    if (!allInterfaces.containsKey(parentName)) {
      LOGGER.warn("Subinterface {}: missing parent {}, skipping", nip, parentNip);
      return Optional.empty();
    }
    Interface parent = allInterfaces.get(parentName);
    if (!isLogicalL1Interface(parent)) {
      LOGGER.debug("Subinterface {}: parent {} is not a logical layer-1 interface", nip, parentNip);
      return Optional.empty();
    } else {
      return Optional.of(parent);
    }
  }

  /**
   * Returns whether this {@link Interface} should be modeled as a logical layer-1 interface. Only
   * interfaces that could be in the logical {@link Layer1Topology} are included -- physical
   * interfaces and aggregated interfaces.
   */
  @VisibleForTesting
  static boolean isLogicalL1Interface(Interface i) {
    return L1_INTERFACE_TYPES.contains(i.getInterfaceType()) && !isAggregated(i);
  }

  private static boolean isAggregated(Interface i) {
    return i.getChannelGroup() != null;
  }

  /** Generate layer-2 settings for a traditional access-mode switchport. */
  public static @Nonnull Layer2Settings accessPortSettings(int accessVlan, String l1Interface) {
    L2ToL1 l2ToL1 = L2ToL1.accessToL1();
    L1ToL2 l1ToL2 = L1ToL2.l1ToAccess();
    Layer2BridgeSettings bridgeSettings = accessPortBridgeSettings(accessVlan);
    return Layer2Settings.of(l1Interface, l2ToL1, l1ToL2, ImmutableSet.of(bridgeSettings));
  }

  /** Generate layer-2 bridge settings for a traditional access-mode switchport. */
  @VisibleForTesting
  static @Nonnull Layer2VlanAwareBridgeSettings accessPortBridgeSettings(int accessVlan) {
    return Layer2VlanAwareBridgeSettings.of(
        DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME,
        VlanAwareBridgeDomainToL2.bridgeDomainToAccess(accessVlan),
        L2ToVlanAwareBridgeDomain.accessToBridgeDomain(accessVlan));
  }

  /** Generate layer-2 settings for a traditional trunk-mode switchport. */
  public static @Nonnull Layer2Settings trunkPortSettings(
      @Nullable Integer nativeVlan, IntegerSpace allowedVlans, String l1Interface) {
    L2ToL1 l2ToL1 = L2ToL1.trunkToL1(nativeVlan);
    L1ToL2 l1ToL2 = L1ToL2.l1ToTrunk(allowedVlans, nativeVlan);
    Layer2BridgeSettings bridgeSettings = trunkPortBridgeSettings(allowedVlans, nativeVlan);
    return Layer2Settings.of(l1Interface, l2ToL1, l1ToL2, ImmutableSet.of(bridgeSettings));
  }

  /** Generate layer-2 bridge settings for a traditional trunk-mode switchport. */
  private static @Nonnull Layer2BridgeSettings trunkPortBridgeSettings(
      IntegerSpace allowedVlans, @Nullable Integer nativeVlan) {
    return Layer2VlanAwareBridgeSettings.of(
        DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME,
        // TODO: support translations
        VlanAwareBridgeDomainToL2.bridgeDomainToTrunk(ImmutableMap.of(), allowedVlans),
        // TODO: support translations
        L2ToVlanAwareBridgeDomain.trunkToBridgeDomain(allowedVlans, nativeVlan, ImmutableMap.of()));
  }

  @VisibleForTesting
  static @Nonnull Optional<Layer3Settings> legacyCreateLayer3Settings(
      Interface i, Map<String, Interface> allInterfaces) {
    if (isTunnelInterface(i)) {
      // These interfaces do not use L2 broadcast domains / adjacency to establish edges
      return Optional.of(Layer3TunnelSettings.instance());
    }
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (!shouldCreateLayer3Settings(i)) {
      return Optional.empty();
    }
    if (isLogicalL1Interface(i)) {
      // This is a logical layer-1 interface with an IP address.
      // Either it has encapsulation or it sends out untagged.
      Integer encapsulationVlan = i.getEncapsulationVlan();
      if (encapsulationVlan == null) {
        LOGGER.debug("L3 interface {} connected to l1 interface {} untagged", nip, nip);
      } else {
        LOGGER.debug(
            "L3 interface {} connected to l1 interface {} in vlan {}",
            nip,
            nip,
            i.getEncapsulationVlan());
      }
      return Optional.of(nonBridgedL3Settings(encapsulationVlan, i.getName()));
    }

    Optional<Dependency> parent =
        i.getDependencies().stream().filter(d -> d.getType() == DependencyType.BIND).findFirst();
    if (parent.isPresent()) {
      String parentName = parent.get().getInterfaceName();
      NodeInterfacePair parentNip = NodeInterfacePair.of(nip.getHostname(), parentName);
      Interface parentIface = allInterfaces.get(parentName);
      if (parentIface == null) {
        LOGGER.warn("Not connecting L3 interface {} to parent: {} not found", nip, parentNip);
        return Optional.empty();
      }
      assert isLogicalL1Interface(parentIface);
      Integer encapsulationVlan = i.getEncapsulationVlan();
      if (encapsulationVlan == null) {
        LOGGER.debug("L3 interface {} connected to physical interface {} untagged", nip, parentNip);
      } else {
        LOGGER.debug(
            "Connecting L3 interface {} to physical interface {} in vlan {}",
            nip,
            parentNip,
            i.getEncapsulationVlan());
      }
      return Optional.of(nonBridgedL3Settings(encapsulationVlan, parentName));
    }

    if (i.getInterfaceType() == InterfaceType.VLAN) {
      Integer vlan = i.getVlan();
      if (vlan == null) {
        LOGGER.warn("Not connecting L3 interface {}: surprised vlan is not set", nip);
        return Optional.empty();
      }
      LOGGER.debug("Connecting L3 interface {} to vlan-aware bridge domain in vlan {}", nip, vlan);
      return Optional.of(irbL3Settings(vlan));
    }
    // This is the end of legacy support. Anything not supported above must be handled directly
    // in conversion.
    LOGGER.warn(
        "Surprised by L3 interface {} of type {}: unsure how to connect",
        nip,
        i.getInterfaceType());
    return Optional.empty();
  }

  private static boolean isTunnelInterface(Interface i) {
    return TUNNEL_INTERFACE_TYPES.contains(i.getInterfaceType());
  }

  @VisibleForTesting
  static @Nonnull Layer3VlanAwareBridgeSettings irbL3Settings(Integer vlan) {
    VlanAwareBridgeDomainToL3 fromBridgeDomain = VlanAwareBridgeDomainToL3.bridgeDomainToIrb(vlan);
    L3ToVlanAwareBridgeDomain toBridgeDomain = L3ToVlanAwareBridgeDomain.irbToBridgeDomain(vlan);
    return Layer3VlanAwareBridgeSettings.of(
        DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME, fromBridgeDomain, toBridgeDomain);
  }

  @VisibleForTesting
  static @Nonnull Layer3NonBridgedSettings nonBridgedL3Settings(
      @Nullable Integer encapsulationVlan, String l1Interface) {
    return encapsulationVlan == null
        ? noEncapsulation(l1Interface)
        : encapsulation(l1Interface, encapsulationVlan);
  }

  @VisibleForTesting
  static boolean shouldCreateLayer3Settings(Interface i) {
    NodeInterfacePair nip = NodeInterfacePair.of(i);
    if (i.getAllAddresses().isEmpty()) {
      LOGGER.debug("Not creating L3 settings {}: no addresses", nip);
      return false;
    } else if (!i.getActive()) {
      LOGGER.debug("Not creating L3 settings {}: not active", nip);
      return false;
    } else if (i.getInterfaceType() == InterfaceType.LOOPBACK) {
      LOGGER.debug("Skipping L3 settings {}: loopback", nip);
      return false;
    } else if (i.getSwitchport()) {
      LOGGER.warn("Skipping L3 settings {}: has switchport set to true", nip);
      return false;
    }
    LOGGER.debug("Created L3 settings for {} with addresses {}", nip, i.getAllAddresses());
    return true;
  }

  private static final @Nonnull EnumSet<InterfaceType> L1_INTERFACE_TYPES =
      EnumSet.of(PHYSICAL, AGGREGATED, REDUNDANT);

  private static final Set<InterfaceType> TUNNEL_INTERFACE_TYPES =
      EnumSet.of(InterfaceType.TUNNEL, InterfaceType.VPN);

  private LegacyInterfaceTopologyUtils() {}
}
