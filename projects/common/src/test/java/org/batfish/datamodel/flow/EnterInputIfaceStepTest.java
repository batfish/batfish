package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.junit.Test;

public class EnterInputIfaceStepTest {
  @Test
  public void testJsonSerialization() {
    EnterInputIfaceStep step =
        EnterInputIfaceStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(
                EnterInputIfaceStepDetail.builder()
                    .setInputInterface(NodeInterfacePair.of("node", "iface"))
                    .setInputVrf("vrf")
                    .build())
            .build();

    Step<?> clonedGenericStep = BatfishObjectMapper.clone(step, Step.class);
    assertThat(clonedGenericStep, instanceOf(EnterInputIfaceStep.class));
    EnterInputIfaceStep clonedStep = (EnterInputIfaceStep) clonedGenericStep;
    assertThat(clonedStep.getAction(), equalTo(StepAction.RECEIVED));
    assertThat(
        clonedStep.getDetail().getInputInterface(), equalTo(NodeInterfacePair.of("node", "iface")));
    assertThat(clonedStep.getDetail().getInputVrf(), equalTo("vrf"));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            EnterInputIfaceStep.builder()
                .setAction(StepAction.RECEIVED)
                .setDetail(
                    EnterInputIfaceStepDetail.builder()
                        .setInputInterface(NodeInterfacePair.of("h", "i"))
                        .setInputVrf("vrf")
                        .build())
                .build(),
            EnterInputIfaceStep.builder()
                .setAction(StepAction.RECEIVED)
                .setDetail(
                    EnterInputIfaceStepDetail.builder()
                        .setInputInterface(NodeInterfacePair.of("h", "i"))
                        .setInputVrf("vrf")
                        .build())
                .build())
        .addEqualityGroup(
            EnterInputIfaceStep.builder()
                .setAction(StepAction.RECEIVED)
                .setDetail(
                    EnterInputIfaceStepDetail.builder()
                        .setInputInterface(NodeInterfacePair.of("h", "i1"))
                        .setInputVrf("vrf")
                        .build())
                .build())
        .addEqualityGroup(
            EnterInputIfaceStep.builder()
                .setAction(StepAction.RECEIVED)
                .setDetail(
                    EnterInputIfaceStepDetail.builder()
                        .setInputInterface(NodeInterfacePair.of("h", "i1"))
                        .setInputVrf("vrf1")
                        .build())
                .build())
        .testEquals();
  }
}
