package org.batfish.question.bgpproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpPeerConfigurationQuestion} */
class BgpPeerConfigurationQuestionTest {

  @Test
  void testJsonSerialization() {
    BgpPeerConfigurationQuestion q =
        new BgpPeerConfigurationQuestion("nodes", BgpPeerPropertySpecifier.LOCAL_IP);
    assertThat(BatfishObjectMapper.clone(q, BgpPeerConfigurationQuestion.class), equalTo(q));
  }
}
