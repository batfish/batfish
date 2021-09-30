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
    assertThat(parse("blah"), equalTo(UnhandledAstNode.of("blah")));
    assertThat(parse("1"), equalTo(UnhandledAstNode.of("1")));
  }

  @Test
  public void testUnhandledComparisonExpr() {
    assertThat(parse("foo=bar"), equalTo(UnhandledAstNode.of("foo=bar")));
    assertThat(parse("foo = bar"), equalTo(UnhandledAstNode.of("foo = bar")));
  }

  @Test
  public void testCallExpr() {
    {
      String text = "SERVICE_HANDLER(ADP_SQL_CMDS_ID, adp_mssql_monitor_code)";
      assertThat(parse(text), equalTo(UnhandledAstNode.of(text)));
    }
    {
      String text = "IPV4_VER (ip_ttl < 30)";
      assertThat(parse(text), equalTo(UnhandledAstNode.of(text)));
    }
  }

  @Test
  public void testError() {
    assertThat(parse("("), equalTo(ErrorAstNode.instance()));
  }

  @Test
  public void testDport() {
    assertThat(
        parse("dport < 5"),
        equalTo(new DportAstNode("dport < 5", LessThanAstNode.instance(), Uint16AstNode.of(5))));
  }

  @Test
  public void testUhDport() {
    assertThat(
        parse("uh_dport < 5"),
        equalTo(
            new UhDportAstNode("uh_dport < 5", LessThanAstNode.instance(), Uint16AstNode.of(5))));
  }

  @Test
  public void testDisjunction() {
    // 2 items
    assertThat(
        parse("dport < 5 or dport>1"),
        equalTo(
            new DportAstNode("dpot < 5", LessThanAstNode.instance(), Uint16AstNode.of(5))
                .or(
                    new DportAstNode(
                        "dport>1", GreaterThanAstNode.instance(), Uint16AstNode.of(1)))));

    // 3 items
    assertThat(
        parse("dport < 5 or dport>1 or dport>2"),
        equalTo(
            new DportAstNode("dport < 5", LessThanAstNode.instance(), Uint16AstNode.of(5))
                .or(new DportAstNode("dport>1", GreaterThanAstNode.instance(), Uint16AstNode.of(1)))
                .or(
                    new DportAstNode(
                        "dport>2", GreaterThanAstNode.instance(), Uint16AstNode.of(2)))));
  }

  @Test
  public void testConjunction() {
    // 2 items
    assertThat(
        parse("dport < 5 , dport>1"),
        equalTo(
            new DportAstNode("dport < 5", LessThanAstNode.instance(), Uint16AstNode.of(5))
                .and(
                    new DportAstNode(
                        "dport>1", GreaterThanAstNode.instance(), Uint16AstNode.of(1)))));

    // 3 items
    assertThat(
        parse("dport < 5, foo= bar,dport>2"),
        equalTo(
            new DportAstNode("dport < 5", LessThanAstNode.instance(), Uint16AstNode.of(5))
                .and(UnhandledAstNode.of("foo= bar"))
                .and(
                    new DportAstNode(
                        "dport>2", GreaterThanAstNode.instance(), Uint16AstNode.of(2)))));
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
    assertThat(parse("direction=0"), equalTo(new IncomingAstNode("direction=0")));
    assertThat(parse("direction = 1"), equalTo(new OutgoingAstNode("direction = 1")));
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
    assertThat(
        parse("(IPV4_VER (ip_ttl < 30))"), equalTo(UnhandledAstNode.of("IPV4_VER (ip_ttl < 30)")));
    assertThat(
        parse("tcp, (IPV4_VER (ip_ttl < 30))"),
        equalTo(TcpAstNode.instance().and(UnhandledAstNode.of("IPV4_VER (ip_ttl < 30)"))));
  }

  @Test
  public void testInExpr() {
    {
      String text = "<src, dst, dport> in mgcp_dynamic_port";
      assertThat(parse(text), equalTo(UnhandledAstNode.of(text)));
    }
    {
      assertThat(
          parse("tcp,<src, dst, dport>in mgcp_dynamic_port , udp"),
          equalTo(
              TcpAstNode.instance()
                  .and(UnhandledAstNode.of("<src, dst, dport>in mgcp_dynamic_port"))
                  .and(UdpAstNode.instance())));
    }
  }
}
