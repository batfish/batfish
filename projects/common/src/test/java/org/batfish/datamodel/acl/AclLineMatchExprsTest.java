package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.AclLineMatchExprs.NEW_FLOWS;
import static org.batfish.datamodel.acl.AclLineMatchExprs.NEW_TCP_FLOWS;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.function.Supplier;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

public class AclLineMatchExprsTest {
  private static boolean matches(AclLineMatchExpr expr, Flow flow) {
    return new Evaluator(flow, "src", ImmutableMap.of(), ImmutableMap.of()).visit(expr);
  }

  private static final Flow ACK_FLOW;
  private static final Flow RST_FLOW;
  private static final Flow SYN_FLOW;
  private static final Flow SYN_ACK_FLOW;
  private static final Flow UDP_FLOW;

  static {
    Supplier<Flow.Builder> fb = () -> Flow.builder().setIngressNode("n");
    Supplier<Flow.Builder> tcpFb =
        () -> fb.get().setIpProtocol(IpProtocol.TCP).setSrcPort(1).setDstPort(2);
    ACK_FLOW = tcpFb.get().setTcpFlagsAck(true).build();
    RST_FLOW = tcpFb.get().setTcpFlagsRst(true).build();
    SYN_FLOW = tcpFb.get().setTcpFlagsSyn(true).build();
    SYN_ACK_FLOW = tcpFb.get().setTcpFlagsSyn(true).setTcpFlagsAck(true).build();

    UDP_FLOW = fb.get().setIpProtocol(IpProtocol.UDP).setSrcPort(1).setDstPort(2).build();
  }

  @Test
  public void testAnd() {
    assertThat(and(ImmutableList.of()), equalTo(TrueExpr.INSTANCE));

    MatchSrcInterface expr = matchSrcInterface("a");
    assertThat(and(ImmutableList.of(expr)), equalTo(expr));
  }

  @Test
  public void testOr() {
    assertThat(or(ImmutableList.of()), equalTo(FalseExpr.INSTANCE));

    MatchSrcInterface expr = matchSrcInterface("a");
    assertThat(or(ImmutableList.of(expr)), equalTo(expr));
  }

  @Test
  public void testNewTcpFlows() {
    assertFalse(matches(NEW_TCP_FLOWS, ACK_FLOW));
    assertFalse(matches(NEW_TCP_FLOWS, ACK_FLOW));
    assertFalse(matches(NEW_TCP_FLOWS, RST_FLOW));
    assertFalse(matches(NEW_TCP_FLOWS, UDP_FLOW));
    assertTrue(matches(NEW_TCP_FLOWS, SYN_FLOW));
    assertFalse(matches(NEW_TCP_FLOWS, SYN_ACK_FLOW));
  }

  @Test
  public void testNewFlows() {
    assertFalse(matches(NEW_FLOWS, ACK_FLOW));
    assertFalse(matches(NEW_FLOWS, RST_FLOW));
    assertFalse(matches(NEW_FLOWS, SYN_ACK_FLOW));
    assertTrue(matches(NEW_FLOWS, SYN_FLOW));
    assertTrue(matches(NEW_FLOWS, UDP_FLOW));
    assertFalse(matches(NEW_FLOWS, SYN_FLOW.toBuilder().setFragmentOffset(1).build()));
    assertFalse(matches(NEW_FLOWS, UDP_FLOW.toBuilder().setFragmentOffset(1).build()));
  }
}
