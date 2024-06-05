package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.DeliveredStep.DeliveredStepDetail;
import org.junit.Test;

public class DeliveredStepTest {
  @Test
  public void testJsonSerialization() {
    DeliveredStep step =
        DeliveredStep.builder()
            .setAction(StepAction.DELIVERED_TO_SUBNET)
            .setDetail(
                DeliveredStepDetail.builder()
                    .setOutputInterface(NodeInterfacePair.of("node", "iface"))
                    .setResolvedNexthopIp(Ip.parse("1.1.1.1"))
                    .build())
            .build();

    DeliveredStep clonedStep = BatfishObjectMapper.clone(step, DeliveredStep.class);
    assertThat(clonedStep.getAction(), equalTo(StepAction.DELIVERED_TO_SUBNET));
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
            DeliveredStep.builder()
                .setAction(StepAction.DELIVERED_TO_SUBNET)
                .setDetail(
                    DeliveredStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build(),
            DeliveredStep.builder()
                .setAction(StepAction.DELIVERED_TO_SUBNET)
                .setDetail(
                    DeliveredStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build())
        .addEqualityGroup(
            DeliveredStep.builder()
                .setAction(StepAction.EXITS_NETWORK)
                .setDetail(
                    DeliveredStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i"))
                        .build())
                .build())
        .addEqualityGroup(
            DeliveredStep.builder()
                .setAction(StepAction.EXITS_NETWORK)
                .setDetail(
                    DeliveredStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i1"))
                        .build())
                .build())
        .addEqualityGroup(
            DeliveredStep.builder()
                .setAction(StepAction.EXITS_NETWORK)
                .setDetail(
                    DeliveredStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of("h", "i1"))
                        .setResolvedNexthopIp(Ip.ZERO)
                        .build())
                .build())
        .testEquals();
  }
}
