package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.junit.Test;

/** Test for {@link RoutingStep} */
public class RoutingStepTest {

  @Test
  public void testJsonSerialization() throws IOException {
    RoutingStepDetail routingStepDetail =
        RoutingStepDetail.builder()
            .setMatchedRoutes(
                ImmutableList.of(
                    new RouteInfo(RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null),
                    new RouteInfo(RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), null, null)))
            .setFinalNextHopInterface("iface")
            .build();

    RoutingStep routingStep =
        RoutingStep.builder()
            .setAction(StepAction.FORWARDED_TO_NEXT_VRF)
            .setDetail(routingStepDetail)
            .build();

    // don't throw
    BatfishObjectMapper.clone(routingStep, RoutingStep.class);
  }
}
