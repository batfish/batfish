package org.batfish.question.namedstructures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.junit.Test;

/** Tests for {@link NamedStructuresQuestion} */
public class NamedStructuresQuestionTest {

  @Test
  public void testJsonSerialization() {
    NamedStructuresQuestion q =
        new NamedStructuresQuestion(
            "nodes",
            NamedStructurePropertySpecifier.ROUTING_POLICY,
            "structureNames",
            false,
            false);
    assertThat(BatfishObjectMapper.clone(q, NamedStructuresQuestion.class), equalTo(q));
  }
}
