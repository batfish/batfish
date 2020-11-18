package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.pojo.Node;

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
  public static Hop forwardedHop(String node) {
    return new Hop(
        new Node(node),
        ImmutableList.of(
            enterInputIfaceStep(node),
            RoutingStep.builder()
                .setAction(StepAction.FORWARDED)
                .setDetail(RoutingStepDetail.builder().build())
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
                .setDetail(RoutingStepDetail.builder().build())
                .build()));
  }
}
