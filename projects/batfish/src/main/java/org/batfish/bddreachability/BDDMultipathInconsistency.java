package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.symbolic.IngressLocation;

/**
 * Detect multipath consistency violations. Given two {@link FlowDisposition dispositions}, find any
 * flows that can have either disposition.
 */
public class BDDMultipathInconsistency {
  private BDDMultipathInconsistency() {}

  /**
   * Return a list of {@link MultipathInconsistency multipath consistency violations} detected in
   * the network.
   */
  public static List<Flow> computeMultipathInconsistencies(
      BDDPacket bddPacket,
      Map<IngressLocation, BDD> disposition1FlowBdds,
      Map<IngressLocation, BDD> disposition2FlowBdds) {
    return computeMultipathInconsistencyBDDs(disposition1FlowBdds, disposition2FlowBdds)
        .map(violation -> multipathInconsistencyToFlow(bddPacket, violation))
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  static Stream<MultipathInconsistency> computeMultipathInconsistencyBDDs(
      Map<IngressLocation, BDD> disposition1FlowBdds,
      Map<IngressLocation, BDD> disposition2FlowBdds) {
    checkArgument(
        disposition1FlowBdds.keySet().equals(disposition2FlowBdds.keySet()),
        "Queries must have the same IngressLocations");

    return disposition1FlowBdds.entrySet().stream()
        .flatMap(
            entry -> {
              IngressLocation loc = entry.getKey();
              BDD query1Bdd = entry.getValue();
              BDD query2Bdd = disposition2FlowBdds.get(loc);

              BDD intersection = query1Bdd.and(query2Bdd);

              return intersection.isZero()
                  ? Stream.empty()
                  : Stream.of(new MultipathInconsistency(loc, intersection));
            });
  }

  @VisibleForTesting
  static Flow multipathInconsistencyToFlow(BDDPacket bddPacket, MultipathInconsistency violation) {
    Flow.Builder fb =
        bddPacket
            .getFlow(violation.getBDD())
            .orElseGet(
                () -> {
                  throw new BatfishException("MultipathConsistencyViolation with UNSAT predicate");
                });

    IngressLocation ingressLocation = violation.getIngressLocation();
    if (ingressLocation.isIngressVrf()) {
      fb.setIngressNode(ingressLocation.getNode());
      fb.setIngressVrf(ingressLocation.getVrf());
    } else {
      fb.setIngressNode(ingressLocation.getNode());
      fb.setIngressInterface(ingressLocation.getInterface());
    }
    return fb.build();
  }
}
