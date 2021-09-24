package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.batfish.vendor.check_point_management.parsing.parboiled.ServiceOtherMatchExpr.LOW_UDP_PORT;
import static org.batfish.vendor.check_point_management.parsing.parboiled.ServiceOtherMatchExpr.parse;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Test of {@link ServiceOtherMatchExpr}. */
public final class ServiceOtherMatchExprTest {

  @Test
  public void testEmpty() {
    assertThat(parse(""), equalTo(EmptyAstNode.instance()));
    assertThat(parse(" "), equalTo(EmptyAstNode.instance()));
  }

  @Test
  public void testUnhandledWord() {
    assertThat(parse("blah"), equalTo(UnhandledAstNode.instance()));
    assertThat(parse("1"), equalTo(UnhandledAstNode.instance()));
  }

  @Test
  public void testUnhandledComparisonExpr() {
    assertThat(parse("foo=bar"), equalTo(UnhandledAstNode.instance()));
    assertThat(parse("foo = bar"), equalTo(UnhandledAstNode.instance()));
  }

  @Test
  public void testCallExpr() {
    assertThat(
        parse("SERVICE_HANDLER(ADP_SQL_CMDS_ID, adp_mssql_monitor_code)"),
        equalTo(UnhandledAstNode.instance()));
    assertThat(parse("IPV4_VER (ip_ttl < 30)"), equalTo(UnhandledAstNode.instance()));
  }

  @Test
  public void testError() {
    assertThat(parse("("), equalTo(ErrorAstNode.instance()));
  }

  @Test
  public void testDport() {
    assertThat(
        parse("dport < 5"),
        equalTo(new DportAstNode(LessThanAstNode.instance(), Uint16AstNode.of(5))));
  }

  @Test
  public void testUhDport() {
    assertThat(
        parse("uh_dport < 5"),
        equalTo(new UhDportAstNode(LessThanAstNode.instance(), Uint16AstNode.of(5))));
  }

  @Test
  public void testDisjunction() {
    // 2 items
    assertThat(
        parse("dport < 5 or dport>1"),
        equalTo(
            new DportAstNode(LessThanAstNode.instance(), Uint16AstNode.of(5))
                .or(new DportAstNode(GreaterThanAstNode.instance(), Uint16AstNode.of(1)))));

    // 3 items
    assertThat(
        parse("dport < 5 or dport>1 or dport>2"),
        equalTo(
            new DportAstNode(LessThanAstNode.instance(), Uint16AstNode.of(5))
                .or(new DportAstNode(GreaterThanAstNode.instance(), Uint16AstNode.of(1)))
                .or(new DportAstNode(GreaterThanAstNode.instance(), Uint16AstNode.of(2)))));
  }

  @Test
  public void testConjunction() {
    // 2 items
    assertThat(
        parse("dport < 5 , dport>1"),
        equalTo(
            new DportAstNode(LessThanAstNode.instance(), Uint16AstNode.of(5))
                .and(new DportAstNode(GreaterThanAstNode.instance(), Uint16AstNode.of(1)))));

    // 3 items
    assertThat(
        parse("dport < 5, foo= bar,dport>2"),
        equalTo(
            new DportAstNode(LessThanAstNode.instance(), Uint16AstNode.of(5))
                .and(UnhandledAstNode.instance())
                .and(new DportAstNode(GreaterThanAstNode.instance(), Uint16AstNode.of(2)))));
  }

  @Test
  public void testComparator() {
    assertThat(parse("<", ServiceOtherMatchExpr::Comparator), equalTo(LessThanAstNode.instance()));
    assertThat(
        parse(">", ServiceOtherMatchExpr::Comparator), equalTo(GreaterThanAstNode.instance()));
    assertThat(
        parse("<=", ServiceOtherMatchExpr::Comparator),
        equalTo(LessThanOrEqualsAstNode.instance()));
    assertThat(
        parse(">=", ServiceOtherMatchExpr::Comparator),
        equalTo(GreaterThanOrEqualsAstNode.instance()));
    assertThat(parse("=", ServiceOtherMatchExpr::Comparator), equalTo(EqualsAstNode.instance()));
  }

  @Test
  public void testUint16Expr() {
    assertThat(parse("5", ServiceOtherMatchExpr::Uint16Expr), equalTo(Uint16AstNode.of(5)));
    assertThat(
        parse("LOW_UDP_PORT", ServiceOtherMatchExpr::Uint16Expr),
        equalTo(Uint16AstNode.of(LOW_UDP_PORT)));
  }

  @Test
  public void testDirectionExpr() {
    assertThat(parse("direction=0"), equalTo(IncomingAstNode.instance()));
    assertThat(parse("direction = 1"), equalTo(OutgoingAstNode.instance()));
  }

  @Test
  public void testTcp() {
    assertThat(parse("tcp"), equalTo(TcpAstNode.instance()));
  }

  @Test
  public void testUdp() {
    assertThat(parse("udp"), equalTo(UdpAstNode.instance()));
  }

  @Test
  public void testParentheticalBooleanExpr() {
    assertThat(parse("(tcp)"), equalTo(TcpAstNode.instance()));
    assertThat(parse("( tcp,udp )"), equalTo(TcpAstNode.instance().and(UdpAstNode.instance())));
    assertThat(
        parse("tcp or ( tcp,udp )"),
        equalTo(TcpAstNode.instance().or(TcpAstNode.instance().and(UdpAstNode.instance()))));
    assertThat(
        parse("tcp or ( tcp,udp or tcp )"),
        equalTo(
            TcpAstNode.instance()
                .or(TcpAstNode.instance().and(UdpAstNode.instance().or(TcpAstNode.instance())))));
    assertThat(parse("(IPV4_VER (ip_ttl < 30))"), equalTo(UnhandledAstNode.instance()));
    assertThat(
        parse("tcp, (IPV4_VER (ip_ttl < 30))"),
        equalTo(TcpAstNode.instance().and(UnhandledAstNode.instance())));
  }

  @Test
  public void testInExpr() {
    assertThat(
        parse("<src, dst, dport> in mgcp_dynamic_port"), equalTo(UnhandledAstNode.instance()));
    assertThat(
        parse("tcp,<src, dst, dport>in mgcp_dynamic_port , udp"),
        equalTo(TcpAstNode.instance().and(UnhandledAstNode.instance()).and(UdpAstNode.instance())));
  }
}
