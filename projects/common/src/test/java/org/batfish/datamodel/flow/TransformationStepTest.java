package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.junit.Test;

/** Tests of {@link TransformationStep} */
public class TransformationStepTest {
  @Test
  public void testJsonSerialization() {
    TransformationStep step =
        new TransformationStep(
            new TransformationStepDetail(
                TransformationType.DEST_NAT,
                ImmutableSortedSet.of(
                    FlowDiff.flowDiff(IpField.DESTINATION, Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP))),
            StepAction.TRANSFORMED);
    Step<?> genericClonedStep = BatfishObjectMapper.clone(step, Step.class);
    assertThat(genericClonedStep, instanceOf(TransformationStep.class));
    TransformationStep clonedStep = (TransformationStep) genericClonedStep;
    assertThat(clonedStep, equalTo(step));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.DEST_NAT,
                    ImmutableSortedSet.of(
                        FlowDiff.flowDiff(
                            IpField.DESTINATION, Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP))),
                StepAction.TRANSFORMED),
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.DEST_NAT,
                    ImmutableSortedSet.of(
                        FlowDiff.flowDiff(
                            IpField.DESTINATION, Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP))),
                StepAction.TRANSFORMED))
        .addEqualityGroup(
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.SOURCE_NAT,
                    ImmutableSortedSet.of(
                        FlowDiff.flowDiff(
                            IpField.DESTINATION, Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP))),
                StepAction.TRANSFORMED))
        .addEqualityGroup(
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.SOURCE_NAT,
                    ImmutableSortedSet.of(
                        FlowDiff.flowDiff(IpField.SOURCE, Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP))),
                StepAction.TRANSFORMED))
        .addEqualityGroup(
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.SOURCE_NAT, ImmutableSortedSet.of()),
                StepAction.PERMITTED))
        .testEquals();
  }
}
