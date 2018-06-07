package org.batfish.symbolic.ainterpreter;

import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory;

public class ReachabilityUnderDomain
    extends DelayedDonutDomain<RouteAclStateSetPair, RouteAclStateSetPair> {

  public ReachabilityUnderDomain(Graph graph, BDDNetFactory netFactory) {
    super(
        new ReachabilityMixer(new DomainHelper(netFactory)),
        new ReachabilityOverDomain(graph, netFactory),
        new ReachabilityPartialUnderDomain(graph, netFactory));
  }
}
