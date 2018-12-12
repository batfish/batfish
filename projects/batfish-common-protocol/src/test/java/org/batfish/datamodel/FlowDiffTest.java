package org.batfish.datamodel;

import static org.batfish.datamodel.Flow.PROP_DST_IP;
import static org.batfish.datamodel.Flow.PROP_SRC_IP;
import static org.batfish.datamodel.FlowDiff.flowDiffs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link FlowDiff}. */
public final class FlowDiffTest {
  @Test
  public void testFlowDiffs() {
    Ip dst1 = new Ip("1.1.1.1");
    Ip src1 = new Ip("2.2.2.2");
    Ip dst2 = new Ip("3.3.3.3");
    Ip src2 = new Ip("4.4.4.4");
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
}
