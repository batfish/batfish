package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.batfish.datamodel.pojo.Node;
import org.junit.Test;

public class TraceAndReverseFlowTest {
  private static Trace trace(String hostname) {
    return new Trace(
        FlowDisposition.ACCEPTED,
        ImmutableList.of(
            new Hop(
                new Node(hostname),
                ImmutableList.of(
                    OriginateStep.builder()
                        .setAction(StepAction.ORIGINATED)
                        .setDetail(OriginateStepDetail.builder().setOriginatingVrf("vrf").build())
                        .build(),
                    InboundStep.builder().setDetail(new InboundStepDetail("iface")).build()))));
  }

  private static ImmutableList<FirewallSessionTraceInfo> sessions(String hostname) {
    return ImmutableList.of(
        new FirewallSessionTraceInfo(
            hostname,
            PostNatFibLookup.INSTANCE,
            new OriginatingSessionScope("vrf"),
            new SessionMatchExpr(IpProtocol.TCP, Ip.ZERO, Ip.ZERO, 0, 0),
            null));
  }

  @Test
  public void testEquals() {
    Trace trace1 = trace("n1");
    Trace trace2 = trace("n2");
    Flow flow1 = Flow.builder().setIngressNode("n1").build();
    Flow flow2 = Flow.builder().setIngressNode("n2").build();
    List<FirewallSessionTraceInfo> sessions1 = sessions("n1");
    List<FirewallSessionTraceInfo> sessions2 = sessions("n2");
    new EqualsTester()
        .addEqualityGroup(
            new TraceAndReverseFlow(trace1, flow1, sessions1),
            new TraceAndReverseFlow(trace1, flow1, sessions1))
        .addEqualityGroup(new TraceAndReverseFlow(trace2, flow1, sessions1))
        .addEqualityGroup(new TraceAndReverseFlow(trace1, flow2, sessions1))
        .addEqualityGroup(new TraceAndReverseFlow(trace1, flow1, sessions2))
        .testEquals();
  }
}
