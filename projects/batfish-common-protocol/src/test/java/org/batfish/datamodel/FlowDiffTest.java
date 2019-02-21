package org.batfish.datamodel;

import static org.batfish.datamodel.Flow.PROP_DST_IP;
import static org.batfish.datamodel.Flow.PROP_DST_PORT;
import static org.batfish.datamodel.Flow.PROP_SRC_IP;
import static org.batfish.datamodel.Flow.PROP_SRC_PORT;
import static org.batfish.datamodel.FlowDiff.flowDiffs;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
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
    Flow orig =
        Flow.builder()
            .setIngressNode("ingressNode")
            .setTag("tag")
            .setDstIp(dst1)
            .setSrcIp(src1)
            .build();
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
  public void testJackson() throws IOException {
    FlowDiff fd = new FlowDiff(PROP_DST_IP, "old", "new");
    assertEquals(BatfishObjectMapper.clone(fd, FlowDiff.class), fd);
  }

  @Test
  public void testFlowDiffPort() throws IOException {
    int dstport1 = 2000;
    int srcport1 = 3000;
    int dstport2 = 4000;
    int srcport2 = 5000;
    Flow orig =
        Flow.builder()
            .setIngressNode("ingressNode")
            .setTag("tag")
            .setDstPort(dstport1)
            .setSrcPort(srcport1)
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
}
