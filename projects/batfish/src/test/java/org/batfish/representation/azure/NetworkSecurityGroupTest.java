package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

public class NetworkSecurityGroupTest {

  private static final IpSpace vnetIpSpace =
      AclIpSpace.union(
          Prefix.parse("10.0.0.0/8").toIpSpace(),
          Prefix.parse("172.16.0.0/20").toIpSpace(),
          Prefix.parse("192.168.0.0/16").toIpSpace());
  private static final IpSpace internetIpSpace =
      AclIpSpace.difference(UniverseIpSpace.INSTANCE, vnetIpSpace);

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/azure/NetworkSecurityGroupTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    NetworkSecurityGroup nsg =
        BatfishObjectMapper.mapper().convertValue(node, NetworkSecurityGroup.class);
    assertNotNull(nsg);

    assertEquals("testNsg", nsg.getName());
    assertEquals("Microsoft.Network/networkSecurityGroups", nsg.getType());
    assertEquals(
        "resourceGroups/test/providers/Microsoft.Network/networkSecurityGroups/NSG-3", nsg.getId());

    NetworkSecurityGroup.Properties properties = nsg.getProperties();
    assertNotNull(properties);

    List<SecurityRule> securityRules = properties.getSecurityRules();
    assertNotNull(securityRules);

    {
      // best priority should be first (lowest)
      SecurityRule securityRule = securityRules.get(0);

      assertNotNull(securityRule);
      assertEquals("AllowCidrBlockSSHInbound", securityRule.getName());
      assertEquals(
          "resourceGroups/test/providers/Microsoft.Network/networkSecurityGroups/"
              + "NSG-3/securityRules/AllowCidrBlockSSHInbound",
          securityRule.getId());
      assertEquals("Microsoft.Network/networkSecurityGroups/securityRules", securityRule.getType());

      SecurityRule.Properties securityRuleProperties = securityRule.getProperties();
      assertNotNull(securityRuleProperties);

      assertEquals(IpProtocol.fromString("TCP"), securityRuleProperties.getProtocol());
      assertEquals("Inbound", securityRuleProperties.getDirection());
      assertEquals("Allow", securityRuleProperties.getAccess());
      assertEquals("10.0.0.0/24", securityRuleProperties.getSourceAddressPrefix());
      assertEquals("*", securityRuleProperties.getDestinationAddressPrefix()); // "*" destination
      assertEquals(100, securityRuleProperties.getPriority());

      // check prefixes ?
    }

    // check order by priority (lowest first)
    int last = 0;
    for (SecurityRule securityRule : securityRules) {
      assertTrue(last <= securityRule.getProperties().getPriority());
      last = securityRule.getProperties().getPriority();
    }
  }

  @Test
  public void testAclRandom() {
    {
      SecurityRule.Properties properties =
          new SecurityRule.Properties(
              "*",
              "22-80",
              null,
              null,
              "*",
              "192.168.1.0/24",
              null,
              null,
              "TCP",
              "Deny",
              100,
              "Outbound");

      SecurityRule securityRule = new SecurityRule("test", "testId", "testType", properties);

      HeaderSpace headerSpace =
          HeaderSpace.builder()
              .setIpProtocols(IpProtocol.TCP)
              .setSrcPorts(new SubRange(0, 65535))
              .setDstPorts(new SubRange(22, 80))
              .setSrcIps(Prefix.ZERO.toIpSpace())
              .setDstIps(Prefix.parse("192.168.1.0/24").toIpSpace())
              .build();

      ExprAclLine expectedAclLine =
          ExprAclLine.builder()
              .setName("test")
              .setAction(LineAction.DENY)
              .setMatchCondition(new MatchHeaderSpace(headerSpace))
              .build();

      assertEquals(expectedAclLine, securityRule.getAclLine());
    }
  }

  @Test
  public void testAclIcmp() {
    SecurityRule.Properties properties =
        new SecurityRule.Properties(
            null,
            null,
            null,
            null,
            "192.168.1.0/24",
            "192.168.2.0/24",
            null,
            null,
            "ICMP",
            "Allow",
            1,
            "inbound");

    SecurityRule securityRule = new SecurityRule("testName", "testId", "testType", properties);

    HeaderSpace headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.ICMP)
            .setSrcIps(Prefix.parse("192.168.1.0/24").toIpSpace())
            .setDstIps(Prefix.parse("192.168.2.0/24").toIpSpace())
            .build();

    ExprAclLine exprAclLine =
        ExprAclLine.builder()
            .setMatchCondition(new MatchHeaderSpace(headerSpace))
            .setName("testName")
            .setAction(LineAction.PERMIT)
            .build();

    assertEquals(exprAclLine, securityRule.getAclLine());
  }

  @Test
  public void testAclInternet() {
    SecurityRule.Properties properties =
        new SecurityRule.Properties(
            "1025",
            null,
            null,
            Set.of("10-80"),
            "Internet",
            null,
            null,
            null,
            "TCP",
            "Disallow",
            1,
            "outbound");

    SecurityRule securityRule = new SecurityRule("testName", "testId", "testType", properties);

    assertEquals(
        ExprAclLine.builder()
            .setAction(LineAction.DENY)
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(internetIpSpace)
                        .setDstIps(UniverseIpSpace.INSTANCE)
                        .setIpProtocols(IpProtocol.TCP)
                        .setSrcPorts(new SubRange(1025))
                        .setDstPorts(new SubRange(10, 80))
                        .build()))
            .setName("testName")
            .build(),
        securityRule.getAclLine());
  }

  @Test
  public void testAclVnetServiceTag() {
    SecurityRule.Properties properties =
        new SecurityRule.Properties(
            null,
            null,
            null,
            null,
            "VirtualNetwork",
            null,
            null,
            null,
            "UDP",
            "Disallow",
            1,
            "outbound");

    SecurityRule securityRule = new SecurityRule("testName", "testId", "testType", properties);

    IpSpace vnetSpace =
        AclIpSpace.union(
            Prefix.parse("10.0.0.0/8").toIpSpace(),
            Prefix.parse("172.16.0.0/20").toIpSpace(),
            Prefix.parse("192.168.0.0/16").toIpSpace());

    assertEquals(
        ExprAclLine.builder()
            .setAction(LineAction.DENY)
            .setMatchCondition(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(vnetSpace)
                        .setDstIps(UniverseIpSpace.INSTANCE)
                        .setIpProtocols(IpProtocol.UDP)
                        .setSrcPorts(new SubRange(0, 65535))
                        .setDstPorts(new SubRange(0, 65535))
                        .build()))
            .setName("testName")
            .build(),
        securityRule.getAclLine());
  }
}
