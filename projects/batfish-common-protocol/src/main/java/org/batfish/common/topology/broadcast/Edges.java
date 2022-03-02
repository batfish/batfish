package org.batfish.common.topology.broadcast;

import javax.annotation.Nullable;
import org.batfish.common.topology.broadcast.DomainToL2Interface.AccessMode;
import org.batfish.common.topology.broadcast.DomainToL2Interface.Trunk;
import org.batfish.datamodel.IntegerSpace;

public final class Edges {

  public static void connectIRB(L3Interface l3, DeviceBroadcastDomain sw, int vlan) {
    l3.sendThroughSwitch(sw, new OriginateInVlan(vlan));
    sw.deliverToL3(l3, new DeliverFromVlan(vlan));
  }

  public static void connectL3Untagged(L3Interface l3, PhysicalInterface phys) {
    l3.sendDirectlyOutIface(phys, new OriginateWithTag(EthernetTag.untagged()));
    phys.deliverDirectlyToInterface(l3, new DeliverTaggedFrames(EthernetTag.untagged()));
  }

  public static void connectL3Dot1q(L3Interface l3, PhysicalInterface phys, int vlan) {
    l3.sendDirectlyOutIface(phys, new OriginateWithTag(EthernetTag.tagged(vlan)));
    phys.deliverDirectlyToInterface(l3, new DeliverTaggedFrames(EthernetTag.tagged(vlan)));
  }

  public static void connectInAccessMode(
      int vlan, PhysicalInterface phys, DeviceBroadcastDomain sw) {
    AccessMode am = new AccessMode(vlan);
    phys.deliverToSwitch(sw, am::receiveTag);
    sw.transmitOutPhysical(phys, am::sendFromVlan);
  }

  public static void connectTrunk(
      PhysicalInterface phys,
      DeviceBroadcastDomain sw,
      IntegerSpace allowedVlans,
      @Nullable Integer nativeVlan) {
    Trunk trunk = new Trunk(allowedVlans, nativeVlan);
    phys.deliverToSwitch(sw, trunk::receiveTag);
    sw.transmitOutPhysical(phys, trunk::sendFromVlan);
  }

  public static void connectToHub(EthernetHub hub, PhysicalInterface... phys) {
    for (PhysicalInterface p : phys) {
      hub.addAttachedInterface(p, Preserve.get());
      p.attachToHub(hub, Preserve.get());
    }
  }

  public static void connectToL2VNIHub(L2VNIHub hub, L2VNI... vnis) {
    for (L2VNI v : vnis) {
      hub.attachL2VNI(v, Preserve.get());
      v.attachToHub(hub, Preserve.get());
    }
  }

  private Edges() {} // prevent instantiation
}
