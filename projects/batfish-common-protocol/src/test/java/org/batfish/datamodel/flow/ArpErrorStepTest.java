package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.ip.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ArpErrorStep.ArpErrorStepDetail;
import org.junit.Test;

public class ArpErrorStepTest {

  @Test
  public void testJsonSerialization() throws IOException {
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
}
