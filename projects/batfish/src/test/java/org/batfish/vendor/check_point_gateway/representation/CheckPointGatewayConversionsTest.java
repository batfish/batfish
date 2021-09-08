package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.aclName;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.checkValidHeaderSpaceInputs;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toAction;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toHeaderSpace;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpAccessLists;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toMatchExpr;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.NatSettings;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.PolicyTargets;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceUdp;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

/** Test of {@link CheckPointGatewayConversions}. */
public final class CheckPointGatewayConversionsTest {

  public static final NatSettings NAT_SETTINGS_TEST_INSTANCE =
      new NatSettings(true, "gateway", "All", "hide");
  private static final Uid UID_ACCEPT = Uid.of("99997");
  private static final Uid UID_DROP = Uid.of("99998");
  private static final Uid UID_CPMI_ANY = Uid.of("99999");
  private static final Uid UID_NET0 = Uid.of("10");
  private static final Uid UID_NET1 = Uid.of("11");
  private static final Uid UID_NET2 = Uid.of("12");
  private static final Uid UID_SERVICE_TCP_22 = Uid.of("13");
  private static final Uid UID_SERVICE_UDP_222 = Uid.of("14");
  private static final CpmiAnyObject CPMI_ANY = new CpmiAnyObject(UID_CPMI_ANY);
  private static final ImmutableMap<Uid, TypedManagementObject> TEST_OBJS =
      ImmutableMap.<Uid, TypedManagementObject>builder()
          .put(
              UID_NET0,
              new Network(
                  "net0",
                  NAT_SETTINGS_TEST_INSTANCE,
                  Ip.parse("10.0.0.0"),
                  Ip.parse("255.255.255.0"),
                  UID_NET0))
          .put(
              UID_NET1,
              new Network(
                  "net1",
                  NAT_SETTINGS_TEST_INSTANCE,
                  Ip.parse("10.0.1.0"),
                  Ip.parse("255.255.255.0"),
                  UID_NET1))
          .put(
              UID_NET2,
              new Network(
                  "net2",
                  NAT_SETTINGS_TEST_INSTANCE,
                  Ip.parse("10.0.2.0"),
                  Ip.parse("255.255.255.0"),
                  UID_NET2))
          .put(UID_CPMI_ANY, CPMI_ANY)
          .put(UID_ACCEPT, new RulebaseAction("Accept", UID_ACCEPT, "Accept"))
          .put(UID_DROP, new RulebaseAction("Drop", UID_DROP, "Drop"))
          .put(UID_SERVICE_TCP_22, new ServiceTcp("service_tcp_22", "22", UID_SERVICE_TCP_22))
          .put(UID_SERVICE_UDP_222, new ServiceUdp("service_udp_222", "222", UID_SERVICE_UDP_222))
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
    // Accept anywhere -> net1
    AccessSection accessSection =
        new AccessSection(
            "accessSectionName",
            ImmutableList.of(
                AccessRule.testBuilder(UID_CPMI_ANY)
                    .setUid(Uid.of("4"))
                    .setAction(UID_ACCEPT)
                    .setDestination(ImmutableList.of(Uid.of("11")))
                    .setName("childRule1")
                    .build()),
            Uid.of("uidSection"));
    ImmutableList<AccessRuleOrSection> rulebase =
        ImmutableList.of(
            // Drop net1 -> anywhere
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setUid(Uid.of("2"))
                .setAction(UID_DROP)
                .setSource(ImmutableList.of(Uid.of("11")))
                .setName("rule1")
                .build(),
            accessSection,
            // Drop all traffic
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setUid(Uid.of("6"))
                .setAction(UID_DROP)
                .setName("rule2")
                .build());

    Flow net0ToNet1 = createFlow(NET0_ADDR, NET1_ADDR);
    Flow net1ToNet1 = createFlow(NET1_ADDR, NET1_ADDR);
    Flow net0ToNet2 = createFlow(NET0_ADDR, NET2_ADDR);

    AccessLayer accessLayer =
        new AccessLayer(TEST_OBJS, rulebase, Uid.of("uidLayer"), "accessLayerName");

    Map<String, IpAccessList> ipAccessLists = toIpAccessLists(accessLayer);
    assertThat(
        ipAccessLists.keySet(), containsInAnyOrder(aclName(accessLayer), aclName(accessSection)));

