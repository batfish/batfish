package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.route.nh.NextHopIp;

/** Utilities for building {@link Hop hops} and {@link Step steps} for testing. */
public final class HopTestUtils {
  private static EnterInputIfaceStep enterInputIfaceStep(String node) {
    return EnterInputIfaceStep.builder()
        .setAction(StepAction.RECEIVED)
        .setDetail(
            EnterInputIfaceStepDetail.builder()
                .setInputInterface(NodeInterfacePair.of(node, "inputIface"))
                .setInputVrf("inputVrf")
                .build())
        .build();
  }

  /** Create a Hop with StepAction Forwarded. */
  public static Hop acceptedHop(String node) {
    return new Hop(
        new Node(node),
        ImmutableList.of(
            enterInputIfaceStep(node),
            InboundStep.builder().setDetail(new InboundStepDetail("iface")).build()));
  }

  /** Create a Hop with StepAction Forwarded. */
  public static Hop forwardedHop(String node, String vrf) {
    return new Hop(
        new Node(node),
        ImmutableList.of(
            enterInputIfaceStep(node),
            RoutingStep.builder()
                .setAction(StepAction.FORWARDED)
                .setDetail(
                    RoutingStepDetail.builder()
                        .setRoutes(
                            ImmutableList.of(
                                new RouteInfo(
                                    RoutingProtocol.BGP,
                                    Prefix.parse("10.0.0.0/8"),
                                    NextHopIp.of(Ip.parse("192.0.2.1")),
                                    10,
                                    15)))
                        .setForwardingDetail(
                            ForwardedOutInterface.of("outIface", Ip.parse("192.0.2.1")))
                        .setVrf(vrf)
                        .build())
                .build(),
            ExitOutputIfaceStep.builder()
                .setAction(StepAction.TRANSMITTED)
                .setDetail(
                    ExitOutputIfaceStepDetail.builder()
                        .setOutputInterface(NodeInterfacePair.of(node, "outIface"))
                        .build())
                .build()));
  }

  public static Hop loopHop(String node) {
    return new Hop(new Node(node), ImmutableList.of(enterInputIfaceStep(node), LoopStep.INSTANCE));
  }

  public static Hop noRouteHop(String node) {
    return new Hop(
        new Node(node),
        ImmutableList.of(
            enterInputIfaceStep(node),
            RoutingStep.builder()
                .setAction(StepAction.NO_ROUTE)
                .setDetail(
                    RoutingStepDetail.builder()
                        .setForwardingDetail(Discarded.instance())
                        .setVrf("vrf")
                        .build())
                .build()));
  }
}
