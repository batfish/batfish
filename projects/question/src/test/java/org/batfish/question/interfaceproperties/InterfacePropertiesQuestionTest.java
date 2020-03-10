package org.batfish.question.interfaceproperties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.junit.Test;

/** Tests for {@link InterfacePropertiesQuestion} */
public class InterfacePropertiesQuestionTest {

  @Test
  public void testJsonSerialization() {
    InterfacePropertiesQuestion q =
        new InterfacePropertiesQuestion(
            "nodes", "interfaces", InterfacePropertySpecifier.ACCESS_VLAN, false);
    assertThat(BatfishObjectMapper.clone(q, InterfacePropertiesQuestion.class), equalTo(q));
  }
}
