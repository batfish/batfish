package org.batfish.datamodel;

import static org.batfish.datamodel.Flow.PROP_DST_IP;
import static org.batfish.datamodel.Flow.PROP_DST_PORT;
import static org.batfish.datamodel.Flow.PROP_SRC_IP;
import static org.batfish.datamodel.Flow.PROP_SRC_PORT;
import static org.batfish.datamodel.FlowDiff.flowDiffs;
import static org.batfish.datamodel.FlowDiff.returnFlowDiffs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link FlowDiff}. */
public final class FlowDiffTest {
  @Test
  public void testFlowDiffs() {
    Ip dst1 = Ip.parse("1.1.1.1");
    Ip src1 = Ip.parse("2.2.2.2");
    Ip dst2 = Ip.parse("3.3.3.3");
    Ip src2 = Ip.parse("4.4.4.4");
    Flow orig = Flow.builder().setIngressNode("ingressNode").setDstIp(dst1).setSrcIp(src1).build();
    Flow newDst = orig.toBuilder().setDstIp(dst2).build();
    Flow newSrc = orig.toBuilder().setSrcIp(src2).build();
    Flow newDstAndSrc = newDst.toBuilder().setSrcIp(src2).build();

    assertThat(flowDiffs(null, orig), empty());
    assertThat(flowDiffs(orig, null), empty());
    assertThat(flowDiffs(orig, orig), empty());

    FlowDiff dstDiff = new FlowDiff(PROP_DST_IP, dst1.toString(), dst2.toString());
    FlowDiff srcDiff = new FlowDiff(PROP_SRC_IP, src1.toString(), src2.toString());
    assertThat(flowDiffs(orig, newDst), contains(dstDiff));
    assertThat(flowDiffs(orig, newSrc), contains(srcDiff));
    assertThat(flowDiffs(orig, newDstAndSrc), containsInAnyOrder(dstDiff, srcDiff));
  }

  @Test
  public void testReturnFlowDiffs() {
    Ip dst1 = Ip.parse("1.1.1.1");
    Ip src1 = Ip.parse("2.2.2.2");
    Ip dst2 = Ip.parse("3.3.3.3");
    Ip src2 = Ip.parse("4.4.4.4");
    Flow origForward =
        Flow.builder().setIngressNode("ingressNode").setDstIp(dst1).setSrcIp(src1).build();
    Flow newForwardDst = origForward.toBuilder().setDstIp(dst2).build();
    Flow newForwardSrc = origForward.toBuilder().setSrcIp(src2).build();
    Flow newForwardDstAndSrc = newForwardDst.toBuilder().setSrcIp(src2).build();

    assertThat(returnFlowDiffs(null, origForward), empty());
    assertThat(returnFlowDiffs(origForward, null), empty());
    assertThat(returnFlowDiffs(origForward, origForward), empty());

    FlowDiff returnDstDiff = new FlowDiff(PROP_DST_IP, src2.toString(), src1.toString());
    FlowDiff returnSrcDiff = new FlowDiff(PROP_SRC_IP, dst2.toString(), dst1.toString());
    assertThat(returnFlowDiffs(origForward, newForwardDst), contains(returnSrcDiff));
    assertThat(returnFlowDiffs(origForward, newForwardSrc), contains(returnDstDiff));
    assertThat(
        returnFlowDiffs(origForward, newForwardDstAndSrc),
        containsInAnyOrder(returnSrcDiff, returnDstDiff));
  }

  @Test
  public void testJackson() {
    FlowDiff fd = new FlowDiff(PROP_DST_IP, "old", "new");
    assertEquals(BatfishObjectMapper.clone(fd, FlowDiff.class), fd);
  }

  @Test
  public void testFlowDiffPort() {
    int dstport1 = 2000;
    int srcport1 = 3000;
    int dstport2 = 4000;
    int srcport2 = 5000;
    Flow orig =
        Flow.builder()
            .setIngressNode("ingressNode")
            .setDstPort(dstport1)
            .setSrcPort(srcport1)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    Flow newDst = orig.toBuilder().setDstPort(dstport2).build();
    Flow newSrc = orig.toBuilder().setSrcPort(srcport2).build();
    Flow newDstAndSrc = newDst.toBuilder().setSrcPort(srcport2).build();

    assertThat(flowDiffs(null, orig), empty());
    assertThat(flowDiffs(orig, null), empty());
    assertThat(flowDiffs(orig, orig), empty());

    FlowDiff dstDiff =
        new FlowDiff(PROP_DST_PORT, Integer.toString(dstport1), Integer.toString(dstport2));
    FlowDiff srcDiff =
        new FlowDiff(PROP_SRC_PORT, Integer.toString(srcport1), Integer.toString(srcport2));
    assertThat(flowDiffs(orig, newDst), contains(dstDiff));
    assertThat(flowDiffs(orig, newSrc), contains(srcDiff));
    assertThat(flowDiffs(orig, newDstAndSrc), containsInAnyOrder(dstDiff, srcDiff));
  }

  @Test
  public void testReturnFlowDiffPort() {
    int dstport1 = 2000;
    int srcport1 = 3000;
    int dstport2 = 4000;
    int srcport2 = 5000;
    Flow origForward =
        Flow.builder()
            .setIngressNode("ingressNode")
            .setDstPort(dstport1)
            .setSrcPort(srcport1)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    Flow newForwardDst = origForward.toBuilder().setDstPort(dstport2).build();
    Flow newForwardSrc = origForward.toBuilder().setSrcPort(srcport2).build();
    Flow newForwardDstAndSrc = newForwardDst.toBuilder().setSrcPort(srcport2).build();

    assertThat(returnFlowDiffs(null, origForward), empty());
    assertThat(returnFlowDiffs(origForward, null), empty());
    assertThat(returnFlowDiffs(origForward, origForward), empty());

    FlowDiff returnDstDiff =
        new FlowDiff(PROP_DST_PORT, Integer.toString(srcport2), Integer.toString(srcport1));
    FlowDiff returnSrcDiff =
        new FlowDiff(PROP_SRC_PORT, Integer.toString(dstport2), Integer.toString(dstport1));
    assertThat(returnFlowDiffs(origForward, newForwardDst), contains(returnSrcDiff));
    assertThat(returnFlowDiffs(origForward, newForwardSrc), contains(returnDstDiff));
    assertThat(
        returnFlowDiffs(origForward, newForwardDstAndSrc),
        containsInAnyOrder(returnDstDiff, returnSrcDiff));
  }
}
