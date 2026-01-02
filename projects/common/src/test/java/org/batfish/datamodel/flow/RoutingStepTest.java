package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.junit.Test;

/** Test for {@link RoutingStep} */
public class RoutingStepTest {

  @Test
  public void testJsonSerialization() {
    RoutingStep obj =
        RoutingStep.builder()
            .setAction(StepAction.FORWARDED_TO_NEXT_VRF)
            .setDetail(
                RoutingStepDetail.builder()
                    .setForwardingDetail(Discarded.instance())
                    .setVrf("v")
                    .setRoutes(ImmutableList.of())
                    .setOutputInterface("iface")
                    .setArpIp(Ip.parse("2.3.4.5"))
                    .build())
            .build();
    assertThat(
        BatfishObjectMapper.clone(obj, new TypeReference<Step<RoutingStepDetail>>() {}),
        equalTo(obj));
  }

  @Test
  public void testEquals() {
    RoutingStepDetail.Builder detailBuilder =
        RoutingStepDetail.builder()
            .setForwardingDetail(Discarded.instance())
            .setVrf("v")
            .setRoutes(ImmutableList.of())
            .setOutputInterface("iface")
            .setArpIp(Ip.parse("2.3.4.5"));
    RoutingStep.Builder builder =
        RoutingStep.builder()
            .setAction(StepAction.FORWARDED_TO_NEXT_VRF)
            .setDetail(detailBuilder.build());
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAction(StepAction.FORWARDED).build())
        .addEqualityGroup(builder.setDetail(detailBuilder.setVrf("v2").build()).build())
        .testEquals();
  }
}
