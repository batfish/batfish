package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ArpErrorStep.ArpErrorStepDetail;
import org.junit.Test;

public class ArpErrorStepTest {

  @Test
  public void testJsonSerialization() {
    ArpErrorStep step =
        ArpErrorStep.builder()
            .setAction(StepAction.NEIGHBOR_UNREACHABLE)
            .setDetail(
                ArpErrorStepDetail.builder()
                    .setOutputInterface(NodeInterfacePair.of("node", "iface"))
                    .setResolvedNexthopIp(Ip.parse("1.1.1.1"))
                    .build())
            .build();

    ArpErrorStep clonedStep = BatfishObjectMapper.clone(step, ArpErrorStep.class);
    assertThat(clonedStep.getAction(), equalTo(StepAction.NEIGHBOR_UNREACHABLE));
    assertThat(
        clonedStep.getDetail().getOutputInterface(),
        equalTo(NodeInterfacePair.of("node", "iface")));
    assertThat(clonedStep.getDetail().getResolvedNexthopIp(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            ArpErrorStep.builder()
                .setAction(StepAction.NEIGHBOR_UNREACHABLE)
                .setDetail(
                    ArpErrorStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build(),
            ArpErrorStep.builder()
                .setAction(StepAction.NEIGHBOR_UNREACHABLE)
                .setDetail(
                    ArpErrorStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build())
        .addEqualityGroup(
            ArpErrorStep.builder()
                .setAction(StepAction.INSUFFICIENT_INFO)
                .setDetail(
                    ArpErrorStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build())
        .addEqualityGroup(
            ArpErrorStep.builder()
                .setAction(StepAction.INSUFFICIENT_INFO)
                .setDetail(
                    ArpErrorStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i1"))
                        .build())
                .build())
        .addEqualityGroup(
            ArpErrorStep.builder()
                .setAction(StepAction.INSUFFICIENT_INFO)
                .setDetail(
                    ArpErrorStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i1"))
                        .setResolvedNexthopIp(Ip.ZERO)
                        .build())
                .build())
        .testEquals();
  }
}
