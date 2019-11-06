package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/** Tests for {@Flow}. */
public class FlowTest {
  @Test
  public void testBuildNonIcmpFlow() {
    // IP protocol is not ICMP, so don't include ICMP code or type.
    Flow flow =
        Flow.builder()
            .setIngressNode("node")
            .setIngressVrf("vrf")
            .setTag("tag")
            .setIcmpCode(1)
            .setIcmpType(2)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1)
            .setDstPort(2)
            .build();
    assertThat(flow.getIcmpCode(), nullValue());
    assertThat(flow.getIcmpType(), nullValue());
  }
}
