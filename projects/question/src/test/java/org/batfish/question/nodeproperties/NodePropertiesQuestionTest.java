package org.batfish.question.nodeproperties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.junit.Test;

/** Tests for {@link org.batfish.question.nodeproperties.NodePropertiesQuestion} */
public class NodePropertiesQuestionTest {

  @Test
  public void testJsonSerialization() {
    NodePropertiesQuestion q =
        new NodePropertiesQuestion("nodes", NodePropertySpecifier.NTP_SERVERS);
    assertThat(BatfishObjectMapper.clone(q, NodePropertiesQuestion.class), equalTo(q));
  }
}
