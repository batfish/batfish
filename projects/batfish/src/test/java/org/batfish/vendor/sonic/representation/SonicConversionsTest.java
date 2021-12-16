package org.batfish.vendor.sonic.representation;

import static junit.framework.TestCase.assertFalse;
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
import static org.batfish.vendor.sonic.representation.SonicConversions.checkVlanId;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.vendor.sonic.representation.SonicConversions.attachAcl;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertAcls;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertVlans;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import java.util.Map;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.vendor.sonic.representation.AclRule.PacketAction;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;
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

  /**
   * Checks if the Vlan interface is created in the right circumstances and the interactions between
   * data in VLAN and VLAN_INTERFACE.
   */
  @Test
  public void testConvertVlans_vlanInterface() {
    {
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
    {
      // no vlan interface
      Configuration c =
          Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
      convertVlans(
          c,
          ImmutableMap.of("Vlan1", Vlan.builder().setVlanId(1).build()),
          ImmutableMap.of(),
          ImmutableMap.of(),
          vrf,
          new Warnings());
      assertNull(c.getAllInterfaces().get("Vlan1"));
    }
    {
      // bad vlanid
      Configuration c =
          Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
      convertVlans(
          c,
          ImmutableMap.of("Vlan1", Vlan.builder().setVlanId(12).build()),
          ImmutableMap.of(),
          ImmutableMap.of("Vlan1", new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24"))),
          vrf,
          new Warnings());
      assertNull(c.getAllInterfaces().get("Vlan1"));
    }
    {
      // vlan exists only in VLAN_INTERFACE
      Configuration c =
          Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
      Warnings warnings = new Warnings(true, true, true);
      convertVlans(
          c,
          ImmutableMap.of(),
          ImmutableMap.of(),
          ImmutableMap.of("Vlan1", new L3Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24"))),
          vrf,
          warnings);
      assertNull(c.getAllInterfaces().get("Vlan1"));
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(
              hasText("Ignoring VLAN_INTERFACEs [Vlan1] because they don't have VLANs defined.")));
    }
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

  @Test
  public void testCheckVlanId() {
    assertFalse(checkVlanId("Vlan1", null, new Warnings()));
    assertFalse(checkVlanId("Vlan1", 0, new Warnings()));
    assertFalse(checkVlanId("Vlan1", 4095, new Warnings()));
    assertFalse(checkVlanId("Vlan0", 1, new Warnings()));
    assertFalse(checkVlanId("VlanXX", 1, new Warnings()));
    assertTrue(checkVlanId("Vlan1", 1, new Warnings()));
  }

  private String ruleKey(String aclName, String ruleName) {
    return String.format("%s|%s", aclName, ruleName);
  }

  @Test
  public void testConvertAcls() {
    String ifaceName = "Ethernet0";
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Interface.builder().setOwner(c).setName(ifaceName).setType(InterfaceType.PHYSICAL).build();

    String aclName = "testAcl";
    Map<String, AclTable> aclTables =
        ImmutableMap.of(
            aclName,
            AclTable.builder()
                .setPorts(ImmutableList.of(ifaceName))
                .setStage(Stage.INGRESS)
                .setType(Type.L3)
                .build());
    Prefix srcPrefix = Prefix.parse("1.1.1.1/24");
    Prefix dstPrefix = Prefix.parse("2.2.2.2/24");
    // RULE_1 (higher pri) drops a subset of traffic that RULE_2 (lower pri) allows
    // RULE_3 is highest priority but belongs to another ACL, so should be ignored
    Map<String, AclRule> aclRules =
        ImmutableMap.of(
            ruleKey(aclName, "RULE_1"),
            AclRule.builder()
                .setPriority(100)
                .setPacketAction(PacketAction.DROP)
                .setSrcIp(srcPrefix.getStartIp().toPrefix())
                .setDstIp(dstPrefix.getStartIp().toPrefix())
                .setL4SrcPort(42424)
                .setL4DstPort(443)
                .setIpProtocol(IpProtocol.TCP.number())
                .build(),
            ruleKey(aclName, "RULE_2"),
            AclRule.builder()
                .setPriority(90)
                .setPacketAction(PacketAction.ACCEPT)
                .setSrcIp(srcPrefix)
                .setDstIp(dstPrefix)
                .setL4SrcPort(42424)
                .setL4DstPort(443)
                .setIpProtocol(IpProtocol.TCP.number())
                .build(),
            ruleKey("other", "RULE_3"),
            AclRule.builder()
                .setPriority(1000)
                .setPacketAction(PacketAction.ACCEPT)
                .setSrcIp(srcPrefix)
                .setDstIp(dstPrefix)
                .setL4SrcPort(42424)
                .setL4DstPort(443)
                .setIpProtocol(IpProtocol.TCP.number())
                .build());

    Flow dropFlowRule1 =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(srcPrefix.getStartIp())
            .setDstIp(dstPrefix.getStartIp())
            .setSrcPort(42424)
            .setDstPort(443)
            .build();
    Flow acceptFlowRule2 =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(srcPrefix.getEndIp())
            .setDstIp(dstPrefix.getEndIp())
            .setSrcPort(42424)
            .setDstPort(443)
            .build();
    Flow dropFlowDefault =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(srcPrefix.getEndIp())
            .setDstIp(dstPrefix.getEndIp())
            .setSrcPort(443) // unmatched port
            .setDstPort(443)
            .build();

    Warnings warnings = new Warnings(true, true, true);
    convertAcls(c, aclTables, aclRules, warnings);

    assertTrue(warnings.getRedFlagWarnings().isEmpty());

    // ACL exists in the VI model and is attached properly
    IpAccessList ipAccessList = c.getIpAccessLists().get(aclName);
    assertNotNull(ipAccessList);
    assertEquals(aclName, c.getAllInterfaces().get(ifaceName).getInboundFilter().getName());

    // ACL has all and only the expected rules
    assertEquals(
        ImmutableList.of("RULE_1", "RULE_2"),
        ipAccessList.getLines().stream()
            .map(AclLine::getName)
            .collect(ImmutableList.toImmutableList()));

    // check behavior
    assertThat(ipAccessList, rejects(dropFlowRule1, null, c));
    assertThat(ipAccessList, accepts(acceptFlowRule2, null, c));
    assertThat(ipAccessList, rejects(dropFlowDefault, null, c));
  }

  @Test
  public void testConvertAcls_rulePriority() {
    String ifaceName = "Ethernet0";
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Interface.builder().setOwner(c).setName(ifaceName).setType(InterfaceType.PHYSICAL).build();

    String aclName = "testAcl";
    Map<String, AclTable> aclTables =
        ImmutableMap.of(
            aclName,
            AclTable.builder()
                .setPorts(ImmutableList.of(ifaceName))
                .setStage(Stage.INGRESS)
                .setType(Type.L3)
                .build());
    Prefix srcPrefix = Prefix.parse("1.1.1.1/24");
    Prefix dstPrefix = Prefix.parse("2.2.2.2/24");
    // RULE_1 and RULE_2 have the same priority
    // RULE_3 has no priority
    Map<String, AclRule> aclRules =
        ImmutableMap.of(
            ruleKey(aclName, "RULE_1"),
            AclRule.builder()
                .setPriority(100)
                .setPacketAction(PacketAction.DROP)
                .setSrcIp(srcPrefix.getStartIp().toPrefix())
                .setDstIp(dstPrefix.getStartIp().toPrefix())
                .setL4SrcPort(42424)
                .setL4DstPort(443)
                .setIpProtocol(IpProtocol.TCP.number())
                .build(),
            ruleKey(aclName, "RULE_2"),
            AclRule.builder()
                .setPriority(100)
                .setPacketAction(PacketAction.ACCEPT)
                .setSrcIp(srcPrefix)
                .setDstIp(dstPrefix)
                .setL4SrcPort(42424)
                .setL4DstPort(443)
                .setIpProtocol(IpProtocol.TCP.number())
                .build(),
            ruleKey(aclName, "RULE_3"),
            AclRule.builder()
                .setPacketAction(PacketAction.ACCEPT)
                .setSrcIp(srcPrefix)
                .setDstIp(dstPrefix)
                .setL4SrcPort(42424)
                .setL4DstPort(443)
                .setIpProtocol(IpProtocol.TCP.number())
                .build());

    Warnings warnings = new Warnings(true, true, true);
    convertAcls(c, aclTables, aclRules, warnings);

    assertThat(
        warnings.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText("Ignored ACL_RULE 'testAcl|RULE_3' because PRIORITY was not defined"),
            hasText(
                "Ignored ACL_RULE 'testAcl|RULE_2' because its PRIORITY is duplicate of"
                    + " 'testAcl|RULE_1'")));

    // ACL exists in the VI model and is attached properly
    IpAccessList ipAccessList = c.getIpAccessLists().get(aclName);
    assertNotNull(ipAccessList);
    assertEquals(aclName, c.getAllInterfaces().get(ifaceName).getInboundFilter().getName());

    // Only RULE_1 is included
    assertEquals(
        ImmutableList.of("RULE_1"),
        ipAccessList.getLines().stream()
            .map(AclLine::getName)
            .collect(ImmutableList.toImmutableList()));
  }

  @Test
  public void testAttachAcl() {
    String ifaceName = "Ethernet0";
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Interface.builder().setOwner(c).setName(ifaceName).setType(InterfaceType.PHYSICAL).build();

    String aclName = "testAcl";
    IpAccessList ipAccessList = IpAccessList.builder().setName(aclName).build();

    {
      // non-L3 ACLs are not attached to interfaces
      AclTable aclTable =
          AclTable.builder()
              .setPorts(ImmutableList.of(ifaceName))
              .setStage(Stage.INGRESS)
              .setType(Type.MIRROR)
              .build();

      Warnings warnings = new Warnings(true, true, true);
      attachAcl(c, aclName, ipAccessList, aclTable, warnings);

      assertTrue(warnings.getRedFlagWarnings().isEmpty());
      assertNull(c.getAllInterfaces().get(ifaceName).getInboundFilter());
    }
    {
      // missing stage
      AclTable aclTable =
          AclTable.builder().setPorts(ImmutableList.of(ifaceName)).setType(Type.L3).build();

      Warnings warnings = new Warnings(true, true, true);
      attachAcl(c, aclName, ipAccessList, aclTable, warnings);

      assertThat(warnings.getRedFlagWarnings(), contains(hasText("Unimplemented ACL stage: null")));
      assertNull(c.getAllInterfaces().get(ifaceName).getInboundFilter());
    }
    {
      // one of the ports is missing
      AclTable aclTable =
          AclTable.builder()
              .setPorts(ImmutableList.of(ifaceName, "other", "ctrlplane"))
              .setStage(Stage.EGRESS)
              .setType(Type.L3)
              .build();

      Warnings warnings = new Warnings(true, true, true);
      attachAcl(c, aclName, ipAccessList, aclTable, warnings);

      // warn about 'other', not about 'ctrlplane'
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText("Port 'other' referenced in ACL_TABLE 'testAcl' does not exist.")));
      assertEquals(aclName, c.getAllInterfaces().get(ifaceName).getOutgoingFilter().getName());
    }
  }
}
