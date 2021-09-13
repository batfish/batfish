package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.addressCpmiAnyTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.addressRangeTraceElement;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.trace.TraceTree;
import org.junit.Test;

/** Test of {@link AddressSpaceToMatchExpr}. */
public final class AddressSpaceToMatchExprTest {
  private static final NatSettings TEST_NAT_SETTINGS =
      new NatSettings(true, "gateway", "All", "hide");
  private static final AddressSpaceToIpSpace ADDR_SPACE_TO_IP_SPACE =
      new AddressSpaceToIpSpace(ImmutableMap.of());

  private static final Uid ADDR_RANGE_UID = Uid.of("10");
  private static final String ADDR_RANGE_NAME = "addrRangeName";
  private static final AddressRange ADDR_RANGE =
      new AddressRange(
          Ip.parse("10.0.0.0"),
          Ip.parse("10.0.0.255"),
          null,
          null,
          TEST_NAT_SETTINGS,
          ADDR_RANGE_NAME,
          ADDR_RANGE_UID);

  private static final ImmutableMap<String, IpSpace> IP_SPACES =
      ImmutableMap.<String, IpSpace>builder()
          .put(ADDR_RANGE_NAME, ADDR_RANGE.accept(ADDR_SPACE_TO_IP_SPACE))
          .build();

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), IP_SPACES);

  private static final Flow TEST_FLOW =
      Flow.builder()
          .setIngressNode("node")
          .setSrcPort(12345)
          .setDstPort(23456)
          .setIpProtocol(IpProtocol.TCP)
          .setIngressInterface("eth1")
          .setSrcIp(Ip.parse("10.0.1.2"))
          .setDstIp(Ip.parse("10.0.2.2"))
          .build();

  @Test
  public void testMatchExpr() {
    AddressSpaceToMatchExpr addrSpaceToMatchExpr = new AddressSpaceToMatchExpr(ImmutableMap.of());
    IpSpace ipSpace = Ip.parse("10.10.10.10").toIpSpace();

    addrSpaceToMatchExpr.setMatchSource(true);
    AclLineMatchExpr srcExpr = addrSpaceToMatchExpr.matchExpr(ipSpace, TraceElement.of("text"));
    addrSpaceToMatchExpr.setMatchSource(false);
    AclLineMatchExpr dstExpr = addrSpaceToMatchExpr.matchExpr(ipSpace, TraceElement.of("text"));

    assertThat(_tb.toBDD(srcExpr), equalTo(_tb.toBDD(AclLineMatchExprs.matchSrc(ipSpace))));
    assertThat(_tb.toBDD(dstExpr), equalTo(_tb.toBDD(AclLineMatchExprs.matchDst(ipSpace))));
  }

  @Test
  public void testCpmiAnyObject() {
    AddressSpaceToMatchExpr addrSpaceToMatchExpr = new AddressSpaceToMatchExpr(ImmutableMap.of());
    AclLineMatchExpr expr = new CpmiAnyObject(Uid.of("1")).accept(addrSpaceToMatchExpr);
    assertBddsEqual(expr, TrueExpr.INSTANCE);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(addressCpmiAnyTraceElement(false)));
  }

  @Test
  public void testAddressRange() {
    AddressSpaceToMatchExpr addrSpaceToMatchExpr = new AddressSpaceToMatchExpr(ImmutableMap.of());

    AclLineMatchExpr expr = ADDR_RANGE.accept(addrSpaceToMatchExpr);
    assertBddsEqual(expr, AclLineMatchExprs.matchDst(Prefix.parse("10.0.0.0/24")));
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstIp(Ip.parse("10.0.0.1")).build(),
            "eth1",
            ImmutableMap.of(),
            IP_SPACES,
            ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(addressRangeTraceElement(ADDR_RANGE, false)));
  }

  @Test
  public void testGroup() {
    //    Uid service1Uid = Uid.of("1");
    //    Uid service2Uid = Uid.of("2");
    //    Uid service3Uid = Uid.of("3");
    //    Uid group1Uid = Uid.of("11");
    //    Uid group2Uid = Uid.of("12");
    //    Uid group3Uid = Uid.of("13");
    //    // Contains a loop; group1 -> group2 -> group3 -> group1
    //    ServiceGroup group1 =
    //        new ServiceGroup("group1", ImmutableList.of(group2Uid, service1Uid), group1Uid);
    //    ServiceGroup group2 =
    //        new ServiceGroup("group2", ImmutableList.of(group3Uid, service2Uid), group2Uid);
    //    ServiceGroup group3 =
    //        new ServiceGroup("group3", ImmutableList.of(group1Uid, service3Uid), group3Uid);
    //    ServiceTcp service1 = new ServiceTcp("service1", "100", service1Uid);
    //    ServiceUdp service2 = new ServiceUdp("service2", "200", service1Uid);
    //    ServiceUdp service3 = new ServiceUdp("service3", "300", service1Uid);
    //    ServiceToMatchExpr serviceToMatchExpr =
    //        new ServiceToMatchExpr(
    //            ImmutableMap.<Uid, NamedManagementObject>builder()
    //                .put(group1Uid, group1)
    //                .put(group2Uid, group2)
    //                .put(group3Uid, group3)
    //                .put(service1Uid, service1)
    //                .put(service2Uid, service2)
    //                .put(service3Uid, service3)
    //                .build());
    //
    //    AclLineMatchExpr expr = group1.accept(serviceToMatchExpr);
    //    assertBddsEqual(
    //        expr,
    //        AclLineMatchExprs.or(
    //            ImmutableList.of(
    //                AclLineMatchExprs.match(
    //                    HeaderSpace.builder()
    //                        .setIpProtocols(IpProtocol.TCP)
    //                        .setDstPorts(new SubRange(100))
    //                        .build()),
    //                AclLineMatchExprs.match(
    //                    HeaderSpace.builder()
    //                        .setIpProtocols(IpProtocol.UDP)
    //                        .setDstPorts(new SubRange(200), new SubRange(300))
    //                        .build()))));
    //
    //    List<TraceTree> trace =
    //        AclTracer.trace(
    //            expr,
    //            TEST_FLOW.toBuilder().setDstPort(300).setIpProtocol(IpProtocol.UDP).build(),
    //            "eth1",
    //            ImmutableMap.of(),
    //            ImmutableMap.of(),
    //            ImmutableMap.of());
    //    assertThat(
    //        trace.get(0),
    //        isTraceTree(
    //            serviceGroupTraceElement(group1),
    //            isTraceTree(
    //                serviceGroupTraceElement(group2),
    //                isTraceTree(
    //                    serviceGroupTraceElement(group3),
    //                    hasTraceElement(serviceUdpTraceElement(service3))))));
  }

  @Test
  public void testGroupInvalidMember() {
    //    Uid unknown1Uid = Uid.of("1");
    //    Uid group1Uid = Uid.of("11");
    //    ServiceGroup group1 = new ServiceGroup("group1", ImmutableList.of(unknown1Uid),
    // group1Uid);
    //    ServiceToMatchExpr serviceToMatchExpr =
    //        new ServiceToMatchExpr(
    //            ImmutableMap.<Uid, NamedManagementObject>builder().put(group1Uid,
    // group1).build());
    //
    //    AclLineMatchExpr expr = group1.accept(serviceToMatchExpr);
    //    // Group containing only invalid members should not match
    //    assertBddsEqual(expr, FalseExpr.INSTANCE);
  }

  private void assertBddsEqual(AclLineMatchExpr left, AclLineMatchExpr right) {
    assertThat(_tb.toBDD(left), equalTo(_tb.toBDD(right)));
  }
}
