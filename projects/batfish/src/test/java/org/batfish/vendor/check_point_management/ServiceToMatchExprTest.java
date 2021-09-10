package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.applications.PortsApplication.MAX_PORT_NUMBER;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.portRangeStringToIntegerSpace;
import static org.batfish.vendor.check_point_management.ServiceToMatchExpr.portStringToIntegerSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

/** Test of {@link ServiceToMatchExpr}. */
public final class ServiceToMatchExprTest {
  private final ServiceToMatchExpr _serviceToMatchExpr = new ServiceToMatchExpr(ImmutableMap.of());

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testCpmiAnyObject() {
    assertBddsEqual(_serviceToMatchExpr.visit(new CpmiAnyObject(Uid.of("1"))), TrueExpr.INSTANCE);
  }

  @Test
  public void testIcmp() {
    Service service = new ServiceIcmp("icmp", 1, 2, Uid.of("1"));
    assertBddsEqual(
        service.accept(_serviceToMatchExpr),
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpTypes(1)
                .setIcmpCodes(2)
                .build()));
    Service serviceNoCode = new ServiceIcmp("icmp", 1, null, Uid.of("1"));
    assertBddsEqual(
        serviceNoCode.accept(_serviceToMatchExpr),
        AclLineMatchExprs.match(
            HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).setIcmpTypes(1).build()));
  }

  @Test
  public void testTcp() {
    assertBddsEqual(
        _serviceToMatchExpr.visit(new ServiceTcp("tcp", "100-105,300", Uid.of("1"))),
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setDstPorts(ImmutableList.of(new SubRange(100, 105), new SubRange(300)))
                .build()));
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
    assertBddsEqual(
        _serviceToMatchExpr.visit(new ServiceUdp("udp", "222", Uid.of("1"))),
        AclLineMatchExprs.match(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(new SubRange(222))
                .build()));
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
    NamedManagementObject group1 =
        new ServiceGroup("group1", ImmutableList.of(group2Uid, service1Uid), group1Uid);
    NamedManagementObject group2 =
        new ServiceGroup("group2", ImmutableList.of(group3Uid, service2Uid), group2Uid);
    NamedManagementObject group3 =
        new ServiceGroup("group3", ImmutableList.of(group1Uid, service3Uid), group3Uid);
    NamedManagementObject service1 = new ServiceTcp("service1", "100", service1Uid);
    NamedManagementObject service2 = new ServiceUdp("service2", "200", service1Uid);
    NamedManagementObject service3 = new ServiceUdp("service3", "300", service1Uid);
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

    assertBddsEqual(
        ((ServiceGroup) group1).accept(serviceToMatchExpr),
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
