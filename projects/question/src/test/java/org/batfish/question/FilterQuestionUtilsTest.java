package org.batfish.question;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class FilterQuestionUtilsTest {
  @Test
  public void testGetFlowWithPreference() {
    BDDPacket pkt = new BDDPacket();
    Optional<Flow> maybeFlow =
        FilterQuestionUtils.getFlow(
            pkt, BDDSourceManager.empty(pkt), "foo", pkt.getFactory().one());
    assertTrue(maybeFlow.isPresent());
    Flow flow = maybeFlow.get();
    assertThat(flow.getSrcIp(), not(equalTo(Ip.ZERO)));
    assertThat(flow.getDstIp(), not(equalTo(Ip.ZERO)));
    assertThat(flow.getIpProtocol().number(), not(equalTo(0)));
  }
}
