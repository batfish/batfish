package org.batfish.question.vxlanproperties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;
import org.junit.Test;

/** Tests for {@link VxlanVniPropertySpecifier} */
public class VxlanVniPropertiesQuestionTest {

  @Test
  public void testJsonSerialization() {
    VxlanVniPropertiesQuestion q =
        new VxlanVniPropertiesQuestion("nodes", VxlanVniPropertySpecifier.VXLAN_PORT);
    assertThat(BatfishObjectMapper.clone(q, VxlanVniPropertiesQuestion.class), equalTo(q));
  }
}
