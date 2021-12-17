package org.batfish.vendor.sonic.representation;

import static junit.framework.TestCase.assertFalse;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.SONIC;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
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
import static org.batfish.vendor.sonic.representation.SonicConversions.attachAcl;
import static org.batfish.vendor.sonic.representation.SonicConversions.checkVlanId;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertAcls;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertVlans;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Map;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.sonic.representation.AclRule.PacketAction;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;
import org.batfish.vendor.sonic.representation.SonicConversions.AclRuleWithName;
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

  private static final BddTestbed _bddTestbed =
      new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
  private static final IpAccessListToBdd _aclToBdd = _bddTestbed.getAclToBdd();

  private static @Nonnull BDD toMatchBDD(AclLine aclLine) {
    return _aclToBdd.toPermitAndDenyBdds(aclLine).getMatchBdd();
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
    Map<String, AclRule> aclRules =
        ImmutableMap.of(
            ruleKey(aclName, "RULE_1"),
            AclRule.builder()
                .setPriority(100) // higher priority
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
                .build());

    convertAcls(c, aclTables, aclRules, new Warnings());

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

    assertThat(
        ipAccessList.getLines().stream()
            .map(SonicConversionsTest::toMatchBDD)
            .collect(ImmutableList.toImmutableList()),
        contains(
            toMatchBDD(
                ExprAclLine.rejecting(
                    and(
                        matchIpProtocol(IpProtocol.TCP),
                        matchSrc(srcPrefix.getStartIp()),
                        matchDst(dstPrefix.getStartIp()),
                        matchSrcPort(42424),
                        matchDstPort(443)))),
            toMatchBDD(ExprAclLine.accepting(and(matchSrc(srcPrefix))))));
  }

  @Test
  public void testConvertAcls_badRules() {
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
    Map<String, AclRule> aclRules =
        ImmutableMap.of(
            "badkey",
            AclRule.builder().setPriority(100).setPacketAction(PacketAction.DROP).build(),
            ruleKey(aclName, "NoPriority"),
            AclRule.builder().setPacketAction(PacketAction.ACCEPT).build(),
            ruleKey(aclName, "NoPacketAction"),
            AclRule.builder().setPriority(100).build(),
            ruleKey("other", "NoAcl"),
            AclRule.builder().setPriority(100).setPacketAction(PacketAction.DROP).build(),
            ruleKey(aclName, "GoodRule"),
            AclRule.builder().setPriority(100).setPacketAction(PacketAction.DROP).build());

    Warnings warnings = new Warnings(true, true, true);
    convertAcls(c, aclTables, aclRules, warnings);

    assertThat(
        warnings.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText("Ignored ACL_RULE badkey: Badly formatted name"),
            hasText("Ignored ACL_RULE testAcl|NoPriority: Missing PRIORITY"),
            hasText("Ignored ACL_RULE testAcl|NoPacketAction: Missing PACKET_ACTION"),
            hasText("Ignored ACL_RULE other|NoAcl: Missing ACL_TABLE 'other'")));

    // ACLs are still converted with whatever is left
    IpAccessList ipAccessList = c.getIpAccessLists().get(aclName);
    assertNotNull(ipAccessList);
    assertEquals(aclName, c.getAllInterfaces().get(ifaceName).getInboundFilter().getName());
    assertEquals(
        ImmutableList.of("GoodRule"),
        ipAccessList.getLines().stream()
            .map(AclLine::getName)
            .collect(ImmutableList.toImmutableList()));
  }

  @Test
  public void testAclWithRuleNameCompareTo() {
    AclRule.Builder builder = AclRule.builder().setPacketAction(PacketAction.ACCEPT);

    assertTrue(
        new AclRuleWithName("rule1", builder.setPriority(100).build())
                .compareTo(new AclRuleWithName("rule1", builder.setPriority(90).build()))
            < 1);
    assertTrue(
        new AclRuleWithName("rule2", builder.setPriority(100).build())
                .compareTo(new AclRuleWithName("rule1", builder.setPriority(100).build()))
            < 1);
    assertTrue(
        new AclRuleWithName("rule1", builder.setPriority(100).build())
                .compareTo(new AclRuleWithName("rule1", builder.setPriority(100).build()))
            == 0);
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
