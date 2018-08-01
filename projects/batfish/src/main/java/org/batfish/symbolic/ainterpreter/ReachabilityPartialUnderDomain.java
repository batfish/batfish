package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDTransferFunction;

public class ReachabilityPartialUnderDomain extends ReachabilityDomain {

  public ReachabilityPartialUnderDomain(Graph graph, BDDNetFactory netFactory) {
    super(graph, netFactory);
  }

  public ReachabilityPartialUnderDomain(Graph graph, BDDNetFactory netFactory, BDDNetwork network) {
    super(graph, netFactory, network);
  }

  @Override
  public RouteAclStateSetPair transform(RouteAclStateSetPair input, EdgeTransformer t) {
    BDDTransferFunction f = _domainHelper.lookupTransferFunction(_network, t);
    if (f != null) {
      BDD under = input.getRoutes();
      BDD allow = f.getFilter();
      BDD block = allow.not();
      BDD blockedInputs = under.and(block);
      BDD blockedPrefixes = blockedInputs.exist(_domainHelper.getAllQuantifyBits());
      BDD notBlockedPrefixes = blockedPrefixes.not();
      // Not sure why, but andWith does not work here (JavaBDD bug?)
      under = under.and(notBlockedPrefixes);
      under = _domainHelper.applyTransformerMods(under, f.getRoute(), t.getProtocol());
      BDD blocked = input.getAcls();
      BDDAcl acl = _domainHelper.lookupAcl(_network, t);
      if (acl != null) {
        BDD h = _domainHelper.headerspace(under);
        BDD toBlock = h.andWith(acl.getBdd().not());
        blocked = blocked.orWith(toBlock);
      }
      return new RouteAclStateSetPair(under, blocked);
    } else {
      return input;
    }
  }

  @Override
  protected BDD initialAcl() {
    return _netFactory.zero();
  }

  @Override
  protected BDD combineAcls(BDD x, BDD y) {
    return x.or(y);
  }

  @Override
  protected BDD notBlocked(BDD acl) {
    return acl.not();
  }
}
