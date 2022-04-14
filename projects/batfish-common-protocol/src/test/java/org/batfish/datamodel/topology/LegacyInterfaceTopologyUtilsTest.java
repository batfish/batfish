package org.batfish.datamodel.topology;

import static org.batfish.datamodel.InterfaceType.LOGICAL;
import static org.batfish.datamodel.InterfaceType.LOOPBACK;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.InterfaceType.TUNNEL;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.accessPortSettings;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.findCorrespondingLogicalL1Interface;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.irbL3Settings;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.isLogicalL1Interface;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.legacyCreateLayer2Settings;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.legacyCreateLayer3Settings;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.nonBridgedL3Settings;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.shouldCreateLayer3Settings;
import static org.batfish.datamodel.topology.LegacyInterfaceTopologyUtils.trunkPortSettings;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SwitchportMode;
import org.junit.Test;

/** Test of {@link LegacyInterfaceTopologyUtils}. */
public final class LegacyInterfaceTopologyUtilsTest {

  @Test
  public void testFindCorrespondingLogicalL1Interface_physical() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    // Correct
    assertThat(
        findCorrespondingLogicalL1Interface(physical, c.getAllInterfaces()),
        equalTo(Optional.of(physical)));
  }

  @Test
  public void testFindCorrespondingLogicalL1Interface_subinterface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();

    // Correct
    Interface subif =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setDependencies(dependsOn(physical))
            .build();
    assertThat(
        findCorrespondingLogicalL1Interface(subif, c.getAllInterfaces()),
        equalTo(Optional.of(physical)));

    // Dep not in configuration
    assertThat(
        findCorrespondingLogicalL1Interface(subif, ImmutableMap.of(subif.getName(), subif)),
        equalTo(Optional.empty()));
    // Dep not l1 interface
    Interface depOnNonL1 =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setDependencies(dependsOn(subif))
            .build();

    assertThat(
        findCorrespondingLogicalL1Interface(depOnNonL1, c.getAllInterfaces()),
        equalTo(Optional.empty()));

    // No deps
    Interface subifNoDeps = nf.interfaceBuilder().setOwner(c).setType(LOGICAL).build();
    assertThat(
        findCorrespondingLogicalL1Interface(subifNoDeps, c.getAllInterfaces()),
        equalTo(Optional.empty()));
  }

  @Test
  public void testIsLogicalL1Interface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    assertTrue(
        "physical should be l1",
        isLogicalL1Interface(
            nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).setAdminUp(true).build()));
    assertFalse(
        "physical but aggregated should not be l1",
        isLogicalL1Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setChannelGroup("Port-Channel1")
                .setAdminUp(true)
                .build()));
    assertFalse(
        "logical is not l1",
        isLogicalL1Interface(nf.interfaceBuilder().setOwner(c).setType(LOGICAL).build()));
    assertFalse(
        "vlan should not be l1",
        isLogicalL1Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.VLAN)
                .setAdminUp(true)
                .build()));
    assertTrue(
        "aggregate should be l1",
        isLogicalL1Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.AGGREGATED)
                .setAdminUp(true)
                .build()));
    assertFalse(
        "aggregate child should not be l1",
        isLogicalL1Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.AGGREGATE_CHILD)
                .setAdminUp(true)
                .build()));
  }

  @Test
  public void tesShouldCreateLayer3Settings() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    assertTrue(
        "l3 should be created",
        shouldCreateLayer3Settings(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAddress(CONCRETE)
                .setAdminUp(true)
                .build()));
    assertTrue(
        "l3 with LLA should be created",
        shouldCreateLayer3Settings(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
                .setAdminUp(true)
                .build()));
    assertFalse(
        "l3 with no addresses should not be created",
        shouldCreateLayer3Settings(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAdminUp(true)
                .build()));
    assertFalse(
        "l3 shutdown should not be created",
        shouldCreateLayer3Settings(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAddress(CONCRETE)
                .setAdminUp(false)
                .build()));
    assertFalse(
        "l3 loopback should not be created",
        shouldCreateLayer3Settings(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(LOOPBACK)
                .setSwitchport(false)
                .setAddress(CONCRETE)
                .setAdminUp(true)
                .build()));
    assertFalse(
        "l3 in weird l3/switchport mode should not be created",
        shouldCreateLayer3Settings(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(true)
                .setSwitchportMode(SwitchportMode.ACCESS)
                .setAddress(CONCRETE)
                .setAdminUp(true)
                .build()));
  }

  @Test
  public void testLegacyCreateLayer2Settings_l3() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface l3 =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAddress(CONCRETE)
            .setType(PHYSICAL)
            .setSwitchport(false)
            .setSwitchportMode(SwitchportMode.NONE)
            .build();

    // Since L3 interface, no edges should be established.
    assertThat(legacyCreateLayer2Settings(l3, c.getAllInterfaces()), equalTo(Optional.empty()));
  }

  @Test
  public void testLegacyCreateLayer2Settings_access() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface access =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(PHYSICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(3)
            .build();
    {
      // Access settings should be created
      assertThat(
          legacyCreateLayer2Settings(access, c.getAllInterfaces()),
          equalTo(Optional.of(accessPortSettings(3, access.getName()))));
    }
    {
      // Missing access vlan should yield no result
      access.setAccessVlan(null);
      assertThat(
          legacyCreateLayer2Settings(access, c.getAllInterfaces()), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testLegacyCreateLayer2Settings_trunk() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface trunk =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(PHYSICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.TRUNK)
            .setAllowedVlans(IntegerSpace.of(3))
            .setNativeVlan(3)
            .build();
    // Trunk settings should be created
    assertThat(
        legacyCreateLayer2Settings(trunk, c.getAllInterfaces()),
        equalTo(Optional.of(trunkPortSettings(3, IntegerSpace.of(3), trunk.getName()))));
  }

  @Test
  public void testLegacyCreateLayer2Settings_subif_access() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    Interface subif =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setDependencies(dependsOn(physical))
            .setAccessVlan(3)
            .build();

    {
      // Should create access settings attached to physical iface
      assertThat(
          legacyCreateLayer2Settings(subif, c.getAllInterfaces()),
          equalTo(Optional.of(accessPortSettings(3, physical.getName()))));
    }
    {
      // Clear the dependency, should not create l2 settings
      subif.setDependencies(ImmutableSet.of());
      assertThat(
          legacyCreateLayer2Settings(subif, c.getAllInterfaces()), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testLegacyCreateLayer2Settings_unknown_mode() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface unhandledMode =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(PHYSICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.MONITOR)
            .build();
    assertThat(
        legacyCreateLayer2Settings(unhandledMode, c.getAllInterfaces()), equalTo(Optional.empty()));
  }

  @Test
  public void testLegacyCreateLayer3Settings_physical() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical =
        nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).setAddress(CONCRETE).build();
    {
      // Should create non-bridged settings
      assertThat(
          legacyCreateLayer3Settings(physical, c.getAllInterfaces()),
          equalTo(Optional.of(nonBridgedL3Settings(null, physical.getName()))));
    }
    {
      // Should not create settings if not valid l3
      physical.setAllAddresses(ImmutableSet.of());
      assertThat(
          legacyCreateLayer3Settings(physical, c.getAllInterfaces()), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testLegacyCreateLayer3Settings_subif() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    Interface subif =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setAddress(CONCRETE)
            .setDependencies(dependsOn(physical))
            .setEncapsulationVlan(3)
            .build();
    {
      // Should create non-bridged settings
      assertThat(
          legacyCreateLayer3Settings(subif, c.getAllInterfaces()),
          equalTo(Optional.of(nonBridgedL3Settings(3, physical.getName()))));
    }
    {
      // Parent is missing on config
      assertThat(
          legacyCreateLayer3Settings(subif, ImmutableMap.of(subif.getName(), subif)),
          equalTo(Optional.empty()));
    }
    {
      // Subif handles untagged frames
      subif.setEncapsulationVlan(null);
      assertThat(
          legacyCreateLayer3Settings(subif, c.getAllInterfaces()),
          equalTo(Optional.of(nonBridgedL3Settings(null, physical.getName()))));
    }
  }

  @Test
  public void testLegacyCreateLayer3Settings_vlan() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface vlan =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(InterfaceType.VLAN)
            .setAddress(CONCRETE)
            .setVlan(4)
            .build();
    {
      // Connect to domain and not physical interface
      // Should not create physical iface for VLAN, but ensure the connection doesn't happen anyway.
      assertThat(
          legacyCreateLayer3Settings(vlan, c.getAllInterfaces()),
          equalTo(Optional.of(irbL3Settings(4))));
    }
    {
      // Missing VLAN
      vlan.setVlan(null);
      assertThat(legacyCreateLayer3Settings(vlan, c.getAllInterfaces()), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testConnectL3InterfaceToPhysicalOrDomain_tunnel() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface tunnel =
        nf.interfaceBuilder().setOwner(c).setType(TUNNEL).setAddress(CONCRETE).build();
    {
      assertThat(
          legacyCreateLayer3Settings(tunnel, c.getAllInterfaces()),
          equalTo(Optional.of(Layer3TunnelSettings.instance())));
    }
  }

  private static Set<Dependency> dependsOn(Interface i) {
    return ImmutableSet.of(new Dependency(i.getName(), DependencyType.BIND));
  }

  private static final InterfaceAddress CONCRETE = ConcreteInterfaceAddress.parse("1.2.3.4/24");
}
