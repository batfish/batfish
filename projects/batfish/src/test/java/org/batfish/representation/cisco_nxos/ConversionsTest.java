package org.batfish.representation.cisco_nxos;

import static org.batfish.datamodel.InterfaceType.LOOPBACK;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.InterfaceType.VLAN;
import static org.batfish.representation.cisco_nxos.Conversions.inferRouterId;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class ConversionsTest {

  private static org.batfish.datamodel.Interface createInterface(
      String name, InterfaceType interfaceType, Ip address) {
    return org.batfish.datamodel.Interface.builder()
        .setName(name)
        .setType(interfaceType)
        .setAddress(ConcreteInterfaceAddress.create(address, 24))
        .build();
  }

  @Test
  public void testInferRouterId() {

    org.batfish.datamodel.Interface loopback0 =
        createInterface("loopback0", LOOPBACK, Ip.parse("10.10.10.10"));
    org.batfish.datamodel.Interface loopback1 =
        createInterface("loopback1", LOOPBACK, Ip.parse("9.9.9.9"));
    org.batfish.datamodel.Interface loopback2 =
        createInterface("loopback2", LOOPBACK, Ip.parse("8.8.8.8"));
    org.batfish.datamodel.Interface physical0 =
        createInterface("Ethernet0", PHYSICAL, Ip.parse("7.7.7.7"));
    org.batfish.datamodel.Interface physical1 =
        createInterface("Ethernet1", PHYSICAL, Ip.parse("6.6.6.6"));
    org.batfish.datamodel.Interface other0 = createInterface("Vlan0", VLAN, Ip.parse("5.5.5.5"));
    org.batfish.datamodel.Interface other1 = createInterface("Vlan1", VLAN, Ip.parse("4.4.4.4"));

    // loopback0 wins if present
    assertEquals(
        Ip.parse("10.10.10.10"),
        inferRouterId(
            "test",
            ImmutableMap.<String, Interface>builder()
                .put(loopback0.getName(), loopback0)
                .put(loopback1.getName(), loopback1)
                .put(loopback2.getName(), loopback2)
                .put(physical0.getName(), physical0)
                .put(physical1.getName(), physical1)
                .put(other0.getName(), other0)
                .put(other1.getName(), other1)
                .build(),
            new Warnings(),
            "test"));

    // if loopback0 is missing, lowest loopback wins
    assertEquals(
        Ip.parse("8.8.8.8"),
        inferRouterId(
            "test",
            ImmutableMap.<String, Interface>builder()
                .put(loopback1.getName(), loopback1)
                .put(loopback2.getName(), loopback2)
                .put(physical0.getName(), physical0)
                .put(physical1.getName(), physical1)
                .put(other0.getName(), other0)
                .put(other1.getName(), other1)
                .build(),
            new Warnings(),
            "test"));

    // if loopbacks are missing, lowest physical wins
    assertEquals(
        Ip.parse("6.6.6.6"),
        inferRouterId(
            "test",
            ImmutableMap.<String, Interface>builder()
                .put(physical0.getName(), physical0)
                .put(physical1.getName(), physical1)
                .put(other0.getName(), other0)
                .put(other1.getName(), other1)
                .build(),
            new Warnings(),
            "test"));

    // if loopbacks and physicals are missing, lowest other wins
    assertEquals(
        Ip.parse("4.4.4.4"),
        inferRouterId(
            "test",
            ImmutableMap.<String, Interface>builder()
                .put(other0.getName(), other0)
                .put(other1.getName(), other1)
                .build(),
            new Warnings(),
            "test"));

    // zeroes if nothing is viable
    assertEquals(Ip.ZERO, inferRouterId("test", ImmutableMap.of(), new Warnings(), "test"));
  }
}
