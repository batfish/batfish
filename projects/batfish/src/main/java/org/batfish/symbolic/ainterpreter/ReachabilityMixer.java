package org.batfish.symbolic.ainterpreter;

public class ReachabilityMixer
    implements IDomainDifferencer<RouteAclStateSetPair, RouteAclStateSetPair> {

  private DomainHelper _domainHelper;

  public ReachabilityMixer(DomainHelper helper) {
    this._domainHelper = helper;
  }

  @Override
  public RouteAclStateSetPair difference(RouteAclStateSetPair x, RouteAclStateSetPair y) {
    return new RouteAclStateSetPair(
        _domainHelper.difference(x.getRoutes(), y.getRoutes()), y.getAcls());
  }
}
