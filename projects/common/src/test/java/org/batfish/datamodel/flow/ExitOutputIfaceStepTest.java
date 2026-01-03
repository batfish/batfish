package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.junit.Test;

public class ExitOutputIfaceStepTest {
  @Test
  public void testJsonSerialization() {
    ExitOutputIfaceStep step =
        ExitOutputIfaceStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(
                ExitOutputIfaceStepDetail.builder()
                    .setOutputInterface(NodeInterfacePair.of("node", "iface"))
                    .setTransformedFlow(Flow.builder().setIngressNode("node").build())
                    .build())
            .build();

    Step<?> clonedGenericStep = BatfishObjectMapper.clone(step, Step.class);
    assertThat(clonedGenericStep, instanceOf(ExitOutputIfaceStep.class));
    ExitOutputIfaceStep clonedStep = (ExitOutputIfaceStep) clonedGenericStep;
    assertThat(clonedStep.getAction(), equalTo(StepAction.RECEIVED));
    assertThat(
        clonedStep.getDetail().getOutputInterface(),
        equalTo(NodeInterfacePair.of("node", "iface")));
    assertThat(
        clonedStep.getDetail().getTransformedFlow(),
        equalTo(Flow.builder().setIngressNode("node").build()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            ExitOutputIfaceStep.builder()
                .setAction(StepAction.TRANSMITTED)
                .setDetail(
                    ExitOutputIfaceStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build(),
            ExitOutputIfaceStep.builder()
                .setAction(StepAction.TRANSMITTED)
                .setDetail(
                    ExitOutputIfaceStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build())
        .addEqualityGroup(
            ExitOutputIfaceStep.builder()
                .setAction(StepAction.TRANSMITTED)
                .setDetail(
                    ExitOutputIfaceStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i1"))
                        .build())
                .build())
        .addEqualityGroup(
            ExitOutputIfaceStep.builder()
                .setAction(StepAction.TRANSMITTED)
                .setDetail(
                    ExitOutputIfaceStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i1"))
                        .setTransformedFlow(Flow.builder().setIngressNode("g").build())
                        .build())
                .build())
        .testEquals();
  }
}
