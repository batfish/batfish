package org.batfish.symbolic.ainterpreter;

import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory;

public class ReachabilityUnderDomain
    extends DelayedDonutDomain<RouteAclStateSetPair, RouteAclStateSetPair> {

  private ReachabilityUnderDomain(
      IDomainDifferencer<RouteAclStateSetPair, RouteAclStateSetPair> mixer,
      IAbstractDomain<RouteAclStateSetPair> domain1,
      IAbstractDomain<RouteAclStateSetPair> domain2) {
    super(mixer, domain1, domain2);
  }

  public static ReachabilityUnderDomain create(Graph graph, BDDNetFactory netFactory) {
    ReachabilityOverDomain overDomain = new ReachabilityOverDomain(graph, netFactory);
    ReachabilityPartialUnderDomain underDomain =
        new ReachabilityPartialUnderDomain(graph, netFactory, overDomain.getNetwork());
    ReachabilityMixer mixer = new ReachabilityMixer(overDomain.getDomainHelper());
    return new ReachabilityUnderDomain(mixer, overDomain, underDomain);
  }
}
