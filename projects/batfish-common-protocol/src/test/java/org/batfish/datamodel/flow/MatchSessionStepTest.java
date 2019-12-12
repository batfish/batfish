package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.junit.Test;

/** Test for {@link MatchSessionStep}. */
public final class MatchSessionStepTest {
  @Test
  public void testBuilder() {
    Set<String> incomingInterfaces = ImmutableSet.of("a");
    MatchSessionStep step =
        MatchSessionStep.builder()
            .setDetail(
                MatchSessionStepDetail.builder().setIncomingInterfaces(incomingInterfaces).build())
            .build();

    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);

    assertEquals(step.getDetail().getIncomingInterfaces(), incomingInterfaces);
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Set<String> incomingInterfaces = new HashSet<String>();
    incomingInterfaces.add("b");
    MatchSessionStep step =
        MatchSessionStep.builder()
            .setDetail(
                MatchSessionStep.MatchSessionStepDetail.builder()
                    .setIncomingInterfaces(incomingInterfaces)
                    .build())
            .build();
    MatchSessionStep clone = BatfishObjectMapper.clone(step, MatchSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(
        step.getDetail().getIncomingInterfaces(), clone.getDetail().getIncomingInterfaces());
  }
}
