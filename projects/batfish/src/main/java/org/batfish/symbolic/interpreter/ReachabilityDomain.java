package org.batfish.symbolic.interpreter;

import java.util.Map.Entry;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDFiniteDomain;
import org.batfish.symbolic.bdd.BDDRouteFactory;
import org.batfish.symbolic.bdd.BDDRouteFactory.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;

public class ReachabilityDomain implements IAbstractDomain<BDD> {

  private static BDDFactory factory = BDDRouteFactory.factory;

  private static BDDPairing pairing = factory.makePair();

  private BDDRoute _variables;

  private BDD _projectVariables;

  ReachabilityDomain(BDDRoute variables, BDD projectVariables) {
    _variables = variables;
    _projectVariables = projectVariables;
  }

  @Override
  public BDD bot() {
    return factory.zero();
  }

  @Override
  public BDD value(String router, Protocol proto, Set<Prefix> prefixes) {
    BDD acc = factory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        BDD pfx = BDDUtils.prefixToBdd(factory, _variables, prefix);
        acc.orWith(pfx);
      }
    }
    BDD prot = _variables.getProtocolHistory().value(proto);
    BDD dst = _variables.getDstRouter().value(router);
    acc.andWith(dst);
    acc.andWith(prot);
    return acc;
  }

  @Override
  public BDD transform(BDD input, EdgeTransformer t) {
    BDDTransferFunction f = t.getBgpTransfer();
    // Filter routes that can not pass through the transformer
    BDD allow = f.getFilter();
    BDD block = allow.not();
    BDD blockedInputs = input.and(block);
    BDD blockedPrefixes = blockedInputs.exist(_projectVariables);
    BDD notBlockedPrefixes = blockedPrefixes.not();
    // Not sure why, but andWith does not work here (JavaBDD bug?)
    input = input.and(notBlockedPrefixes);

    // Modify the result
    BDDRoute mods = f.getRoute();
    pairing.reset();
    if (mods.getConfig().getKeepCommunities()) {
      for (Entry<CommunityVar, BDD> e : _variables.getCommunities().entrySet()) {
        CommunityVar cvar = e.getKey();
        BDD x = e.getValue();
        BDD temp = _variables.getCommunitiesTemp().get(cvar);
        BDD expr = mods.getCommunities().get(cvar);
        BDD equal = temp.biimp(expr);
        input.andWith(equal);
        pairing.set(temp.var(), x.var());
      }
    }

    if (mods.getConfig().getKeepHistory()) {
      BDDFiniteDomain<Protocol> var = _variables.getProtocolHistory();
      BDDFiniteDomain<Protocol> prot = new BDDFiniteDomain<>(var);
      prot.setValue(Protocol.BGP);
      BDD[] vec = _variables.getProtocolHistory().getInteger().getBitvec();
      for (int i = 0; i < vec.length; i++) {
        BDD x = vec[i];
        BDD temp = _variables.getProtocolHistoryTemp().getInteger().getBitvec()[i];
        BDD expr = prot.getInteger().getBitvec()[i];
        BDD equal = temp.biimp(expr);
        input.andWith(equal);
        pairing.set(temp.var(), x.var());
      }
    }

    input = input.exist(_projectVariables);
    input.replaceWith(pairing);
    return input;
  }

  @Override
  public BDD merge(BDD x, BDD y) {
    return x.or(y);
  }

  @Override
  public BDD toBdd(BDD value) {
    return value;
  }

  public BDDRoute getVariables() {
    return _variables;
  }
}
