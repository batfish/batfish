package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.aclName;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.checkValidHeaderSpaceInputs;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.servicesToMatchExpr;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toAction;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toAddressMatchExpr;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpAccessLists;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toMatchExpr;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
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
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatSettings;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.PolicyTargets;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.ServiceIcmp;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.ServiceUdp;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

/** Test of {@link CheckPointGatewayConversions}. */
public final class CheckPointGatewayConversionsTest {
  public static final NatSettings NAT_SETTINGS_TEST_INSTANCE =
      new NatSettings(true, "gateway", "All", null, "hide");
  private static final Uid UID_ACCEPT = Uid.of("99997");
  private static final Uid UID_DROP = Uid.of("99998");
  private static final Uid UID_CPMI_ANY = Uid.of("99999");
  private static final Uid UID_NET0 = Uid.of("10");
  private static final Uid UID_NET1 = Uid.of("11");
  private static final Uid UID_NET2 = Uid.of("12");
  private static final Uid UID_SERVICE_TCP_22 = Uid.of("13");
  private static final Uid UID_SERVICE_UDP_222 = Uid.of("14");
  private static final CpmiAnyObject CPMI_ANY = new CpmiAnyObject(UID_CPMI_ANY);
  private static final Uid UID_TCP_RANGES = Uid.of("15");
  private static final Uid UID_UDP = Uid.of("16");
  private static final Uid UID_ICMP = Uid.of("17");
  private static final Uid UID_ICMP_NO_CODE = Uid.of("18");
  private static final ServiceTcp SERVICE_TCP_RANGES =
      new ServiceTcp("tcp_ranges", "1-100,105-106", UID_TCP_RANGES);
  private static final ServiceUdp SERVICE_UDP = new ServiceUdp("udp", "1234", UID_UDP);
  private static final ServiceIcmp SERVICE_ICMP = new ServiceIcmp("icmp", 8, 3, UID_ICMP);
  private static final ServiceIcmp SERVICE_ICMP_NO_CODE =
      new ServiceIcmp("icmpNoCode", 8, null, UID_ICMP_NO_CODE);
  private static final Network NETWORK_0 =
      new Network(
          "net0",
          NAT_SETTINGS_TEST_INSTANCE,
          Ip.parse("10.0.0.0"),
          Ip.parse("255.255.255.0"),
          UID_NET0);
  private static final Network NETWORK_1 =
      new Network(
          "net1",
          NAT_SETTINGS_TEST_INSTANCE,
          Ip.parse("10.0.1.0"),
          Ip.parse("255.255.255.0"),
          UID_NET1);
  private static final Network NETWORK_2 =
      new Network(
          "net2",
          NAT_SETTINGS_TEST_INSTANCE,
          Ip.parse("10.0.2.0"),
          Ip.parse("255.255.255.0"),
          UID_NET2);

