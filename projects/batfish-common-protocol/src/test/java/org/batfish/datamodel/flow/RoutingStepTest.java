package org.batfish.datamodel.flow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.junit.Test;

/** Test for {@link RoutingStep} */
public class RoutingStepTest {

  @Test
  public void testJsonSerialization() {
    RoutingStepDetail routingStepDetail =
        RoutingStepDetail.builder()
            .setVrf("v")
            .setRoutes(
                ImmutableList.of(
                    new RouteInfo(
                        RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null, 0, 1),
                    new RouteInfo(
                        RoutingProtocol.STATIC, Prefix.parse("2.1.1.3/30"), null, null, 1, 0)))
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

    assertThat(routingStepCloned.getDetail().getVrf(), equalTo("v"));
    assertThat(
        routingStepCloned.getDetail().getRoutes(),
        equalTo(
            ImmutableList.of(
                new RouteInfo(RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null, 0, 1),
                new RouteInfo(
                    RoutingProtocol.STATIC, Prefix.parse("2.1.1.3/30"), null, null, 1, 0))));
    assertThat(routingStepCloned.getDetail().getArpIp(), equalTo(Ip.parse("2.3.4.5")));
    assertThat(routingStepCloned.getDetail().getOutputInterface(), equalTo("iface"));
    assertThat(routingStep.getAction(), equalTo(StepAction.FORWARDED_TO_NEXT_VRF));
  }

  @Test
  public void testEquals() {
    RouteInfo info =
        new RouteInfo(RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null, 0, 1);
    RoutingStepDetail d1 =
        RoutingStepDetail.builder()
            .setVrf("v")
            .setRoutes(ImmutableList.of(info))
            .setOutputInterface("iface")
            .setArpIp(Ip.parse("2.3.4.5"))
            .build();
    RoutingStepDetail d2 =
        RoutingStepDetail.builder()
            .setVrf("v2")
            .setRoutes(ImmutableList.of(info))
            .setOutputInterface("iface")
            .setArpIp(Ip.parse("2.3.4.5"))
            .build();

    // Step equality
    new EqualsTester()
        .addEqualityGroup(
            RoutingStep.builder().setAction(StepAction.FORWARDED_TO_NEXT_VRF).setDetail(d1).build(),
            RoutingStep.builder().setAction(StepAction.FORWARDED_TO_NEXT_VRF).setDetail(d1).build())
        .addEqualityGroup(
            RoutingStep.builder().setAction(StepAction.NO_ROUTE).setDetail(d1).build())
        .addEqualityGroup(
            RoutingStep.builder().setAction(StepAction.NO_ROUTE).setDetail(d2).build())
        .testEquals();

    // Details equality
    new EqualsTester()
        .addEqualityGroup(
            RoutingStepDetail.builder()
                .setVrf("v")
                .setRoutes(ImmutableList.of())
                .setOutputInterface("iface1")
                .setArpIp(Ip.parse("2.3.4.5"))
                .build(),
            RoutingStepDetail.builder()
                .setVrf("v")
                .setRoutes(ImmutableList.of())
                .setOutputInterface("iface1")
                .setArpIp(Ip.parse("2.3.4.5"))
                .build())
        .addEqualityGroup(
            RoutingStepDetail.builder()
                .setVrf("v2")
                .setRoutes(ImmutableList.of())
                .setOutputInterface("iface1")
                .setArpIp(Ip.parse("2.3.4.5"))
                .build())
        .addEqualityGroup(
            RoutingStepDetail.builder()
                .setVrf("v2")
                .setRoutes(ImmutableList.of(info))
                .setOutputInterface("iface1")
                .setArpIp(Ip.parse("2.3.4.5"))
                .build())
        .addEqualityGroup(
            RoutingStepDetail.builder()
                .setVrf("v2")
                .setRoutes(ImmutableList.of(info))
                .setOutputInterface("iface2")
                .setArpIp(Ip.parse("2.3.4.5"))
                .build())
        .addEqualityGroup(
            RoutingStepDetail.builder()
                .setVrf("v2")
                .setRoutes(ImmutableList.of(info))
                .setOutputInterface("iface2")
                .setArpIp(Ip.parse("1.2.3.4"))
                .build())
        .testEquals();
  }
}
