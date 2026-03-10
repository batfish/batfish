package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.junit.Test;

/** Tests of {@link OriginateStep} */
public class OriginateStepTest {
  @Test
  public void testJsonSerialization() {
    OriginateStep step =
        OriginateStep.builder()
            .setAction(StepAction.ORIGINATED)
            .setDetail(OriginateStepDetail.builder().setOriginatingVrf("vrf").build())
            .build();
    Step<?> genericClonedStep = BatfishObjectMapper.clone(step, Step.class);
    assertThat(genericClonedStep, instanceOf(OriginateStep.class));
    OriginateStep clonedStep = (OriginateStep) genericClonedStep;
    assertThat(clonedStep.getAction(), equalTo(StepAction.ORIGINATED));
    assertThat(clonedStep.getDetail().getOriginatingVrf(), equalTo("vrf"));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            OriginateStep.builder()
                .setAction(StepAction.ORIGINATED)
                .setDetail(OriginateStepDetail.builder().setOriginatingVrf("vrf").build())
                .build(),
            OriginateStep.builder()
                .setAction(StepAction.ORIGINATED)
                .setDetail(OriginateStepDetail.builder().setOriginatingVrf("vrf").build())
                .build())
        .addEqualityGroup(
            OriginateStep.builder()
                .setAction(StepAction.ORIGINATED)
                .setDetail(OriginateStepDetail.builder().setOriginatingVrf("vrf2").build())
                .build())
        .testEquals();
  }
}
