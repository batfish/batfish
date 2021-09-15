package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.applications.PortsApplication.MAX_PORT_NUMBER;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceCpmiAnyTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceGroupTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceIcmpTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceOtherTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceTcpTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.serviceUdpTraceElement;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.destPortTraceElement;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.icmpCodeTraceElement;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.icmpTypeTraceElement;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.ipProtocolTraceElement;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.matchConditionTraceElement;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.portRangeStringToIntegerSpace;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.portStringToIntegerSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.trace.TraceTree;
import org.junit.Test;

/** Test of {@link ServiceToMatchExpr}. */
public final class ServiceToMatchExprTest {
  private final ServiceToMatchExpr _serviceToMatchExpr = new ServiceToMatchExpr(ImmutableMap.of());

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

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
  public void testCpmiAnyObject() {
    AclLineMatchExpr expr = _serviceToMatchExpr.visit(new CpmiAnyObject(Uid.of("1")));
    assertBddsEqual(expr, TrueExpr.INSTANCE);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(serviceCpmiAnyTraceElement()));
  }

  @Test
  public void testIcmp() {
    ServiceIcmp service = new ServiceIcmp("icmp", 1, 2, Uid.of("1"));
    AclLineMatchExpr expr = service.accept(_serviceToMatchExpr);
    assertBddsEqual(
        expr,
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpTypes(1)
                .setIcmpCodes(2)
                .build()));
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder()
                .setIpProtocol(IpProtocol.ICMP)
                .setIcmpType(1)
                .setIcmpCode(2)
                .build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        trace.get(0),
        isTraceTree(
            serviceIcmpTraceElement(service),
            isTraceTree(ipProtocolTraceElement(IpProtocol.ICMP)),
            isTraceTree(icmpTypeTraceElement(1)),
            isTraceTree(icmpCodeTraceElement(2))));
  }

  @Test
  public void testIcmpNoCode() {
    ServiceIcmp serviceNoCode = new ServiceIcmp("icmp", 1, null, Uid.of("1"));
    AclLineMatchExpr exprNoCode = serviceNoCode.accept(_serviceToMatchExpr);
    assertBddsEqual(
        exprNoCode,
        AclLineMatchExprs.match(
            HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).setIcmpTypes(1).build()));
    List<TraceTree> trace =
        AclTracer.trace(
            exprNoCode,
            TEST_FLOW.toBuilder()
                .setIpProtocol(IpProtocol.ICMP)
                .setIcmpType(1)
                .setIcmpCode(2)
                .build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        trace.get(0),
        isTraceTree(
            serviceIcmpTraceElement(serviceNoCode),
            isTraceTree(ipProtocolTraceElement(IpProtocol.ICMP)),
            isTraceTree(icmpTypeTraceElement(1))));
  }

  @Test
  public void testOther() {
    String match = "uh_dport > 33000, (IPV4_VER (ip_ttl < 30))";
    ServiceOther service = new ServiceOther("udp", 17, match, Uid.of("1"));
    AclLineMatchExpr expr = _serviceToMatchExpr.visit(service);
    assertBddsEqual(
        expr,
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(new SubRange(33001, 65535))
                .build()));

    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstPort(33333).setIpProtocol(IpProtocol.UDP).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        trace.get(0),
        isTraceTree(
            serviceOtherTraceElement(service), isTraceTree(matchConditionTraceElement(match))));
  }

  @Test
  public void testTcp() {
    ServiceTcp service = new ServiceTcp("tcp", "100-105,300", Uid.of("1"));
    AclLineMatchExpr expr = _serviceToMatchExpr.visit(service);
    assertBddsEqual(
        expr,
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(ImmutableList.of(new SubRange(100, 105), new SubRange(300)))
                .build()));
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstPort(100).setIpProtocol(IpProtocol.TCP).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        trace.get(0),
        isTraceTree(
            serviceTcpTraceElement(service),
            isTraceTree(ipProtocolTraceElement(IpProtocol.TCP)),
            isTraceTree(destPortTraceElement("100-105,300"))));

    assertBddsEqual(
        _serviceToMatchExpr.visit(new ServiceTcp("tcp", ">5", Uid.of("1"))),
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(ImmutableList.of(new SubRange(6, 65535)))
                .build()));
  }

  @Test
  public void testUdp() {
    ServiceUdp service = new ServiceUdp("udp", "222", Uid.of("1"));
    AclLineMatchExpr expr = _serviceToMatchExpr.visit(service);
    assertBddsEqual(
        expr,
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(new SubRange(222))
                .build()));

    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstPort(222).setIpProtocol(IpProtocol.UDP).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        trace.get(0),
        isTraceTree(
            serviceUdpTraceElement(service),
            isTraceTree(ipProtocolTraceElement(IpProtocol.UDP)),
            isTraceTree(destPortTraceElement("222"))));
  }

  @Test
  public void testGroup() {
    Uid service1Uid = Uid.of("1");
    Uid service2Uid = Uid.of("2");
    Uid service3Uid = Uid.of("3");
    Uid group1Uid = Uid.of("11");
    Uid group2Uid = Uid.of("12");
    Uid group3Uid = Uid.of("13");
    // Contains a loop; group1 -> group2 -> group3 -> group1
    ServiceGroup group1 =
        new ServiceGroup("group1", ImmutableList.of(group2Uid, service1Uid), group1Uid);
    ServiceGroup group2 =
        new ServiceGroup("group2", ImmutableList.of(group3Uid, service2Uid), group2Uid);
    ServiceGroup group3 =
        new ServiceGroup("group3", ImmutableList.of(group1Uid, service3Uid), group3Uid);
    ServiceTcp service1 = new ServiceTcp("service1", "100", service1Uid);
    ServiceUdp service2 = new ServiceUdp("service2", "200", service1Uid);
    ServiceUdp service3 = new ServiceUdp("service3", "300", service1Uid);
    ServiceToMatchExpr serviceToMatchExpr =
        new ServiceToMatchExpr(
            ImmutableMap.<Uid, NamedManagementObject>builder()
                .put(group1Uid, group1)
                .put(group2Uid, group2)
                .put(group3Uid, group3)
                .put(service1Uid, service1)
                .put(service2Uid, service2)
                .put(service3Uid, service3)
                .build());

    AclLineMatchExpr expr = group1.accept(serviceToMatchExpr);
    assertBddsEqual(
        expr,
        AclLineMatchExprs.or(
            ImmutableList.of(
                AclLineMatchExprs.match(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP)
                        .setDstPorts(new SubRange(100))
                        .build()),
                AclLineMatchExprs.match(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.UDP)
                        .setDstPorts(new SubRange(200), new SubRange(300))
                        .build()))));

    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstPort(300).setIpProtocol(IpProtocol.UDP).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        trace.get(0),
        isTraceTree(
            serviceGroupTraceElement(group1),
            isTraceTree(
                serviceGroupTraceElement(group2),
                isTraceTree(
                    serviceGroupTraceElement(group3),
                    isTraceTree(
                        serviceUdpTraceElement(service3),
                        isTraceTree(ipProtocolTraceElement(IpProtocol.UDP)),
                        isTraceTree(destPortTraceElement("300")))))));
  }

  @Test
  public void testGroupInvalidMember() {
    Uid unknown1Uid = Uid.of("1");
    Uid group1Uid = Uid.of("11");
    ServiceGroup group1 = new ServiceGroup("group1", ImmutableList.of(unknown1Uid), group1Uid);
    ServiceToMatchExpr serviceToMatchExpr =
        new ServiceToMatchExpr(
            ImmutableMap.<Uid, NamedManagementObject>builder().put(group1Uid, group1).build());

    AclLineMatchExpr expr = group1.accept(serviceToMatchExpr);
    // Group containing only invalid members should not match
    assertBddsEqual(expr, FalseExpr.INSTANCE);
  }

  private void assertBddsEqual(AclLineMatchExpr left, AclLineMatchExpr right) {
    assertThat(_tb.toBDD(left), equalTo(_tb.toBDD(right)));
  }

  @Test
  public void testPortRangeStringToIntegerSpace() {
    // Empty string
    assertThat(portRangeStringToIntegerSpace(""), equalTo(IntegerSpace.EMPTY));

    // Single number
    assertThat(portRangeStringToIntegerSpace("1"), equalTo(IntegerSpace.of(1)));
    assertThat(portRangeStringToIntegerSpace("100000"), equalTo(IntegerSpace.EMPTY));

    // Range of two numbers
    assertThat(portRangeStringToIntegerSpace("1-5"), equalTo(IntegerSpace.of(new SubRange(1, 5))));
    assertThat(
        portRangeStringToIntegerSpace("1-100000"),
        equalTo(IntegerSpace.of(new SubRange(1, MAX_PORT_NUMBER))));

    // <=
    assertThat(portRangeStringToIntegerSpace("<=5"), equalTo(IntegerSpace.of(new SubRange(0, 5))));
    assertThat(portRangeStringToIntegerSpace("<=0"), equalTo(IntegerSpace.of(0)));
    assertThat(
        portRangeStringToIntegerSpace("<=100000"),
        equalTo(IntegerSpace.of(new SubRange(0, MAX_PORT_NUMBER))));

    // <
    assertThat(portRangeStringToIntegerSpace("<5"), equalTo(IntegerSpace.of(new SubRange(0, 4))));
    assertThat(portRangeStringToIntegerSpace("<0"), equalTo(IntegerSpace.EMPTY));
    assertThat(
        portRangeStringToIntegerSpace("<100000"),
        equalTo(IntegerSpace.of(new SubRange(0, MAX_PORT_NUMBER))));

    // >=
    assertThat(
        portRangeStringToIntegerSpace(">=5"),
        equalTo(IntegerSpace.of(new SubRange(5, MAX_PORT_NUMBER))));
    assertThat(
        portRangeStringToIntegerSpace(">=0"),
        equalTo(IntegerSpace.of(new SubRange(0, MAX_PORT_NUMBER))));
    assertThat(portRangeStringToIntegerSpace(">=100000"), equalTo(IntegerSpace.EMPTY));

    // >
    assertThat(
        portRangeStringToIntegerSpace(">5"),
        equalTo(IntegerSpace.of(new SubRange(6, MAX_PORT_NUMBER))));
    assertThat(
        portRangeStringToIntegerSpace(">0"),
        equalTo(IntegerSpace.of(new SubRange(1, MAX_PORT_NUMBER))));
    assertThat(portRangeStringToIntegerSpace(">100000"), equalTo(IntegerSpace.EMPTY));
  }

  @Test
  public void testPortStringToIntegerSpace() {
    assertThat(portStringToIntegerSpace(""), equalTo(IntegerSpace.EMPTY));
    assertThat(portStringToIntegerSpace(","), equalTo(IntegerSpace.EMPTY));
    assertThat(portStringToIntegerSpace("50"), equalTo(IntegerSpace.of(50)));
    assertThat(
        portStringToIntegerSpace("50,>65000"),
        equalTo(
            IntegerSpace.builder()
                .including(50)
                .including(new SubRange(65001, MAX_PORT_NUMBER))
                .build()));
    assertThat(
        portStringToIntegerSpace(" 50 , >65000 "),
        equalTo(
            IntegerSpace.builder()
                .including(50)
                .including(new SubRange(65001, MAX_PORT_NUMBER))
                .build()));
  }
}
