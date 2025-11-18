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
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDhcpRelayAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasHumanName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;
import static org.batfish.vendor.sonic.representation.SonicConversions.allowsSnmp;
import static org.batfish.vendor.sonic.representation.SonicConversions.attachAcl;
import static org.batfish.vendor.sonic.representation.SonicConversions.checkVlanId;
import static org.batfish.vendor.sonic.representation.SonicConversions.computeSnmpClientSpace;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertAcls;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertDhcpServers;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertLoopbacks;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertSnmpServer;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertVlans;
import static org.batfish.vendor.sonic.representation.SonicConversions.getAclRulesByTableName;
import static org.batfish.vendor.sonic.representation.SonicConversions.isSnmpTable;
import static org.batfish.vendor.sonic.representation.SonicConversions.setInterfaceAddresses;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TestInterface;
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
          ImmutableMap.of(
              "iface",
              new L3Interface(
                  ImmutableMap.of(
                      ConcreteInterfaceAddress.parse(ifaceAddress),
                      InterfaceKeyProperties.builder().build()))),
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
          ImmutableMap.of(
              "Vlan1",
              Vlan.builder().setDhcpServers(ImmutableList.of("10.1.1.1")).setVlanId(1).build()),
          ImmutableMap.of(),
          ImmutableMap.of(
              "Vlan1",
              new L3Interface(
                  ImmutableMap.of(
                      ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                      InterfaceKeyProperties.builder().build()))),
          vrf,
          new Warnings());
      assertThat(
          c.getAllInterfaces().get("Vlan1"),
          allOf(
              hasName("Vlan1"),
              hasVrfName(vrf.getName()),
              hasAddress("1.1.1.1/24"),
              hasInterfaceType(InterfaceType.VLAN),
              hasDhcpRelayAddresses(contains(Ip.parse("10.1.1.1")))));
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
          ImmutableMap.of(
              "Vlan1",
              new L3Interface(
                  ImmutableMap.of(
                      ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                      InterfaceKeyProperties.builder().build()))),
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
          ImmutableMap.of(
              "Vlan1",
              new L3Interface(
                  ImmutableMap.of(
                      ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                      InterfaceKeyProperties.builder().build()))),
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
                TestInterface.builder()
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

  @Test
  public void testConvertDhcpServers() {
    {
      Warnings w = new Warnings(true, true, true);
      assertEquals(
          ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.2")),
          convertDhcpServers(ImmutableList.of("1.1.1.1", "1.1.1.2"), w));
      assertTrue(w.getRedFlagWarnings().isEmpty());
    }
    {
      Warnings w = new Warnings(true, true, true);
      assertEquals(
          ImmutableList.of(Ip.parse("1.1.1.2")),
          convertDhcpServers(ImmutableList.of("dhcp", "1.1.1.2"), w));
      assertThat(
          w.getRedFlagWarnings(),
          contains(hasText("Cannot add a non-IP address value 'dhcp' as DHCP server")));
    }
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
    TestInterface.builder().setOwner(c).setName(ifaceName).setType(InterfaceType.PHYSICAL).build();

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

    convertAcls(
        c, aclTables, getAclRulesByTableName(aclTables, aclRules, new Warnings()), new Warnings());

    // ACL exists in the VI model and is attached properly
    IpAccessList ipAccessList = c.getIpAccessLists().get(aclName);
    assertNotNull(ipAccessList);
    assertEquals(aclName, c.getAllInterfaces().get(ifaceName).getIncomingFilter().getName());

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
  public void testGetAclRulesByTableName() {
    String aclName = "testAcl";
    Map<String, AclTable> aclTables =
        ImmutableMap.of(
            aclName,
            AclTable.builder()
                .setPorts(ImmutableList.of("Ethernet0"))
                .setStage(Stage.INGRESS)
                .setType(Type.L3)
                .build());
    AclRule goodRule =
        AclRule.builder().setPriority(100).setPacketAction(PacketAction.DROP).build();
    Map<String, AclRule> aclRules =
        ImmutableMap.of(
            "badkey",
            AclRule.builder().setPriority(100).setPacketAction(PacketAction.DROP).build(),
            ruleKey(aclName, "NoPriority"),
            AclRule.builder().setPacketAction(PacketAction.ACCEPT).build(),
            ruleKey(aclName, "NoPacketAction"),
            AclRule.builder().setPriority(100).build(),
            ruleKey(aclName, "NonIpEtherType"),
            AclRule.builder()
                .setPriority(100)
                .setPacketAction(PacketAction.DROP)
                .setEtherType(0x8100)
                .build(),
            ruleKey("other", "NoAcl"),
            AclRule.builder().setPriority(100).setPacketAction(PacketAction.DROP).build(),
            ruleKey(aclName, "GoodRule"),
            goodRule);

    Warnings warnings = new Warnings(true, true, true);

    Map<String, SortedSet<AclRuleWithName>> aclRulesByName =
        getAclRulesByTableName(aclTables, aclRules, warnings);

    assertThat(
        warnings.getRedFlagWarnings(),
        containsInAnyOrder(
            hasText("Ignored ACL_RULE badkey: Badly formatted name"),
            hasText("Ignored ACL_RULE testAcl|NoPriority: Missing PRIORITY"),
            hasText("Ignored ACL_RULE testAcl|NoPacketAction: Missing PACKET_ACTION"),
            hasText("Ignored ACL_RULE testAcl|NonIpEtherType: Non-IPv4 ETHER_TYPE"),
            hasText("Ignored ACL_RULE other|NoAcl: Missing ACL_TABLE 'other'")));

    // good rule is left
    assertThat(
        aclRulesByName.get(aclName),
        equalTo(ImmutableSortedSet.of(new AclRuleWithName("GoodRule", goodRule))));
  }

  @Test
  public void testAclWithRuleNameCompareTo() {
    AclRule.Builder builder = AclRule.builder().setPacketAction(PacketAction.ACCEPT);

    assertTrue(
        new AclRuleWithName("rule1", builder.setPriority(100).build())
                .compareTo(new AclRuleWithName("rule2", builder.setPriority(90).build()))
            < 1);
    assertTrue(
        new AclRuleWithName("rule1", builder.setPriority(100).build())
                .compareTo(new AclRuleWithName("rule2", builder.setPriority(100).build()))
            < 1);
    assertEquals(
        0,
        new AclRuleWithName("rule1", builder.setPriority(100).build())
            .compareTo(new AclRuleWithName("rule1", builder.setPriority(100).build())));
  }

  @Test
  public void testAttachAcl() {
    String ifaceName = "Ethernet0";
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    TestInterface.builder().setOwner(c).setName(ifaceName).setType(InterfaceType.PHYSICAL).build();

    String aclName = "testAcl";
    IpAccessList ipAccessList = IpAccessList.builder().setOwner(c).setName(aclName).build();

    {
      // non-L3 ACLs are not attached to interfaces
      AclTable aclTable =
          AclTable.builder()
              .setPorts(ImmutableList.of(ifaceName))
              .setStage(Stage.INGRESS)
              .setType(Type.MIRROR)
              .build();

      Warnings warnings = new Warnings(true, true, true);
      attachAcl(c, ipAccessList, aclTable, warnings);

      assertTrue(warnings.getRedFlagWarnings().isEmpty());
      assertNull(c.getAllInterfaces().get(ifaceName).getIncomingFilter());
    }
    {
      // missing stage
      AclTable aclTable =
          AclTable.builder().setPorts(ImmutableList.of(ifaceName)).setType(Type.L3).build();

      Warnings warnings = new Warnings(true, true, true);
      attachAcl(c, ipAccessList, aclTable, warnings);

      assertThat(warnings.getRedFlagWarnings(), contains(hasText("Unimplemented ACL stage: null")));
      assertNull(c.getAllInterfaces().get(ifaceName).getIncomingFilter());
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
      attachAcl(c, ipAccessList, aclTable, warnings);

      // warn about 'other', not about 'ctrlplane'
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText("Port 'other' referenced in ACL_TABLE 'testAcl' does not exist.")));
      assertEquals(aclName, c.getAllInterfaces().get(ifaceName).getOutgoingFilter().getName());
    }
  }

  @Test
  public void testConvertLoopbacks() {
    {
      // interface only in LOOPBACK_INTERFACE
      Configuration c =
          Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setOwner(c).setName("vrf").build();
      convertLoopbacks(
          c,
          ImmutableSet.of(),
          ImmutableMap.of(
              "Loopback0",
              new L3Interface(
                  ImmutableMap.of(
                      ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                      InterfaceKeyProperties.builder().build()))),
          vrf);
      assertThat(
          c.getAllInterfaces().get("Loopback0"),
          allOf(
              hasVrfName("vrf"),
              hasInterfaceType(InterfaceType.LOOPBACK),
              hasAddress("1.1.1.1/24")));
    }
    {
      // interface only in LOOPBACK
      Configuration c =
          Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setOwner(c).setName("vrf").build();
      convertLoopbacks(c, ImmutableSet.of("Loopback0"), ImmutableMap.of(), vrf);
      assertThat(
          c.getAllInterfaces().get("Loopback0"),
          allOf(
              hasVrfName("vrf"),
              hasInterfaceType(InterfaceType.LOOPBACK),
              hasAddress(nullValue())));
    }
    {
      // interface in both tables
      Configuration c =
          Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
      Vrf vrf = Vrf.builder().setOwner(c).setName("vrf").build();
      convertLoopbacks(
          c,
          ImmutableSet.of("Loopback0"),
          ImmutableMap.of(
              "Loopback0",
              new L3Interface(
                  ImmutableMap.of(
                      ConcreteInterfaceAddress.parse("1.1.1.1/24"),
                      InterfaceKeyProperties.builder().build()))),
          vrf);
      assertThat(
          c.getAllInterfaces().get("Loopback0"),
          allOf(
              hasVrfName("vrf"),
              hasInterfaceType(InterfaceType.LOOPBACK),
              hasAddress("1.1.1.1/24")));
    }
  }

  @Test
  public void testSnmpServer() {
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();

    AclTable.Builder aclTable =
        AclTable.builder()
            .setStage(Stage.INGRESS)
            .setType(Type.CTRLPLANE)
            .setServices(ImmutableList.of("SNMP"));
    Prefix clientPrefix = Prefix.parse("1.1.1.0/24");
    AclRule aclRule =
        AclRule.builder()
            .setSrcIp(clientPrefix)
            .setPriority(10)
            .setPacketAction(PacketAction.ACCEPT)
            .build();

    Map<String, AclTable> aclTables = ImmutableMap.of("table", aclTable.build());
    Map<String, SortedSet<AclRuleWithName>> aclRules =
        ImmutableMap.of("table", ImmutableSortedSet.of(new AclRuleWithName("rule1", aclRule)));

    convertSnmpServer(c, "community", aclTables, aclRules, new Warnings());

    SnmpCommunity snmpCommunity =
        Iterables.getOnlyElement(c.getDefaultVrf().getSnmpServer().getCommunities().values());
    assertThat(snmpCommunity.getAccessList(), equalTo("table"));
    assertThat(
        snmpCommunity.getClientIps(),
        equalTo(
            AclIpSpace.builder().thenAction(LineAction.PERMIT, clientPrefix.toIpSpace()).build()));
  }

  @Test
  public void testConvertSnmpServer_multipleSnmpTables() {
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();

    Warnings w = new Warnings(true, true, true);

    AclTable.Builder aclTable =
        AclTable.builder()
            .setStage(Stage.INGRESS)
            .setType(Type.CTRLPLANE)
            .setServices(ImmutableList.of("SNMP"));
    Map<String, AclTable> aclTables =
        ImmutableMap.of("table1", aclTable.build(), "table2", aclTable.build());

    convertSnmpServer(c, "community", aclTables, ImmutableMap.of(), w);

    SnmpCommunity snmpCommunity =
        Iterables.getOnlyElement(c.getDefaultVrf().getSnmpServer().getCommunities().values());
    assertThat(snmpCommunity.getAccessList(), nullValue());
    assertThat(
        w.getRedFlagWarnings(),
        contains(hasText("Found multiple SNMP ACL tables: [table1, table2]. Ignored all.")));
  }

  @Test
  public void testConvertSnmpServer_noSnmpTable() {
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();

    Warnings w = new Warnings(true, true, true);

    AclTable.Builder aclTable =
        AclTable.builder()
            .setStage(Stage.EGRESS) // will not be SNMP
            .setType(Type.CTRLPLANE)
            .setServices(ImmutableList.of("SNMP"));
    Map<String, AclTable> aclTables =
        ImmutableMap.of("table1", aclTable.build(), "table2", aclTable.build());

    convertSnmpServer(c, "community", aclTables, ImmutableMap.of(), w);

    SnmpCommunity snmpCommunity =
        Iterables.getOnlyElement(c.getDefaultVrf().getSnmpServer().getCommunities().values());
    assertThat(snmpCommunity.getAccessList(), nullValue());
  }

  @Test
  public void testConvertSnmpServer_noAclRules() {
    Configuration c =
        Configuration.builder().setHostname("host").setConfigurationFormat(SONIC).build();
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();

    Warnings w = new Warnings(true, true, true);

    AclTable.Builder aclTable =
        AclTable.builder()
            .setStage(Stage.INGRESS)
            .setType(Type.CTRLPLANE)
            .setServices(ImmutableList.of("SNMP"));
    Map<String, AclTable> aclTables = ImmutableMap.of("table", aclTable.build());

    convertSnmpServer(c, "community", aclTables, ImmutableMap.of(), w);

    SnmpCommunity snmpCommunity =
        Iterables.getOnlyElement(c.getDefaultVrf().getSnmpServer().getCommunities().values());
    assertThat(snmpCommunity.getAccessList(), nullValue());
    assertThat(
        w.getRedFlagWarnings(), contains(hasText("ACL rules not found for SNMP table 'table'.")));
  }

  @Test
  public void testComputeClientIpSpace() {
    Prefix clientPrefix = Prefix.parse("1.1.1.0/24");
    AclRule allowsSnmp =
        AclRule.builder()
            .setSrcIp(clientPrefix)
            .setPriority(10)
            .setPacketAction(PacketAction.ACCEPT)
            .build();
    AclRule notSnmp =
        AclRule.builder()
            .setSrcIp(Prefix.parse("2.2.2.0/24"))
            .setIpProtocol(6) // not UDP
            .setPriority(10)
            .setPacketAction(PacketAction.ACCEPT)
            .build();
    AclRule deniesClient =
        AclRule.builder()
            .setSrcIp(Prefix.parse("3.3.3.0/24"))
            .setPriority(10)
            .setPacketAction(PacketAction.DROP)
            .build();

    IpSpace clientSpace =
        computeSnmpClientSpace(
            ImmutableSortedSet.of(
                new AclRuleWithName("r1", allowsSnmp),
                new AclRuleWithName("r2", notSnmp),
                new AclRuleWithName("r3", deniesClient)));

    assertThat(
        clientSpace,
        equalTo(
            AclIpSpace.builder().thenAction(LineAction.PERMIT, clientPrefix.toIpSpace()).build()));
  }

  @Test
  public void testIsSnmpTable() {
    assertFalse(isSnmpTable(AclTable.builder().build()));
    assertFalse(isSnmpTable(AclTable.builder().setStage(Stage.EGRESS).build()));
    assertFalse(isSnmpTable(AclTable.builder().setType(Type.L3).build()));
    assertFalse(isSnmpTable(AclTable.builder().setServices(ImmutableList.of("SSH")).build()));

    AclTable.Builder aclTable =
        AclTable.builder()
            .setStage(Stage.INGRESS)
            .setType(Type.CTRLPLANE)
            .setServices(ImmutableList.of("SNMP"));

    assertTrue(isSnmpTable(aclTable.build()));
    assertTrue(isSnmpTable(aclTable.setServices(ImmutableList.of("snmp")).build()));
    assertTrue(isSnmpTable(aclTable.setServices(ImmutableList.of("SNMP", "SSH")).build()));
  }

  @Test
  public void testAllowsSnmp() {
    assertTrue(allowsSnmp(AclRule.builder().build()));
    assertTrue(allowsSnmp(AclRule.builder().setL4DstPort(161).build()));
    assertTrue(allowsSnmp(AclRule.builder().setIpProtocol(17).build()));
    assertTrue(allowsSnmp(AclRule.builder().setIpProtocol(17).setL4DstPort(161).build()));

    assertFalse(allowsSnmp(AclRule.builder().setIpProtocol(6).setL4DstPort(161).build()));
    assertFalse(allowsSnmp(AclRule.builder().setIpProtocol(17).setL4DstPort(61).build()));
  }

  @Test
  public void testSetInterfaceAddresses() {
    Interface.Builder ib = TestInterface.builder().setName("iface");
    ConcreteInterfaceAddress addr1 = ConcreteInterfaceAddress.parse("1.1.1.1/31");
    ConcreteInterfaceAddress addr2 = ConcreteInterfaceAddress.parse("2.1.1.1/31");

    // no addresses
    setInterfaceAddresses(ib, new L3Interface(ImmutableMap.of()));
    assertThat(ib.build().getAllAddresses(), equalTo(ImmutableSet.of()));

    // one non-secondary address
    setInterfaceAddresses(
        ib, new L3Interface(ImmutableMap.of(addr1, InterfaceKeyProperties.builder().build())));
    assertThat(ib.build().getAllAddresses(), equalTo(ImmutableSet.of(addr1)));
    assertThat(ib.build().getAddress(), equalTo(addr1));

    // two non-secondary addresses (pick lower)
    setInterfaceAddresses(
        ib,
        new L3Interface(
            ImmutableMap.of(
                addr1,
                InterfaceKeyProperties.builder().build(),
                addr2,
                InterfaceKeyProperties.builder().build())));
    assertThat(ib.build().getAllAddresses(), equalTo(ImmutableSet.of(addr1, addr2)));
    assertThat(ib.build().getAddress(), equalTo(addr1));

    // one secondary and one non-secondary
    setInterfaceAddresses(
        ib,
        new L3Interface(
            ImmutableMap.of(
                addr1,
                InterfaceKeyProperties.builder().setSecondary(true).build(),
                addr2,
                InterfaceKeyProperties.builder().build())));
    assertThat(ib.build().getAllAddresses(), equalTo(ImmutableSet.of(addr1, addr2)));
    assertThat(ib.build().getAddress(), equalTo(addr2));

    // only secondary addresses: primary is not set
    setInterfaceAddresses(
        ib,
        new L3Interface(
            ImmutableMap.of(addr1, InterfaceKeyProperties.builder().setSecondary(true).build())));
    assertThat(ib.build().getAllAddresses(), equalTo(ImmutableSet.of(addr1)));
    assertThat(ib.build().getAddress(), nullValue());
  }
}
