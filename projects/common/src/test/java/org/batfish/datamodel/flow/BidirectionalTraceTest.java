package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests for {@link BidirectionalTrace}. */
public final class BidirectionalTraceTest {
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

  @Test
  public void testEquals() {
    Flow f1 =
        Flow.builder()
            .setIngressNode("n1")
            .setIngressVrf("v")
            .setIpProtocol(IpProtocol.ICMP)
            .setSrcIp(Ip.parse("1.1.1.1"))
            .setDstIp(Ip.parse("1.1.1.2"))
            .setPacketLength(64)
            .setIcmpType(0)
            .setIcmpCode(0)
            .build();
    Flow f2 = f1.toBuilder().setIcmpCode(5).build();
    FirewallSessionTraceInfo info =
        new FirewallSessionTraceInfo(
            "n",
            Accept.INSTANCE,
            ImmutableSet.of(),
            new SessionMatchExpr(IpProtocol.ICMP, f1.getSrcIp(), f1.getDstIp(), null, null),
            Transformation.always().build());
    Trace t1 = trace(f1.getIngressNode());
    Trace t2 = trace("n2");
    new EqualsTester()
        .addEqualityGroup(
            new BidirectionalTrace(f1, t1, ImmutableSet.of(), f1, t2),
            new BidirectionalTrace(f1, t1, ImmutableSet.of(), f1, t2))
        .addEqualityGroup(new BidirectionalTrace(f2, t1, ImmutableSet.of(), f1, t2))
        .addEqualityGroup(new BidirectionalTrace(f2, t2, ImmutableSet.of(), f1, t2))
        .addEqualityGroup(new BidirectionalTrace(f2, t2, ImmutableSet.of(info), f1, t2))
        .addEqualityGroup(new BidirectionalTrace(f2, t2, ImmutableSet.of(info), f2, t2))
        .addEqualityGroup(new BidirectionalTrace(f2, t2, ImmutableSet.of(info), f2, t1))
        .testEquals();
  }
}
