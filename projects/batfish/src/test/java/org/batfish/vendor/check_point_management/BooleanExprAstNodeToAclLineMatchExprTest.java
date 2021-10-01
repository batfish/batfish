package org.batfish.vendor.check_point_management;

import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.applications.PortsApplication.MAX_PORT_NUMBER;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.vendor.check_point_management.BooleanExprAstNodeToAclLineMatchExpr.convert;
import static org.batfish.vendor.check_point_management.BooleanExprAstNodeToAclLineMatchExpr.inspectTraceElement;
import static org.batfish.vendor.check_point_management.BooleanExprAstNodeToAclLineMatchExpr.portRangeToIntegerSpace;
import static org.batfish.vendor.check_point_management.BooleanExprAstNodeToAclLineMatchExpr.unhandledInspectTraceElement;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.List;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.vendor.check_point_management.parsing.parboiled.ConjunctionAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.DisjunctionAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.DportAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.EmptyAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.EqualsAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.ErrorAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.GreaterThanAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.GreaterThanOrEqualsAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.IncomingAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.LessThanAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.LessThanOrEqualsAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.OutgoingAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.TcpAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.UdpAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.UhDportAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.Uint16AstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.UnhandledAstNode;
import org.junit.Test;

/** Test of {@link BooleanExprAstNodeToAclLineMatchExpr}. */
public final class BooleanExprAstNodeToAclLineMatchExprTest {

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  private void assertBddsEqual(AclLineMatchExpr left, AclLineMatchExpr right) {
    assertThat(_tb.toBDD(left), equalTo(_tb.toBDD(right)));
  }

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
  public void testConvertConjunction() {
    assertBddsEqual(convert(new ConjunctionAstNode(), true), TRUE);
    assertBddsEqual(convert(new ConjunctionAstNode(UnhandledAstNode.of("foo")), true), TRUE);
    assertBddsEqual(convert(new ConjunctionAstNode(UnhandledAstNode.of("foo")), false), FALSE);
    assertBddsEqual(
        convert(
            new ConjunctionAstNode(
                UdpAstNode.instance(),
                new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1))),
            false),
        and(matchIpProtocol(UDP), matchDstPort(1)));

    // tracing
    AclLineMatchExpr expr = convert(new ConjunctionAstNode(UnhandledAstNode.of("foo")), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(unhandledInspectTraceElement("foo")));
  }

  @Test
  public void testConvertDisjunction() {
    assertBddsEqual(convert(new DisjunctionAstNode(), true), FALSE);
    assertBddsEqual(convert(new DisjunctionAstNode(UnhandledAstNode.of("foo")), true), TRUE);
    assertBddsEqual(convert(new DisjunctionAstNode(UnhandledAstNode.of("foo")), false), FALSE);
    assertBddsEqual(
        convert(
            new DisjunctionAstNode(
                UdpAstNode.instance(),
                new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1))),
            true),
        or(matchIpProtocol(UDP), matchDstPort(1)));

    // tracing
    AclLineMatchExpr expr = convert(new DisjunctionAstNode(UnhandledAstNode.of("foo")), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(unhandledInspectTraceElement("foo")));
  }

  @Test
  public void testConvertDport() {
    assertBddsEqual(
        convert(new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)), true),
        matchDstPort(1));
    assertBddsEqual(
        convert(new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)), false),
        matchDstPort(1));

    // tracing
    AclLineMatchExpr expr =
        convert(new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setDstPort(1).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(inspectTraceElement("foo")));
  }

  @Test
  public void testConvertEmpty() {
    assertBddsEqual(convert(EmptyAstNode.instance(), true), TRUE);
    assertBddsEqual(convert(EmptyAstNode.instance(), false), TRUE);
  }

  @Test
  public void testConvertError() {
    assertBddsEqual(convert(ErrorAstNode.instance(), true), FALSE);
    assertBddsEqual(convert(ErrorAstNode.instance(), false), FALSE);
  }

  @Test
  public void testConvertIncoming() {
    // TODO: support direction
    assertBddsEqual(convert(new IncomingAstNode("foo"), true), TRUE);
    assertBddsEqual(convert(new IncomingAstNode("foo"), false), FALSE);

    // tracing
    AclLineMatchExpr expr = convert(new IncomingAstNode("foo"), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(unhandledInspectTraceElement("foo")));
  }

  @Test
  public void testConvertOutgoing() {
    // TODO: support direction
    assertBddsEqual(convert(new OutgoingAstNode("foo"), true), TRUE);
    assertBddsEqual(convert(new OutgoingAstNode("foo"), false), FALSE);

    // tracing
    AclLineMatchExpr expr = convert(new OutgoingAstNode("foo"), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(unhandledInspectTraceElement("foo")));
  }

  @Test
  public void testConvertTcp() {
    assertBddsEqual(convert(TcpAstNode.instance(), true), matchIpProtocol(TCP));
    assertBddsEqual(convert(TcpAstNode.instance(), false), matchIpProtocol(TCP));

    // tracing
    AclLineMatchExpr expr = convert(TcpAstNode.instance(), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setIpProtocol(TCP).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(inspectTraceElement("tcp")));
  }

  @Test
  public void testConvertUdp() {
    assertBddsEqual(convert(UdpAstNode.instance(), true), matchIpProtocol(UDP));
    assertBddsEqual(convert(UdpAstNode.instance(), false), matchIpProtocol(UDP));

    // tracing
    AclLineMatchExpr expr = convert(UdpAstNode.instance(), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setIpProtocol(UDP).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(inspectTraceElement("udp")));
  }

  @Test
  public void testConvertUhDport() {
    assertBddsEqual(
        convert(new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)), true),
        and(matchIpProtocol(UDP), matchDstPort(1)));
    assertBddsEqual(
        convert(new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)), false),
        and(matchIpProtocol(UDP), matchDstPort(1)));

    // tracing
    AclLineMatchExpr expr =
        convert(new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr,
            TEST_FLOW.toBuilder().setIpProtocol(UDP).setDstPort(1).build(),
            "eth1",
            ImmutableMap.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(inspectTraceElement("foo")));
  }

  @Test
  public void testConvertUnhandled() {
    assertBddsEqual(convert(UnhandledAstNode.of("foo"), true), TRUE);
    assertBddsEqual(convert(UnhandledAstNode.of("foo"), false), FALSE);

    // tracing
    AclLineMatchExpr expr = convert(UnhandledAstNode.of("foo"), true);
    List<TraceTree> trace =
        AclTracer.trace(
            expr, TEST_FLOW, "eth1", ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace.get(0), isTraceTree(unhandledInspectTraceElement("foo")));
  }

  @Test
  public void testPortRangeToIntegerSpaceEquals() {
    assertThat(portRangeToIntegerSpace(EqualsAstNode.instance(), 5), equalTo(IntegerSpace.of(5)));
    assertThat(portRangeToIntegerSpace(EqualsAstNode.instance(), -5), equalTo(IntegerSpace.EMPTY));
    assertThat(
        portRangeToIntegerSpace(EqualsAstNode.instance(), 100000), equalTo(IntegerSpace.EMPTY));
  }

  @Test
  public void testPortRangeToIntegerSpaceGreaterThan() {
    assertThat(
        portRangeToIntegerSpace(GreaterThanAstNode.instance(), 5),
        equalTo(IntegerSpace.of(Range.closed(6, MAX_PORT_NUMBER))));
    assertThat(
        portRangeToIntegerSpace(GreaterThanAstNode.instance(), 100000),
        equalTo(IntegerSpace.EMPTY));
    assertThat(
        portRangeToIntegerSpace(GreaterThanAstNode.instance(), -5),
        equalTo(IntegerSpace.of(Range.closed(0, MAX_PORT_NUMBER))));
  }

  @Test
  public void testPortRangeToIntegerSpaceGreaterThanOrEquals() {
    assertThat(
        portRangeToIntegerSpace(GreaterThanOrEqualsAstNode.instance(), 5),
        equalTo(IntegerSpace.of(Range.closed(5, MAX_PORT_NUMBER))));
    assertThat(
        portRangeToIntegerSpace(GreaterThanOrEqualsAstNode.instance(), 100000),
        equalTo(IntegerSpace.EMPTY));
    assertThat(portRangeToIntegerSpace(GreaterThanOrEqualsAstNode.instance(), -5), equalTo(PORTS));
  }

  @Test
  public void testPortRangeToIntegerSpaceLessThan() {
    assertThat(
        portRangeToIntegerSpace(LessThanAstNode.instance(), 5),
        equalTo(IntegerSpace.of(Range.closed(0, 4))));
    assertThat(portRangeToIntegerSpace(LessThanAstNode.instance(), 100000), equalTo(PORTS));
    assertThat(
        portRangeToIntegerSpace(LessThanAstNode.instance(), -5), equalTo(IntegerSpace.EMPTY));
  }

  @Test
  public void testPortRangeToIntegerSpaceLessThanOrEquals() {
    assertThat(
        portRangeToIntegerSpace(LessThanOrEqualsAstNode.instance(), 5),
        equalTo(IntegerSpace.of(Range.closed(0, 5))));
    assertThat(portRangeToIntegerSpace(LessThanOrEqualsAstNode.instance(), 100000), equalTo(PORTS));
    assertThat(
        portRangeToIntegerSpace(LessThanOrEqualsAstNode.instance(), -5),
        equalTo(IntegerSpace.EMPTY));
  }

  @Test
  public void testInspectTraceElement() {
    assertThat(inspectTraceElement("foo"), equalTo(TraceElement.of("Matched: 'foo'")));
  }

  @Test
  public void testUnhandledInspectTraceElement() {
    assertThat(
        unhandledInspectTraceElement("foo"),
        equalTo(TraceElement.of("Assumed matched since unsupported: 'foo'")));
  }
}
