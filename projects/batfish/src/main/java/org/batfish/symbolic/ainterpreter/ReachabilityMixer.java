package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;

public class ReachabilityMixer implements IDomainMixable<BDD, RouteAclStateSetPair> {

  private DomainHelper _domainHelper;

  public ReachabilityMixer(DomainHelper helper) {
    this._domainHelper = helper;
  }

  @Override
  public AbstractRib<RouteAclStateSetPair> difference(
      AbstractRib<BDD> x, AbstractRib<RouteAclStateSetPair> y) {
    BDD routes = _domainHelper.difference(x.getMainRib(), y.getMainRib().getRoutes());
    return new AbstractRib<>(
        y.getBgpRib(),
        y.getOspfRib(),
        y.getStaticRib(),
        y.getConnectedRib(),
        new RouteAclStateSetPair(routes, y.getMainRib().getBlockedAcls()));
  }
}
