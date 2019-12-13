package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.junit.Test;

/** Test for {@link MatchSessionStep}. */
public final class MatchSessionStepTest {
  @Test
  public void testConstructor() {
    Set<String> incomingInterfaces = ImmutableSet.of("a");
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(Accept.INSTANCE)
                .build());
    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);
    assertEquals(step.getDetail().getIncomingInterfaces(), incomingInterfaces);
    assertEquals(step.getDetail().getSessionAction(), Accept.INSTANCE);

    incomingInterfaces = ImmutableSet.of("b");
    step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(FibLookup.INSTANCE)
                .build());
    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);
    assertEquals(step.getDetail().getIncomingInterfaces(), incomingInterfaces);
    assertEquals(step.getDetail().getSessionAction(), FibLookup.INSTANCE);

    ForwardOutInterface forwardAction =
        new ForwardOutInterface("a", NodeInterfacePair.of("a", "b"));
    step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(forwardAction)
                .build());
    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);
    assertEquals(step.getDetail().getIncomingInterfaces(), incomingInterfaces);
    assertEquals(step.getDetail().getSessionAction(), forwardAction);
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Set<String> incomingInterfaces = ImmutableSet.of("b");
    ForwardOutInterface forwardAction =
        new ForwardOutInterface("a", NodeInterfacePair.of("a", "b"));
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(forwardAction)
                .build());
    MatchSessionStep clone = BatfishObjectMapper.clone(step, MatchSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertThat(
        clone.getDetail().getIncomingInterfaces(),
        allOf(equalTo(step.getDetail().getIncomingInterfaces()), equalTo(incomingInterfaces)));
    assertThat(
        clone.getDetail().getSessionAction(),
        allOf(equalTo(step.getDetail().getSessionAction()), equalTo(forwardAction)));
  }
}
