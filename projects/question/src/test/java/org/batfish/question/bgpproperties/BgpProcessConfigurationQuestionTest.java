package org.batfish.question.bgpproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpProcessConfigurationQuestion} */
class BgpProcessConfigurationQuestionTest {

  @Test
  void testJsonSerialization() {
    BgpProcessConfigurationQuestion q =
        new BgpProcessConfigurationQuestion("nodes", BgpProcessPropertySpecifier.MULTIPATH_EBGP);
    assertThat(BatfishObjectMapper.clone(q, BgpProcessConfigurationQuestion.class), equalTo(q));
  }
}
