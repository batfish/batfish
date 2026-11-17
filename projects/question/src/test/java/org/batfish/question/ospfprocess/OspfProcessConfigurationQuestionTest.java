package org.batfish.question.ospfprocess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.junit.jupiter.api.Test;

/** Tests for {@link OspfProcessConfigurationQuestion} */
class OspfProcessConfigurationQuestionTest {

  @Test
  void testJsonSerialization() {
    OspfProcessConfigurationQuestion q =
        new OspfProcessConfigurationQuestion("nodes", OspfProcessPropertySpecifier.AREAS);
    assertThat(BatfishObjectMapper.clone(q, OspfProcessConfigurationQuestion.class), equalTo(q));
  }
}
