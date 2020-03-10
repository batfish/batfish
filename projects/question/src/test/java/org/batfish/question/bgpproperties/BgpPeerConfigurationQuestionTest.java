package org.batfish.question.bgpproperties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.junit.Test;

/** Tests for {@link BgpPeerConfigurationQuestion} */
public class BgpPeerConfigurationQuestionTest {

  @Test
  public void testJsonSerialization() {
    BgpPeerConfigurationQuestion q =
        new BgpPeerConfigurationQuestion("nodes", BgpPeerPropertySpecifier.LOCAL_IP);
    assertThat(BatfishObjectMapper.clone(q, BgpPeerConfigurationQuestion.class), equalTo(q));
  }
}
