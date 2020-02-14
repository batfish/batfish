package org.batfish.datamodel.flow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.Prefix;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.junit.Test;

/** Test for {@link RoutingStep} */
public class RoutingStepTest {

  @Test
  public void testJsonSerialization() throws IOException {
    RoutingStepDetail routingStepDetail =
        RoutingStepDetail.builder()
            .setRoutes(
                ImmutableList.of(
                    new RouteInfo(RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null),
                    new RouteInfo(RoutingProtocol.STATIC, Prefix.parse("2.1.1.3/30"), null, null)))
            .setOutputInterface("iface")
            .setArpIp(Ip.parse("2.3.4.5"))
            .build();

    RoutingStep routingStep =
        RoutingStep.builder()
            .setAction(StepAction.FORWARDED_TO_NEXT_VRF)
            .setDetail(routingStepDetail)
            .build();

    // don't throw
    RoutingStep routingStepCloned = BatfishObjectMapper.clone(routingStep, RoutingStep.class);

    assertThat(
        routingStepCloned.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null),
                new RouteInfo(RoutingProtocol.STATIC, Prefix.parse("2.1.1.3/30"), null, null))));
    assertThat(routingStepCloned.getDetail().getArpIp(), equalTo(Ip.parse("2.3.4.5")));
    assertThat(routingStepCloned.getDetail().getOutputInterface(), equalTo("iface"));
    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));
  }
}
