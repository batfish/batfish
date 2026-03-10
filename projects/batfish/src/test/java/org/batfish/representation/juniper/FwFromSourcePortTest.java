package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.trace.TraceTree;
import org.junit.Test;

/** Test for {@link FwFromSourcePort} */
public class FwFromSourcePortTest {
  private static void assertMatches(AclLineMatchExpr expr, Flow flow) {
    List<TraceTree> trace =
        AclTracer.trace(expr, flow, null, ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace, hasSize(1));
    assertThat(trace.get(0).getTraceElement().getText(), containsString("Matched source-port"));
  }

  private static void assertNoMatches(AclLineMatchExpr expr, Flow flow) {
    List<TraceTree> trace =
        AclTracer.trace(expr, flow, null, ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    assertThat(trace, empty());
  }

  @Test
  public void testConversion() {
    AclLineMatchExpr converted =
        new FwFromSourcePort(new SubRange(1, 2)).toAclLineMatchExpr(null, null, null);
    Flow testFlow =
        Flow.builder()
            .setIngressNode("c")
            .setSrcPort(1)
            .setDstPort(5)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(Ip.ZERO)
            .setDstIp(Ip.MAX)
            .build();
    assertMatches(converted, testFlow);
    assertMatches(converted, testFlow.toBuilder().setIpProtocol(IpProtocol.UDP).build());
    assertMatches(converted, testFlow.toBuilder().setIpProtocol(IpProtocol.SCTP).build());
    assertNoMatches(converted, testFlow.toBuilder().setIpProtocol(IpProtocol.GRE).build());
    assertMatches(converted, testFlow.toBuilder().setSrcPort(2).build());
    assertNoMatches(converted, testFlow.toBuilder().setSrcPort(5).build());
  }
}
