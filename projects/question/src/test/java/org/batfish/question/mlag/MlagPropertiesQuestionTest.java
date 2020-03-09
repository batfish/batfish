package org.batfish.question.mlag;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link org.batfish.question.mlag.MlagPropertiesQuestion} */
public final class MlagPropertiesQuestionTest {
  @Test
  public void testJsonSerialization() {
    MlagPropertiesQuestion q = new MlagPropertiesQuestion("nodes", "mlags");
    assertThat(BatfishObjectMapper.clone(q, MlagPropertiesQuestion.class), equalTo(q));
  }
}