  private static final ImmutableMap<Uid, NamedManagementObject> TEST_OBJS =
      ImmutableMap.<Uid, NamedManagementObject>builder()
          .put(UID_NET0, NETWORK_0)
          .put(UID_NET1, NETWORK_1)
          .put(UID_NET2, NETWORK_2)
          .put(UID_CPMI_ANY, CPMI_ANY)
          .put(UID_ACCEPT, new RulebaseAction("Accept", UID_ACCEPT, "Accept"))
          .put(UID_DROP, new RulebaseAction("Drop", UID_DROP, "Drop"))
          .put(UID_SERVICE_TCP_22, new ServiceTcp("service_tcp_22", "22", UID_SERVICE_TCP_22))
          .put(UID_SERVICE_UDP_222, new ServiceUdp("service_udp_222", "222", UID_SERVICE_UDP_222))
          .put(UID_TCP_RANGES, SERVICE_TCP_RANGES)
          .put(UID_UDP, SERVICE_UDP)
          .put(UID_ICMP, SERVICE_ICMP)
          .put(UID_ICMP_NO_CODE, SERVICE_ICMP_NO_CODE)
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

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), TEST_IP_SPACES);
  private final ServiceToMatchExpr _serviceToMatchExpr = new ServiceToMatchExpr(TEST_OBJS);
  private final AddressSpaceToMatchExpr _addressSpaceToMatchExpr =
      new AddressSpaceToMatchExpr(TEST_OBJS);

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
                // Drop everything - not enabled
                AccessRule.testBuilder(UID_CPMI_ANY)
                    .setUid(Uid.of("10"))
                    .setAction(UID_DROP)
                    .setEnabled(false)
                    .setName("childRule0_drop_all")
                    .build(),
                AccessRule.testBuilder(UID_CPMI_ANY)
                    .setUid(Uid.of("4"))
                    .setAction(UID_ACCEPT)
                    .setDestination(ImmutableList.of(Uid.of("11")))
                    .setName("childRule1")
                    .build()),
            Uid.of("uidSection"));
    ImmutableList<AccessRuleOrSection> rulebase =
        ImmutableList.of(
            // Drop everything - not enabled
            AccessRule.testBuilder(UID_CPMI_ANY)
                .setUid(Uid.of("8"))
                .setAction(UID_DROP)
                .setEnabled(false)
                .setName("rule0_drop_all")
                .build(),
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

    Map<String, IpAccessList> ipAccessLists =
        toIpAccessLists(
            accessLayer, TEST_OBJS, _serviceToMatchExpr, _addressSpaceToMatchExpr, new Warnings());
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
  public void testAccessRuleToMatchExpr() {
    Warnings w = new Warnings();
    AclLineMatchExpr matchNet0 =
        AclLineMatchExprs.match(
            HeaderSpace.builder().setDstIps(new IpSpaceReference("net0")).build());
    AclLineMatchExpr matchNotNet0 =
        AclLineMatchExprs.match(
            HeaderSpace.builder().setNotDstIps(new IpSpaceReference("net0")).build());
    AclLineMatchExpr matchNet1 =
        AclLineMatchExprs.match(
            HeaderSpace.builder().setSrcIps(new IpSpaceReference("net1")).build());
    AclLineMatchExpr matchNotNet1 =
        AclLineMatchExprs.match(
            HeaderSpace.builder().setNotSrcIps(new IpSpaceReference("net1")).build());
    AclLineMatchExpr matchSvc =
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(IntegerSpace.of(22).getSubRanges())
                .build());
    AclLineMatchExpr matchUdpSvc =
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(IntegerSpace.of(222).getSubRanges())
                .build());
    AclLineMatchExpr matchNotSvc =
        AclLineMatchExprs.not(
            AclLineMatchExprs.match(
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
            TEST_OBJS,
            _serviceToMatchExpr,
            _addressSpaceToMatchExpr,
            w);
    assertThat(
        _tb.toBDD(matches),
        equalTo(_tb.toBDD(matchNet0).and(_tb.toBDD(matchNet1).and(_tb.toBDD(matchSvc)))));

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
            TEST_OBJS,
            _serviceToMatchExpr,
            _addressSpaceToMatchExpr,
            w);
    assertThat(
        _tb.toBDD(negatedMatches),
        equalTo(_tb.toBDD(matchNotNet0).and(_tb.toBDD(matchNotNet1).and(_tb.toBDD(matchNotSvc)))));

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
            TEST_OBJS,
            _serviceToMatchExpr,
            _addressSpaceToMatchExpr,
            w);
    assertThat(_tb.toBDD(mutliSvc), equalTo(_tb.toBDD(matchSvc).or(_tb.toBDD(matchUdpSvc))));
  }

  @Test
  public void testToIpSpace() {
    Warnings w = new Warnings(false, true, false);
    toAddressMatchExpr(
        ImmutableList.of(Uid.of("1"), Uid.of("2"), Uid.of("3")),
        ImmutableMap.of(
            Uid.of("2"),
            new ServiceTcp("tcp", "22", Uid.of("2")),
            Uid.of("3"),
            new CpmiAnyObject(Uid.of("1"))),
        _addressSpaceToMatchExpr,
        true,
        w);

    assertThat(
        w,
        hasRedFlags(
            contains(
                hasText("Cannot convert non-existent object (Uid '1') to IpSpace, ignoring"),
                hasText(
                    "Cannot convert object 'tcp' (Uid '2') of type 'ServiceTcp' to IpSpace,"
                        + " ignoring"))));
  }

  @Test
  public void testToAction() {
    Warnings w = new Warnings(false, true, false);
    assertThat(
        toAction(new RulebaseAction("Accept", Uid.of("1"), "Accept"), Uid.of("1"), w),
        equalTo(LineAction.PERMIT));
    assertThat(
        toAction(new RulebaseAction("Drop", Uid.of("2"), "Drop"), Uid.of("2"), w),
        equalTo(LineAction.DENY));
    assertThat(
        toAction(new RulebaseAction("Unknown", Uid.of("3"), "Unknown"), Uid.of("3"), w),
        equalTo(LineAction.DENY));
    assertThat(toAction(null, Uid.of("4"), w), equalTo(LineAction.DENY));
    assertThat(
        toAction(new ServiceTcp("tcp", "22", Uid.of("5")), Uid.of("5"), w),
        equalTo(LineAction.DENY));

    assertThat(
        w,
        hasRedFlags(
            contains(
                hasText(
                    "Cannot convert action 'Unknown' (Uid '3') into an access-rule action,"
                        + " defaulting to deny action"),
                hasText(
                    "Cannot convert non-existent object (Uid '4') into an access-rule action,"
                        + " defaulting to deny action"),
                hasText(
                    "Cannot convert object 'tcp' (Uid '5') of type ServiceTcp into an access-rule"
                        + " action, defaulting to deny action"))));
  }

  @Test
  public void testNatOrigToMatchExpr() {
    Uid uid = Uid.of("1");
    Warnings warnings = new Warnings();
    {
      TypedManagementObject policyTargets = new PolicyTargets(uid);
      assertThat(
          toMatchExpr(
              policyTargets,
              policyTargets,
              policyTargets,
              _serviceToMatchExpr,
              _addressSpaceToMatchExpr,
              warnings),
          equalTo(Optional.empty()));
    }
    {
      assertThat(
          _tb.toBDD(
              toMatchExpr(
                      NETWORK_0,
                      NETWORK_1,
                      SERVICE_TCP_RANGES,
                      _serviceToMatchExpr,
                      _addressSpaceToMatchExpr,
                      warnings)
                  .get()),
          equalTo(
              _tb.toBDD(
                  AclLineMatchExprs.match(
                      HeaderSpace.builder()
                          .setSrcIps(new IpSpaceReference(NETWORK_0.getName()))
                          .setDstIps(new IpSpaceReference(NETWORK_1.getName()))
                          .setDstPorts(
                              ImmutableList.of(new SubRange(1, 100), new SubRange(105, 106)))
                          .setIpProtocols(IpProtocol.TCP)
                          .build()))));
    }
    {
      assertThat(
          _tb.toBDD(
              toMatchExpr(
                      CPMI_ANY,
                      CPMI_ANY,
                      CPMI_ANY,
                      _serviceToMatchExpr,
                      _addressSpaceToMatchExpr,
                      warnings)
                  .get()),
          equalTo(_tb.toBDD(AclLineMatchExprs.match(HeaderSpace.builder().build()))));
    }
    {
      assertThat(
          _tb.toBDD(
              toMatchExpr(
                      CPMI_ANY,
                      CPMI_ANY,
                      SERVICE_UDP,
                      _serviceToMatchExpr,
                      _addressSpaceToMatchExpr,
                      warnings)
                  .get()),
          equalTo(
              _tb.toBDD(
                  AclLineMatchExprs.match(
                      HeaderSpace.builder()
                          .setDstPorts(ImmutableList.of(new SubRange(1234)))
                          .setIpProtocols(IpProtocol.UDP)
                          .build()))));
    }
    {
      assertThat(
          _tb.toBDD(
              toMatchExpr(
                      CPMI_ANY,
                      CPMI_ANY,
                      SERVICE_ICMP,
                      _serviceToMatchExpr,
                      _addressSpaceToMatchExpr,
                      warnings)
                  .get()),
          equalTo(
              _tb.toBDD(
                  AclLineMatchExprs.match(
                      HeaderSpace.builder()
                          .setIcmpTypes(8)
                          .setIcmpCodes(3)
                          .setIpProtocols(IpProtocol.ICMP)
                          .build()))));
    }
    {
      assertThat(
          _tb.toBDD(
              toMatchExpr(
                      CPMI_ANY,
                      CPMI_ANY,
                      SERVICE_ICMP_NO_CODE,
                      _serviceToMatchExpr,
                      _addressSpaceToMatchExpr,
                      warnings)
                  .get()),
          equalTo(
              _tb.toBDD(
                  AclLineMatchExprs.match(
                      HeaderSpace.builder()
                          .setIcmpTypes(8)
                          .setIpProtocols(IpProtocol.ICMP)
                          .build()))));
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

  @Test
  public void testServicesToMatchExpr() {
    Warnings w = new Warnings(false, true, false);

    assertThat(
        _tb.toBDD(
            servicesToMatchExpr(
                ImmutableList.of(UID_SERVICE_TCP_22, UID_NET0), TEST_OBJS, _serviceToMatchExpr, w)),
        equalTo(
            _tb.toBDD(
                AclLineMatchExprs.match(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP)
                        .setDstPorts(new SubRange(22))
                        .build()))));
    assertThat(
        w,
        hasRedFlags(
            contains(
                hasText(
                    "Cannot convert net0 (type Network) to a service match expression,"
                        + " making unmatchable."))));
  }

  @Test
  public void testServicesToMatchExprOnlyUnknown() {
    // If the only service(s) are unknown/unhandled, then the rule shouldn't match
    assertThat(
        _tb.toBDD(
            servicesToMatchExpr(
                ImmutableList.of(UID_NET0), TEST_OBJS, _serviceToMatchExpr, new Warnings())),
        equalTo(_tb.toBDD(FalseExpr.INSTANCE)));
  }

  @Test
  public void testAclNameAccessLayer() {
    Uid uid = Uid.of("1234");
    String name = "Named AccessLayer";
    AccessLayer named = new AccessLayer(ImmutableMap.of(), ImmutableList.of(), uid, name);

    assertThat(aclName(named), containsString(uid.getValue()));
    assertThat(aclName(named), containsString(name));
  }

  @Test
  public void testAclNameAccessSection() {
    Uid uid = Uid.of("1234");
    AccessSection unnamed =
        new AccessSection(AccessSection.generateName(uid), ImmutableList.of(), uid);
    String name = "Named AccessSection";
    AccessSection named = new AccessSection(name, ImmutableList.of(), uid);

    assertThat(aclName(unnamed), containsString(uid.getValue()));
    assertThat(aclName(named), containsString(uid.getValue()));
    assertThat(aclName(named), containsString(name));
  }
}
