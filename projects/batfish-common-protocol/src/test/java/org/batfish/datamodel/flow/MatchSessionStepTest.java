package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.junit.Test;

/** Test for {@link MatchSessionStep}. */
public final class MatchSessionStepTest {
  @Test
  public void testConstructor() {
    Set<String> incomingInterfaces = ImmutableSet.of("a");
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder().setIncomingInterfaces(incomingInterfaces).build());

    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);
    assertEquals(step.getDetail().getIncomingInterfaces(), incomingInterfaces);
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Set<String> incomingInterfaces = ImmutableSet.of("b");
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStep.MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .build());
    MatchSessionStep clone = BatfishObjectMapper.clone(step, MatchSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(
        step.getDetail().getIncomingInterfaces(), clone.getDetail().getIncomingInterfaces());
    assertEquals(clone.getDetail().getIncomingInterfaces(), incomingInterfaces);
  }
}
