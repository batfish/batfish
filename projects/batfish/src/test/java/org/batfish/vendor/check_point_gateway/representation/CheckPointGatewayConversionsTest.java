package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.matchers.AndMatchExprMatchers.hasConjuncts;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.isAndMatchExprThat;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toAction;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpAccessLists;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toMatchExpr;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

public class CheckPointGatewayConversionsTest {

  private static final Uid UID_ACCEPT = Uid.of("99997");
  private static final Uid UID_DROP = Uid.of("99998");
  private static final Uid UID_CPMI_ANY = Uid.of("99999");
  private static final Uid UID_NET0 = Uid.of("10");
  private static final Uid UID_NET1 = Uid.of("11");
  private static final Uid UID_NET2 = Uid.of("12");
  private static final CpmiAnyObject CPMI_ANY = new CpmiAnyObject(UID_CPMI_ANY);
  private static final ImmutableMap<Uid, TypedManagementObject> TEST_OBJS =
      ImmutableMap.<Uid, TypedManagementObject>builder()
          .put(
              UID_NET0,
              new Network("net0", Ip.parse("10.0.0.0"), Ip.parse("255.255.255.0"), UID_NET0))
          .put(
              UID_NET1,
              new Network("net1", Ip.parse("10.0.1.0"), Ip.parse("255.255.255.0"), UID_NET1))
          .put(
              UID_NET2,
              new Network("net2", Ip.parse("10.0.2.0"), Ip.parse("255.255.255.0"), UID_NET2))
          .put(UID_CPMI_ANY, CPMI_ANY)
          .put(UID_ACCEPT, new RulebaseAction("Accept", UID_ACCEPT, "Accept"))
          .put(UID_DROP, new RulebaseAction("Drop", UID_DROP, "Drop"))
          .build();
  private static final ImmutableMap<String, IpSpace> TEST_IP_SPACES =
      ImmutableMap.of(
          "net0",
          Prefix.parse("10.0.0.0/24").toIpSpace(),
          "net1",
          Prefix.parse("10.0.1.0/24").toIpSpace(),
          "net2",
          Prefix.parse("10.0.2.0/24").toIpSpace(),
          "Any",
          UniverseIpSpace.INSTANCE);
  private static final String NET0_ADDR = "10.0.0.100";
  private static final String NET1_ADDR = "10.0.1.100";
  private static final String NET2_ADDR = "10.0.2.100";

  private static Flow createFlow(String sourceAddress, String destinationAddress) {
    return createFlow(sourceAddress, destinationAddress, IpProtocol.TCP, 1, 1);
  }

  private static Flow createFlow(
      String sourceAddress,
      String destinationAddress,
      IpProtocol protocol,
      int sourcePort,
      int destinationPort) {
    return Flow.builder()
        .setIngressNode("node")
        .setSrcIp(Ip.parse(sourceAddress))
        .setDstIp(Ip.parse(destinationAddress))
        .setIpProtocol(protocol)
        .setDstPort(destinationPort)
        .setSrcPort(sourcePort)
        .build();
  }

  @Test
  public void testToIpAccessLists() {
    String accessLayerName = "accessLayerName";
    String accessSectionName = "accessSectionName";

    ImmutableList<AccessRuleOrSection> rulebase =
        ImmutableList.of(
            // Drop net1 -> anywhere
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setUid(Uid.of("2"))
                .setAction(UID_DROP)
                .setSource(ImmutableList.of(Uid.of("11")))
                .setName("rule1")
                .build(),
            // Accept anywhere -> net1
            new AccessSection(
                accessSectionName,
                ImmutableList.of(
                    AccessRule.testBuilder(UID_CPMI_ANY)
                        .setUid(Uid.of("4"))
                        .setAction(UID_ACCEPT)
                        .setDestination(ImmutableList.of(Uid.of("11")))
                        .setName("childRule1")
                        .build()),
                Uid.of("3")),
            // Drop all traffic
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setUid(Uid.of("6"))
                .setAction(UID_DROP)
                .setName("rule2")
                .build());

    Flow net0ToNet1 = createFlow(NET0_ADDR, NET1_ADDR);
    Flow net1ToNet1 = createFlow(NET1_ADDR, NET1_ADDR);
    Flow net0ToNet2 = createFlow(NET0_ADDR, NET2_ADDR);

    Map<String, IpAccessList> ipAccessLists =
        toIpAccessLists(new AccessLayer(TEST_OBJS, rulebase, Uid.of("1"), accessLayerName));
    assertThat(ipAccessLists.keySet(), containsInAnyOrder(accessLayerName, accessSectionName));

    IpAccessList aclLayer = ipAccessLists.get(accessLayerName);
    assertThat(aclLayer, accepts(net0ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclLayer, rejects(net1ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclLayer, rejects(net0ToNet2, "eth0", ipAccessLists, TEST_IP_SPACES));

    IpAccessList aclSection = ipAccessLists.get(accessSectionName);
    assertThat(aclSection, accepts(net0ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclSection, accepts(net1ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclSection, rejects(net0ToNet2, "eth0", ipAccessLists, TEST_IP_SPACES));
  }

  @Test
  public void testToAclLineMatchExpr() {
    // Non-negated matches
    assertThat(
        toMatchExpr(
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setAction(UID_ACCEPT)
                .setDestination(ImmutableList.of(UID_NET0))
                .setSource(ImmutableList.of(UID_NET1))
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            TEST_OBJS),
        isAndMatchExprThat(
            hasConjuncts(
                containsInAnyOrder(
                    new MatchHeaderSpace(
                        HeaderSpace.builder().setDstIps(new IpSpaceReference("net0")).build()),
                    new MatchHeaderSpace(
                        HeaderSpace.builder().setSrcIps(new IpSpaceReference("net1")).build())))));

    // Negated matches
    assertThat(
        toMatchExpr(
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setAction(UID_ACCEPT)
                .setDestinationNegate(true)
                .setDestination(ImmutableList.of(UID_NET0))
                .setSourceNegate(true)
                .setSource(ImmutableList.of(UID_NET1))
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            TEST_OBJS),
        isAndMatchExprThat(
            hasConjuncts(
                containsInAnyOrder(
                    new MatchHeaderSpace(
                        HeaderSpace.builder().setNotDstIps(new IpSpaceReference("net0")).build()),
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setNotSrcIps(new IpSpaceReference("net1"))
                            .build())))));
  }

  @Test
  public void testToAction() {
    assertThat(
        toAction(new RulebaseAction("Accept", Uid.of("1"), "Accept")), equalTo(LineAction.PERMIT));
    assertThat(toAction(new RulebaseAction("Drop", Uid.of("1"), "Drop")), equalTo(LineAction.DENY));
    assertThat(
        toAction(new RulebaseAction("Unknown", Uid.of("1"), "Unknown")), equalTo(LineAction.DENY));
    assertThat(toAction(null), equalTo(LineAction.DENY));
  }
}
