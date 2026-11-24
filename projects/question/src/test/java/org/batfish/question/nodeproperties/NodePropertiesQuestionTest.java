package org.batfish.question.nodeproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.junit.jupiter.api.Test;

/** Tests for {@link org.batfish.question.nodeproperties.NodePropertiesQuestion} */
class NodePropertiesQuestionTest {

  @Test
  void testJsonSerialization() {
    NodePropertiesQuestion q =
        new NodePropertiesQuestion("nodes", NodePropertySpecifier.NTP_SERVERS);
    assertThat(BatfishObjectMapper.clone(q, NodePropertiesQuestion.class), equalTo(q));
  }
}
