package org.batfish.question.mlag;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link org.batfish.question.mlag.MlagPropertiesQuestion} */
final class MlagPropertiesQuestionTest {
  @Test
  void testJsonSerialization() {
    MlagPropertiesQuestion q = new MlagPropertiesQuestion("nodes", "mlags");
    assertThat(BatfishObjectMapper.clone(q, MlagPropertiesQuestion.class), equalTo(q));
  }
}
