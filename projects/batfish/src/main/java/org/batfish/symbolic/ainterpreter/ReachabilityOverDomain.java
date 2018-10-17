package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDTransferFunction;

public class ReachabilityOverDomain extends ReachabilityDomain {

  public ReachabilityOverDomain(Graph graph, BDDNetFactory netFactory) {
    super(graph, netFactory);
  }

  public ReachabilityOverDomain(Graph graph, BDDNetFactory netFactory, BDDNetwork network) {
    super(graph, netFactory, network);
  }

  @Override
  public RouteAclStateSetPair transform(RouteAclStateSetPair input, EdgeTransformer t) {
    BDDTransferFunction f = _domainHelper.lookupTransferFunction(_network, t);
    if (f != null) {
      BDD allow = f.getFilter();
      BDD over = input.getRoutes().and(allow);
      over = _domainHelper.applyTransformerMods(over, f.getRoute(), t.getProtocol());
      BDD allowed = input.getAcls();
      BDDAcl acl = _domainHelper.lookupAcl(_network, t);
      if (acl != null) {
        BDD h = _domainHelper.headerspace(over);
        BDD toAllow = h.andWith(acl.getBdd());
        allowed = allowed.andWith(toAllow);
      }
      return new RouteAclStateSetPair(over, allowed);
    } else {
      return input;
    }
  }

  @Override
  protected BDD initialAcl() {
    return _netFactory.one();
  }

  @Override
  protected BDD combineAcls(BDD x, BDD y) {
    return x.and(y);
  }

  @Override
  protected BDD notBlocked(BDD acl) {
    return acl;
  }
}
