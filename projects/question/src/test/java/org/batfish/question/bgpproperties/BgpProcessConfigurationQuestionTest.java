package org.batfish.question.bgpproperties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.junit.Test;

/** Tests for {@link BgpProcessConfigurationQuestion} */
public class BgpProcessConfigurationQuestionTest {

  @Test
  public void testJsonSerialization() {
    BgpProcessConfigurationQuestion q =
        new BgpProcessConfigurationQuestion("nodes", BgpProcessPropertySpecifier.MULTIPATH_EBGP);
    assertThat(BatfishObjectMapper.clone(q, BgpProcessConfigurationQuestion.class), equalTo(q));
  }
}
