package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.ip.Ip;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.DeliveredStep.DeliveredStepDetail;
import org.junit.Test;

public class DeliveredStepTest {
  @Test
  public void testJsonSerialization() throws IOException {
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
}
