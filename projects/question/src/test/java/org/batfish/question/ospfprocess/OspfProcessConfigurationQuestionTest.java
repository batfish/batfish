package org.batfish.question.ospfprocess;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.junit.Test;

/** Tests for {@link OspfProcessConfigurationQuestion} */
public class OspfProcessConfigurationQuestionTest {

  @Test
  public void testJsonSerialization() {
    OspfProcessConfigurationQuestion q =
        new OspfProcessConfigurationQuestion("nodes", OspfProcessPropertySpecifier.AREAS);
    assertThat(BatfishObjectMapper.clone(q, OspfProcessConfigurationQuestion.class), equalTo(q));
  }
}
