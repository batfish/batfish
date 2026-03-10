package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Test;

/** Test for {@link RoutingStepDetail} */
public class RoutingStepDetailTest {

  @Test
  public void testJsonSerialization() {
    RoutingStepDetail obj =
        RoutingStepDetail.builder()
            .setForwardingDetail(Discarded.instance())
            .setVrf("v")
            .setRoutes(
                ImmutableList.of(
                    new RouteInfo(
                        RoutingProtocol.BGP, Prefix.ZERO, NextHopDiscard.instance(), 5, 6L)))
            .setOutputInterface("iface")
            .setArpIp(Ip.parse("2.3.4.5"))
            .build();
    assertThat(BatfishObjectMapper.clone(obj, RoutingStepDetail.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    RoutingStepDetail.Builder builder =
        RoutingStepDetail.builder()
            .setForwardingDetail(Discarded.instance())
            .setVrf("v")
            .setRoutes(
                ImmutableList.of(
                    new RouteInfo(
                        RoutingProtocol.BGP, Prefix.ZERO, NextHopDiscard.instance(), 5, 6L)))
            .setOutputInterface("iface")
            .setArpIp(Ip.parse("2.3.4.5"));
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setForwardingDetail(DelegatedToNextVrf.of("a")).build())
        .addEqualityGroup(builder.setVrf("b").build())
        .addEqualityGroup(builder.setRoutes(ImmutableList.of()).build())
        .addEqualityGroup(builder.setOutputInterface("bar").build())
        .addEqualityGroup(builder.setArpIp(Ip.parse("1.1.1.1")).build())
        .testEquals();
  }
}
