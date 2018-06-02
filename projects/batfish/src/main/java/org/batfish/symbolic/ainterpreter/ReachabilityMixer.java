package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;

public class ReachabilityMixer implements IDomainMixable<BDD, RouteAclStateSetPair> {

  private DomainHelper _domainHelper;

  public ReachabilityMixer(DomainHelper helper) {
    this._domainHelper = helper;
  }

  @Override
  public BDD fibDifference(BDD x, BDD y) {
    return _domainHelper.toFib(x, y);
  }

  // TODO: implement me
  @Override
  public AbstractRib<RouteAclStateSetPair> ribDifference(
      AbstractRib<BDD> x, AbstractRib<RouteAclStateSetPair> y) {
    return y;
  }
}
