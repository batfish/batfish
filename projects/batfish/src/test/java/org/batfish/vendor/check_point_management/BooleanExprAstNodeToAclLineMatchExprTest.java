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
import static org.batfish.vendor.check_point_management.BooleanExprAstNodeToAclLineMatchExpr.convert;
import static org.batfish.vendor.check_point_management.BooleanExprAstNodeToAclLineMatchExpr.portRangeToIntegerSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
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

  @Test
  public void testConvertConjunction() {
    assertBddsEqual(convert(new ConjunctionAstNode(), true), TRUE);
    assertBddsEqual(convert(new ConjunctionAstNode(UnhandledAstNode.of("foo")), true), TRUE);
    assertBddsEqual(convert(new ConjunctionAstNode(UnhandledAstNode.of("foo")), false), FALSE);
    assertBddsEqual(
        convert(
            new ConjunctionAstNode(
                UdpAstNode.instance(),
                new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1))),
            false),
        and(matchIpProtocol(UDP), matchDstPort(1)));
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
                new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1))),
            true),
        or(matchIpProtocol(UDP), matchDstPort(1)));
  }

  @Test
  public void testConvertDport() {
    assertBddsEqual(
        convert(new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1)), true),
        matchDstPort(1));
    assertBddsEqual(
        convert(new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1)), false),
        matchDstPort(1));
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
    assertBddsEqual(convert(IncomingAstNode.instance(), true), TRUE);
    assertBddsEqual(convert(IncomingAstNode.instance(), false), FALSE);
  }

  @Test
  public void testConvertOutgoing() {
    // TODO: support direction
    assertBddsEqual(convert(OutgoingAstNode.instance(), true), TRUE);
    assertBddsEqual(convert(OutgoingAstNode.instance(), false), FALSE);
  }

  @Test
  public void testConvertTcp() {
    assertBddsEqual(convert(TcpAstNode.instance(), true), matchIpProtocol(TCP));
    assertBddsEqual(convert(TcpAstNode.instance(), false), matchIpProtocol(TCP));
  }

  @Test
  public void testConvertUdp() {
    assertBddsEqual(convert(UdpAstNode.instance(), true), matchIpProtocol(UDP));
    assertBddsEqual(convert(UdpAstNode.instance(), false), matchIpProtocol(UDP));
  }

  @Test
  public void testConvertUhDport() {
    assertBddsEqual(
        convert(new UhDportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1)), true),
        and(matchIpProtocol(UDP), matchDstPort(1)));
    assertBddsEqual(
        convert(new UhDportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1)), false),
        and(matchIpProtocol(UDP), matchDstPort(1)));
  }

  @Test
  public void testConvertUnhandled() {
    assertBddsEqual(convert(UnhandledAstNode.of("foo"), true), TRUE);
    assertBddsEqual(convert(UnhandledAstNode.of("foo"), false), FALSE);
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
}
