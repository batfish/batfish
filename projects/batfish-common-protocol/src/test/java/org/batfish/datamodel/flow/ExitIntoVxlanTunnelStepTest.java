package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.ExitIntoVxlanTunnelStep.ExitIntoVxlanTunnelStepDetail;
import org.junit.Test;

/** Test of {@link ExitIntoVxlanTunnelStep}. */
public final class ExitIntoVxlanTunnelStepTest {

  @Test
  public void testJsonSerialization() {
    ExitIntoVxlanTunnelStep obj =
        ExitIntoVxlanTunnelStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(
                ExitIntoVxlanTunnelStepDetail.builder()
                    .setDstVtepIp(Ip.parse("10.0.0.2"))
                    .setOutputVrf("tenantA")
                    .setSrcVtepIp(Ip.parse("10.0.0.1"))
                    .setVni(5000)
                    .build())
            .build();

    assertThat(BatfishObjectMapper.clone(obj, Step.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    ExitIntoVxlanTunnelStepDetail.Builder dBuilder =
        ExitIntoVxlanTunnelStepDetail.builder()
            .setDstVtepIp(Ip.parse("10.0.0.2"))
            .setOutputVrf("tenantA")
            .setSrcVtepIp(Ip.parse("10.0.0.1"))
            .setVni(5000);
    ExitIntoVxlanTunnelStep.Builder builder =
        ExitIntoVxlanTunnelStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(dBuilder.build());
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAction(StepAction.TRANSMITTED).build())
        .addEqualityGroup(
            builder.setDetail(dBuilder.setDstVtepIp(Ip.parse("10.10.0.2")).build()).build())
        .addEqualityGroup(builder.setDetail(dBuilder.setOutputVrf("foo").build()).build())
        .addEqualityGroup(
            builder.setDetail(dBuilder.setSrcVtepIp(Ip.parse("10.10.0.1")).build()).build())
        .addEqualityGroup(builder.setDetail(dBuilder.setVni(42).build()).build())
        .testEquals();
  }
}
