package org.batfish.vendor.sonic.representation;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.datamodel.ConfigurationFormat.SONIC;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;
import static org.batfish.vendor.sonic.representation.SonicConversions.attachAcl;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertAcls;
import static org.batfish.vendor.sonic.representation.SonicConversions.convertPorts;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
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
