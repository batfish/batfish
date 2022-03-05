package org.batfish.common.topology.bridge_domain.edge;

import com.google.common.collect.ImmutableMap;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain;
import org.batfish.common.topology.bridge_domain.node.BridgedL3Interface;
import org.batfish.common.topology.bridge_domain.node.EthernetHub;
import org.batfish.common.topology.bridge_domain.node.L2Interface;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L2VniHub;
import org.batfish.common.topology.bridge_domain.node.NonBridgedL3Interface;
import org.batfish.common.topology.bridge_domain.node.PhysicalInterface;
import org.batfish.datamodel.IntegerSpace;

/**
 * Utility class containing helper methods for generating all the directed edges between a set of
 * inter-related {@link org.batfish.common.topology.bridge_domain.node.Node}s.
 */
public final class Edges {

  /**
   * Generate edges connecting an {@link EthernetHub} to every one of a list of {@link
   * PhysicalInterface}s.
   */
  public static void connectToHub(EthernetHub hub, PhysicalInterface... phys) {
    for (PhysicalInterface p : phys) {
      hub.addAttachedInterface(p);
      p.attachToHub(hub);
    }
  }

  /**
   * Generate edges connecting an access port {@link L2Interface} to its {@link PhysicalInterface}
   * and the device's vlan-aware {@link BridgeDomain}.
   */
  public static void connectAccessToBridgeDomainAndPhysical(
      int vlan,
      L2Interface l2Interface,
      BridgeDomain bridgeDomain,
      PhysicalInterface physicalInterface,
      Integer accessVlan) {
    l2Interface.connectToBridgeDomain(bridgeDomain, L2ToBridgeDomain.accessToBridgeDomain(vlan));
    bridgeDomain.connectToL2Interface(
        l2Interface, BridgeDomainToL2.bridgeDomainToAccess(accessVlan));
    l2Interface.connectToPhysicalInterface(physicalInterface, L2ToPhysical.accessToPhysical());
    physicalInterface.connectToL2Interface(l2Interface, PhysicalToL2.physicalToAccess());
  }

  /**
   * Generate edges connecting a trunk port {@link L2Interface} to its {@link PhysicalInterface} and
   * the device's vlan-aware {@link BridgeDomain}.
   */
  public static void connectTrunkToBridgeDomainAndPhysical(
      L2Interface l2Interface,
      BridgeDomain bridgeDomain,
      PhysicalInterface physicalInterface,
      @Nullable Integer nativeVlan,
      IntegerSpace allowedVlans) {
    // TODO: support translations
    l2Interface.connectToBridgeDomain(
        bridgeDomain, L2ToBridgeDomain.trunkToBridgeDomain(ImmutableMap.of()));
    // TODO: support translations
    bridgeDomain.connectToL2Interface(
        l2Interface, BridgeDomainToL2.bridgeDomainToTrunk(ImmutableMap.of(), allowedVlans));
    l2Interface.connectToPhysicalInterface(
        physicalInterface, L2ToPhysical.trunkToPhysical(nativeVlan));
    physicalInterface.connectToL2Interface(
        l2Interface, PhysicalToL2.physicalToTrunk(allowedVlans, nativeVlan));
  }

  /** Generate edges connecting a {@link NonBridgedL3Interface} to its {@link PhysicalInterface}. */
  public static void connectNonBridgedL3ToPhysical(
      NonBridgedL3Interface nonBridgedL3Interface,
      PhysicalInterface physicalInterface,
      @Nullable Integer tag) {
    nonBridgedL3Interface.connectToPhysicalInterface(
        physicalInterface, NonBridgedL3ToPhysical.nonBridgedLayer3ToPhysical(tag));
    physicalInterface.connectToNonBridgedL3Interface(
        nonBridgedL3Interface, PhysicalToNonBridgedL3.physicalToNonBridgedL3(tag));
  }

  /**
   * Generate edges connecting an IRB/Vlan {@link BridgedL3Interface} to the device's vlan-aware
   * {@link BridgeDomain}.
   */
  public static void connectIrbToBridgeDomain(
      BridgedL3Interface bridgedL3Interface, BridgeDomain bridgeDomain, int vlan) {
    bridgedL3Interface.connectToBridgeDomain(
        bridgeDomain, BridgedL3ToBridgeDomain.irbToBridgeDomain(vlan));
    bridgeDomain.connectToBridgedL3Interface(
        bridgedL3Interface, BridgeDomainToBridgedL3.bridgeDomainToIrb(vlan));
  }

  /** Generate edges connecting an {@link L2Vni} to a vlan-aware {@link BridgeDomain}. */
  public static void connectVniToVlanAwareBridgeDomain(
      L2Vni vni, BridgeDomain bridgeDomain, int vlan) {
    vni.connectToBridgeDomain(bridgeDomain, L2VniToBridgeDomain.l2VniToVlanAwareBridgeDomain(vlan));
    bridgeDomain.connectToL2Vni(vni, BridgeDomainToL2Vni.vlanAwareBridgeDomainToL2Vni(vlan));
  }

  /** Generate edges connecting an {@link L2VniHub} to every one of a list of {@link L2Vni}s. */
  public static void connectToL2VniHub(L2VniHub l2VniHub, L2Vni... l2Vnis) {
    for (L2Vni l2Vni : l2Vnis) {
      l2VniHub.attachL2Vni(l2Vni);
      l2Vni.connectToL2VniHub(l2VniHub);
    }
  }

  private Edges() {}
}
