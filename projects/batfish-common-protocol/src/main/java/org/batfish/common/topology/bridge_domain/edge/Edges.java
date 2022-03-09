package org.batfish.common.topology.bridge_domain.edge;

import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.EthernetTag;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2.AccessMode;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2.Trunk;
import org.batfish.common.topology.bridge_domain.function.Identity;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain;
import org.batfish.common.topology.bridge_domain.node.EthernetHub;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L2VniHub;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.PhysicalInterface;
import org.batfish.datamodel.IntegerSpace;

public final class Edges {

  public static void connectIRB(L3Interface l3, BridgeDomain sw, int vlan) {
    l3.sendThroughSwitch(sw, new BridgedL3ToBridgeDomain(vlan));
    sw.deliverToL3(l3, new BridgeDomainToBridgedL3(vlan));
  }

  public static void connectL3Untagged(L3Interface l3, PhysicalInterface phys) {
    l3.sendDirectlyOutIface(phys, new NonBridgedL3ToPhysical(EthernetTag.untagged()));
    phys.deliverDirectlyToInterface(l3, new PhysicalToNonBridgedL3(EthernetTag.untagged()));
  }

  public static void connectL3Dot1q(L3Interface l3, PhysicalInterface phys, int vlan) {
    l3.sendDirectlyOutIface(phys, new NonBridgedL3ToPhysical(EthernetTag.tagged(vlan)));
    phys.deliverDirectlyToInterface(l3, new PhysicalToNonBridgedL3(EthernetTag.tagged(vlan)));
  }

  public static void connectInAccessMode(int vlan, PhysicalInterface phys, BridgeDomain sw) {
    AccessMode am = new AccessMode(vlan);
    phys.deliverToSwitch(sw, am::receiveTag);
    sw.transmitOutPhysical(phys, am::sendFromVlan);
  }

  public static void connectTrunk(
      PhysicalInterface phys,
      BridgeDomain sw,
      IntegerSpace allowedVlans,
      @Nullable Integer nativeVlan) {
    Trunk trunk = new Trunk(allowedVlans, nativeVlan);
    phys.deliverToSwitch(sw, trunk::receiveTag);
    sw.transmitOutPhysical(phys, trunk::sendFromVlan);
  }

  public static void connectToHub(EthernetHub hub, PhysicalInterface... phys) {
    for (PhysicalInterface p : phys) {
      hub.addAttachedInterface(p, Identity.get());
      p.attachToHub(hub, Identity.get());
    }
  }

  public static void connectToL2VNIHub(L2VniHub hub, L2Vni... vnis) {
    for (L2Vni v : vnis) {
      hub.attachL2VNI(v, Identity.get());
      v.attachToHub(hub, Identity.get());
    }
  }

  private Edges() {} // prevent instantiation
}