    IpAccessList aclLayer = ipAccessLists.get(aclName(accessLayer));
    assertThat(aclLayer, accepts(net0ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclLayer, rejects(net1ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclLayer, rejects(net0ToNet2, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclLayer.getSourceName(), equalTo(accessLayer.getName()));

    IpAccessList aclSection = ipAccessLists.get(aclName(accessSection));
    assertThat(aclSection, accepts(net0ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclSection, accepts(net1ToNet1, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclSection, rejects(net0ToNet2, "eth0", ipAccessLists, TEST_IP_SPACES));
    assertThat(aclSection.getSourceName(), equalTo(accessSection.getName()));
  }

  @Test
  public void testToAclLineMatchExpr() {
    BddTestbed tb = new BddTestbed(ImmutableMap.of(), TEST_IP_SPACES);
    AclLineMatchExpr matchNet0 =
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(new IpSpaceReference("net0")).build());
    AclLineMatchExpr matchNotNet0 =
        new MatchHeaderSpace(
            HeaderSpace.builder().setNotDstIps(new IpSpaceReference("net0")).build());
    AclLineMatchExpr matchNet1 =
        new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(new IpSpaceReference("net1")).build());
    AclLineMatchExpr matchNotNet1 =
        new MatchHeaderSpace(
            HeaderSpace.builder().setNotSrcIps(new IpSpaceReference("net1")).build());
    AclLineMatchExpr matchSvc =
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(IntegerSpace.of(22).getSubRanges())
                .build());
    AclLineMatchExpr matchUdpSvc =
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(IntegerSpace.of(222).getSubRanges())
                .build());
    AclLineMatchExpr matchNotSvc =
        new NotMatchExpr(
            new MatchHeaderSpace(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.TCP)
                    .setDstPorts(IntegerSpace.of(22).getSubRanges())
                    .build()));

    // Non-negated matches
    AclLineMatchExpr matches =
        toMatchExpr(
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setAction(UID_ACCEPT)
                .setDestination(ImmutableList.of(UID_NET0))
                .setSource(ImmutableList.of(UID_NET1))
                .setService(ImmutableList.of(UID_SERVICE_TCP_22))
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            TEST_OBJS);
    assertThat(
        tb.toBDD(matches),
        equalTo(tb.toBDD(matchNet0).and(tb.toBDD(matchNet1).and(tb.toBDD(matchSvc)))));

    // Negated matches
    AclLineMatchExpr negatedMatches =
        toMatchExpr(
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setAction(UID_ACCEPT)
                .setDestinationNegate(true)
                .setDestination(ImmutableList.of(UID_NET0))
                .setSourceNegate(true)
                .setSource(ImmutableList.of(UID_NET1))
                .setServiceNegate(true)
                .setService(ImmutableList.of(UID_SERVICE_TCP_22))
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            TEST_OBJS);
    assertThat(
        tb.toBDD(negatedMatches),
        equalTo(tb.toBDD(matchNotNet0).and(tb.toBDD(matchNotNet1).and(tb.toBDD(matchNotSvc)))));

    // Multiple services
    AclLineMatchExpr mutliSvc =
        toMatchExpr(
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setAction(UID_ACCEPT)
                .setDestination(ImmutableList.of(UID_CPMI_ANY))
                .setSource(ImmutableList.of(UID_CPMI_ANY))
                .setService(ImmutableList.of(UID_SERVICE_TCP_22, UID_SERVICE_UDP_222))
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            TEST_OBJS);
    assertThat(tb.toBDD(mutliSvc), equalTo(tb.toBDD(matchSvc).or(tb.toBDD(matchUdpSvc))));
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

  @Test
  public void testToHeaderSpace() {
    Uid uid = Uid.of("1");
    Warnings warnings = new Warnings();
    {
      TypedManagementObject policyTargets = new PolicyTargets(uid);
      assertThat(
          toHeaderSpace(policyTargets, policyTargets, policyTargets, warnings),
          equalTo(Optional.empty()));
    }
    {
      assertThat(
          toHeaderSpace(
              new Host(Ip.parse("1.1.1.1"), NAT_SETTINGS_TEST_INSTANCE, "source", uid),
              new Host(Ip.parse("2.2.2.2"), NAT_SETTINGS_TEST_INSTANCE, "dest", uid),
              new ServiceTcp("foo", "1-100,105-106", uid),
              warnings),
          equalTo(
              Optional.of(
                  HeaderSpace.builder()
                      .setSrcIps(new IpSpaceReference("source"))
                      .setDstIps(new IpSpaceReference("dest"))
                      .setDstPorts(ImmutableList.of(new SubRange(1, 100), new SubRange(105, 106)))
                      .setIpProtocols(IpProtocol.TCP)
                      .build())));
    }
    {
      assertThat(
          toHeaderSpace(CPMI_ANY, CPMI_ANY, CPMI_ANY, warnings),
          equalTo(Optional.of(HeaderSpace.builder().build())));
    }
    {
      assertThat(
          toHeaderSpace(CPMI_ANY, CPMI_ANY, new ServiceUdp("foo", "1234", uid), warnings),
          equalTo(
              Optional.of(
                  HeaderSpace.builder()
                      .setDstPorts(ImmutableList.of(new SubRange(1234)))
                      .setIpProtocols(IpProtocol.UDP)
                      .build())));
    }
  }

  @Test
  public void testCheckValidHeaderSpaceInputs() {
    Uid uid = Uid.of("1");
    TypedManagementObject addressSpace = new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", uid);
    TypedManagementObject service = new ServiceTcp("foo", "1", uid);
    Warnings warnings = new Warnings();

    assertFalse(checkValidHeaderSpaceInputs(service, addressSpace, service, warnings));
    assertFalse(checkValidHeaderSpaceInputs(addressSpace, service, service, warnings));
    assertFalse(checkValidHeaderSpaceInputs(addressSpace, addressSpace, addressSpace, warnings));
    assertTrue(checkValidHeaderSpaceInputs(addressSpace, addressSpace, service, warnings));
  }
}
