package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.matchers.AndMatchExprMatchers.hasConjuncts;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.isAndMatchExprThat;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toAction;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpAccessLists;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpSpace;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toMatchExpr;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Test;

public class CheckPointGatewayConversionsTest {

  @Test
  public void testToIpSpace_addressRange() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    AddressRange range = new AddressRange(ip1, ip2, null, null, "name", Uid.of("uid"));
    assertThat(toIpSpace(range), equalTo(IpRange.range(ip1, ip2)));
  }

  @Test
  public void testToIpSpace_addressRangeIpv6() {
    Ip6 ip1 = Ip6.parse("1::1");
    Ip6 ip2 = Ip6.parse("1::2");
    AddressRange range = new AddressRange(null, null, ip1, ip2, "name", Uid.of("uid"));
    assertNull(toIpSpace(range));
  }

  @Test
  public void testToIpSpace_network() {
    Ip ip = Ip.parse("1.1.1.0");
    Ip mask = Ip.parse("255.255.255.0");
    Network network = new Network("name", ip, mask, Uid.of("uid"));
    Ip flippedMask = Ip.parse("0.0.0.255");
    assertThat(
        toIpSpace(network), equalTo(IpWildcard.ipWithWildcardMask(ip, flippedMask).toIpSpace()));
  }

  @Test
  public void testToIpAccessLists() {
    Uid acceptUid = Uid.of("99997");
    Uid dropUid = Uid.of("99998");
    Uid cpmiAnyUid = Uid.of("99999");
    CpmiAnyObject cpmiAny = new CpmiAnyObject(cpmiAnyUid);
    ImmutableMap.Builder<Uid, TypedManagementObject> objsBuilder = ImmutableMap.builder();
    ImmutableMap<Uid, TypedManagementObject> objs =
        objsBuilder
            .put(
                Uid.of("10"),
                new Network("net0", Ip.parse("10.0.0.0"), Ip.parse("255.255.255.0"), Uid.of("10")))
            .put(
                Uid.of("11"),
                new Network("net1", Ip.parse("10.0.1.0"), Ip.parse("255.255.255.0"), Uid.of("11")))
            .put(
                Uid.of("12"),
                new Network("net2", Ip.parse("10.0.2.0"), Ip.parse("255.255.255.0"), Uid.of("12")))
            .put(
                Uid.of("13"),
                new Network("net3", Ip.parse("10.0.3.0"), Ip.parse("255.255.255.0"), Uid.of("13")))
            .put(cpmiAnyUid, cpmiAny)
            .put(acceptUid, new RulebaseAction("Accept", acceptUid, "Accept"))
            .put(dropUid, new RulebaseAction("Drop", dropUid, "Drop"))
            .build();

    Map<String, IpAccessList> ipAccessLists =
        toIpAccessLists(
            new AccessLayer(
                objs,
                ImmutableList.of(AccessRule.testBuilder(cpmiAnyUid).build()),
                Uid.of("1"),
                "accessLayerName"));
    // TODO test a section and rule
  }

  @Test
  public void testToAclLineMatchExpr() {
    Uid acceptUid = Uid.of("99997");
    Uid dropUid = Uid.of("99998");
    Uid cpmiAnyUid = Uid.of("99999");
    CpmiAnyObject cpmiAny = new CpmiAnyObject(cpmiAnyUid);
    ImmutableMap.Builder<Uid, TypedManagementObject> objsBuilder = ImmutableMap.builder();
    ImmutableMap<Uid, TypedManagementObject> objs =
        objsBuilder
            .put(
                Uid.of("10"),
                new Network("net0", Ip.parse("10.0.0.0"), Ip.parse("255.255.255.0"), Uid.of("10")))
            .put(
                Uid.of("11"),
                new Network("net1", Ip.parse("10.0.1.0"), Ip.parse("255.255.255.0"), Uid.of("11")))
            .put(cpmiAnyUid, cpmiAny)
            .put(acceptUid, new RulebaseAction("Accept", acceptUid, "Accept"))
            .put(dropUid, new RulebaseAction("Drop", dropUid, "Drop"))
            .build();

    // Non-negated matches
    assertThat(
        toMatchExpr(
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(acceptUid)
                .setDestination(ImmutableList.of(Uid.of("10"))) // net0
                .setSource(ImmutableList.of(Uid.of("11"))) // net1
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            objs),
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
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(acceptUid)
                .setDestinationNegate(true)
                .setDestination(ImmutableList.of(Uid.of("10"))) // net0
                .setSourceNegate(true)
                .setSource(ImmutableList.of(Uid.of("11"))) // net1
                .setRuleNumber(2)
                .setName("ruleName")
                .setUid(Uid.of("2"))
                .build(),
            objs),
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
  }
}
