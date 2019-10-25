package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test for {@link MatchSessionStep}. */
public final class MatchSessionStepTest {
  @Test
  public void testJsonSerialization() throws IOException {
    MatchSessionStep step = new MatchSessionStep();
    MatchSessionStep clone = BatfishObjectMapper.clone(step, MatchSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(step.getDetail(), clone.getDetail());
  }
}
