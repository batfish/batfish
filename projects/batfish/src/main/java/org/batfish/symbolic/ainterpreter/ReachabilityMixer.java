package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;

public class ReachabilityMixer implements IDomainDifferencer<BDD, RouteAclStateSetPair> {

  private DomainHelper _domainHelper;

  public ReachabilityMixer(DomainHelper helper) {
    this._domainHelper = helper;
  }

  @Override
  public RouteAclStateSetPair difference(BDD x, RouteAclStateSetPair y) {
    return new RouteAclStateSetPair(_domainHelper.difference(x, y.getRoutes()), y.getBlockedAcls());
  }
}
