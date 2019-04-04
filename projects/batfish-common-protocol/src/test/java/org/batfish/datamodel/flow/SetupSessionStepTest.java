package org.batfish.datamodel.flow;

import static org.batfish.datamodel.flow.StepAction.SETUP_SESSION;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;
import org.junit.Test;

/** Tests of {@link SetupSessionStep}. */
public class SetupSessionStepTest {
  @Test
  public void testSerialization() throws IOException {
    SetupSessionStep step = new SetupSessionStep();
    Step<?> cloned = BatfishObjectMapper.clone(step, Step.class);
    assertThat(cloned, instanceOf(SetupSessionStep.class));
    SetupSessionStep clonedStep = (SetupSessionStep) cloned;
    assertThat(clonedStep.getAction(), equalTo(SETUP_SESSION));
    assertThat(clonedStep.getDetail(), instanceOf(SetupSessionStepDetail.class));
  }
}
