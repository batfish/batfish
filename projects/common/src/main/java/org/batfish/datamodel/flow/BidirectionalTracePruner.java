package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.batfish.common.util.Pruner;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.pojo.Node;

/**
 * A {@link Pruner} for {@link BidirectionalTrace}. Choose traces to cover the range of values of
 * each of (in order): the forward flow, the reverse flow, the forward disposition, the reverse
 * disposition, the nodes traversed in the forward direction, and the nodes traversed in the reverse
 * direction. Note that the flows include the start location, so the hops are mainly providing
 * variability of intermediate hops.
 */
public final class BidirectionalTracePruner {
  private BidirectionalTracePruner() {}

  private static final Pruner<BidirectionalTrace> INSTANCE =
      Pruner.<BidirectionalTrace>builder()
          .addProperty(BidirectionalTrace::getForwardFlow)
          .addProperty(BidirectionalTrace::getReverseFlow)
          .addProperty(BidirectionalTracePruner::forwardDisposition)
          .addProperty(BidirectionalTracePruner::reverseDisposition)
          .addProperty(BidirectionalTracePruner::forwardHops)
          .addProperty(BidirectionalTracePruner::reverseHops)
          .build();

  public static List<BidirectionalTrace> prune(List<BidirectionalTrace> objects, int maxSize) {
    return INSTANCE.prune(objects, maxSize);
  }

  private static FlowDisposition forwardDisposition(BidirectionalTrace trace) {
    return trace.getForwardTrace().getDisposition();
  }

  private static Optional<FlowDisposition> reverseDisposition(BidirectionalTrace trace) {
    return Optional.ofNullable(trace.getReverseTrace()).map(Trace::getDisposition);
  }

  private static List<String> forwardHops(BidirectionalTrace trace) {
    return hops(trace.getForwardTrace());
  }

  private static List<String> reverseHops(BidirectionalTrace trace) {
    return Optional.ofNullable(trace.getReverseTrace())
        .map(BidirectionalTracePruner::hops)
        .orElse(ImmutableList.of());
  }

  private static List<String> hops(Trace trace) {
    return trace.getHops().stream()
        .map(Hop::getNode)
        .map(Node::getName)
        .collect(ImmutableList.toImmutableList());
  }
}
