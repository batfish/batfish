package org.batfish.datamodel.flow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.OriginateStep.OriginateStepDetail;
import org.junit.Test;

public class OriginateStepTest {

  @Test
  public void testJsonSerialization() throws IOException {
    OriginateStep originateStep =
        OriginateStep.builder()
            .setAction(StepAction.ORIGINATED)
            .setDetail(
                OriginateStepDetail.builder()
                    .setOriginatingVrf("vrf1")
                    .setOriginatingInterface("iface1")
                    .build())
            .build();

    OriginateStep clonedStep = BatfishObjectMapper.clone(originateStep, OriginateStep.class);

    assertThat(clonedStep.getAction(), equalTo(StepAction.ORIGINATED));
    assertThat(clonedStep.getDetail().getOriginatingVrf(), equalTo("vrf1"));
    assertThat(clonedStep.getDetail().getOriginatingInterface(), equalTo("iface1"));
  }
}
