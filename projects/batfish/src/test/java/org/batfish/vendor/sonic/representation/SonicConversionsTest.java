package org.batfish.vendor.sonic.representation;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.SONIC;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAccessVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasHumanName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertVlans;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.sonic.representation.VlanMember.TaggingMode;
import org.junit.Test;

public class SonicConversionsTest {

  @Test
  public void testConvertPorts() {
    Port port =
        Port.builder()
            .setAdminStatusUp(false)
            .setAlias("alias")
            .setMtu(56)
            .setDescription("desc")
            .setSpeed(23)
            .build();
    String ifaceAddress = "1.1.1.1/24";
    {
      Configuration c =
          Configuration.builder().setHostname("name").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setName("vrf").setOwner(c).build();
      convertPorts(
          c,
          ImmutableMap.of("iface", port),
          ImmutableMap.of("iface", new L3Interface(ConcreteInterfaceAddress.parse(ifaceAddress))),
          vrf);
      assertThat(
          Iterables.getOnlyElement(c.getAllInterfaces().values()),
          allOf(
              hasName("iface"),
              hasHumanName("alias"),
              hasAddress(ifaceAddress),
              hasMtu(56),
              hasDescription("desc"),
              hasSpeed(23 * SPEED_CONVERSION_FACTOR)));
    }
    {
      // interface does not exist
      Configuration c =
          Configuration.builder().setHostname("name").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setName("vrf").setOwner(c).build();
      convertPorts(c, ImmutableMap.of("iface", port), ImmutableMap.of(), vrf);
      assertThat(Iterables.getOnlyElement(c.getAllInterfaces().values()).getAddress(), nullValue());
    }
  }

  @Test
  public void testConvertVlans_createsVlanInterface() {
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Vrf vrf = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    convertVlans(
        c,
        ImmutableMap.of("Vlan1", Vlan.builder().setVlanId(1).build()),
        ImmutableMap.of(),
        ImmutableMap.of("Vlan1", new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24"))),
        vrf,
        new Warnings());
    assertThat(
        c.getAllInterfaces().get("Vlan1"),
        allOf(
            hasName("Vlan1"),
            hasVrfName(vrf.getName()),
            hasAddress("1.1.1.1/24"),
            hasInterfaceType(InterfaceType.VLAN)));
  }

  @Test
  public void testConvertVlans_configuredMemberInterfaces() {
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Vrf vrf = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    ImmutableList.of("Ethernet0", "Ethernet1")
        .forEach(
            ifaceName ->
                Interface.builder()
                    .setName("Ethernet0")
                    .setOwner(c)
                    .setVrf(vrf)
                    .setType(InterfaceType.PHYSICAL)
                    .build());
    Warnings w = new Warnings(true, true, true);
    convertVlans(
        c,
        ImmutableMap.of(
            "Vlan1",
            Vlan.builder()
                .setVlanId(1)
                .setMembers(ImmutableList.of("Ethernet0", "Ethernet1", "Ethernet2", "Ethernet3"))
                .build()),
        ImmutableMap.of(
            "Vlan1|Ethernet0",
            VlanMember.builder().setTaggingMode(TaggingMode.TAGGED).build(),
            "Vlan1|Ethernet1",
            VlanMember.builder().setTaggingMode(TaggingMode.UNTAGGED).build(),
            "Vlan1|Ethernet2",
            VlanMember.builder().setTaggingMode(TaggingMode.UNTAGGED).build()),
        ImmutableMap.of(),
        vrf,
        w);
    // Ethernet0 already exists in c -- its L2 settings should match 'tagged'
    assertThat(
        c.getAllInterfaces().get("Ethernet0"),
        allOf(
            hasSwitchPortMode(SwitchportMode.TRUNK),
            hasNativeVlan(1),
            hasAllowedVlans(IntegerSpace.of(1))));
    // Ethernet1 already exists in c -- its L2 settings should match 'untagged'
    assertThat(
        c.getAllInterfaces().get("Ethernet1"),
        allOf(hasSwitchPortMode(SwitchportMode.ACCESS), hasAccessVlan(1)));
    // Ethernet2 does not exist in c -- it should have been created with the right L2 settings
    assertThat(
        c.getAllInterfaces().get("Ethernet2"),
        allOf(
            hasName("Ethernet2"),
            hasVrfName(vrf.getName()),
            hasInterfaceType(InterfaceType.PHYSICAL),
            hasSwitchPortMode(SwitchportMode.ACCESS),
            hasAccessVlan(1)));
    // Ethernet3 has no member information -- it should be created and we should get a warning
    assertThat(
        c.getAllInterfaces().get("Ethernet3"),
        allOf(
            hasName("Ethernet3"),
            hasVrfName(vrf.getName()),
            hasSwitchPortMode(SwitchportMode.NONE)));
    assertThat(
        w.getRedFlagWarnings(), hasItem(hasText("Vlan member Vlan1|Ethernet3 is not configured")));
  }
}
