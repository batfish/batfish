package org.batfish.question.mlag;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link org.batfish.question.mlag.MlagPropertiesQuestion} */
public final class MlagPropertiesQuestionTest {
  @Test
  public void testJsonSerialization() throws IOException {
    MlagPropertiesQuestion q = new MlagPropertiesQuestion("nodes", "mlags");
    assertThat(BatfishObjectMapper.clone(q, MlagPropertiesQuestion.class), equalTo(q));
  }
}
