package org.batfish.symbolic.ainterpreter;

import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory;

public class ReachabilityDonutDomain
    extends DelayedDonutDomain<RouteAclStateSetPair, RouteAclStateSetPair> {

  private ReachabilityDonutDomain(
      IDomainDifferencer<RouteAclStateSetPair, RouteAclStateSetPair> mixer,
      IAbstractDomain<RouteAclStateSetPair> domain1,
      IAbstractDomain<RouteAclStateSetPair> domain2) {
    super(mixer, domain1, domain2);
  }

  public static ReachabilityDonutDomain create(Graph graph, BDDNetFactory netFactory) {
    ReachabilityOverDomain overDomain = new ReachabilityOverDomain(graph, netFactory);
    ReachabilityUnderDomain underDomain =
        new ReachabilityUnderDomain(graph, netFactory, overDomain.getNetwork());
    ReachabilityMixer mixer = new ReachabilityMixer(overDomain.getDomainHelper());
    return new ReachabilityDonutDomain(mixer, overDomain, underDomain);
  }
}
