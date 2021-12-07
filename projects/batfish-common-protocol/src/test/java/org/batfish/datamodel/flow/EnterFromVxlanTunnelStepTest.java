package org.batfish.datamodel.flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.EnterFromVxlanTunnelStep.EnterFromVxlanTunnelStepDetail;
import org.junit.Test;

/** Test of {@link EnterFromVxlanTunnelStep}. */
public final class EnterFromVxlanTunnelStepTest {

  @Test
  public void testJsonSerialization() {
    EnterFromVxlanTunnelStep obj =
        EnterFromVxlanTunnelStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(
                EnterFromVxlanTunnelStepDetail.builder()
                    .setDstVtepIp(Ip.parse("10.0.0.2"))
                    .setInputVrf("tenantA")
                    .setSrcVtepIp(Ip.parse("10.0.0.1"))
                    .setVni(5000)
                    .build())
            .build();

    assertThat(BatfishObjectMapper.clone(obj, Step.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    EnterFromVxlanTunnelStepDetail.Builder dBuilder =
        EnterFromVxlanTunnelStepDetail.builder()
            .setDstVtepIp(Ip.parse("10.0.0.2"))
            .setInputVrf("tenantA")
            .setSrcVtepIp(Ip.parse("10.0.0.1"))
            .setVni(5000);
    EnterFromVxlanTunnelStep.Builder builder =
        EnterFromVxlanTunnelStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(dBuilder.build());
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAction(StepAction.TRANSMITTED).build())
        .addEqualityGroup(
            builder.setDetail(dBuilder.setDstVtepIp(Ip.parse("10.10.0.2")).build()).build())
        .addEqualityGroup(builder.setDetail(dBuilder.setInputVrf("foo").build()).build())
        .addEqualityGroup(
            builder.setDetail(dBuilder.setSrcVtepIp(Ip.parse("10.10.0.1")).build()).build())
        .addEqualityGroup(builder.setDetail(dBuilder.setVni(42).build()).build())
        .testEquals();
  }
}
