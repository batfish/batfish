package org.batfish.symbolic.interpreter;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.SubRange;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDRouteFactory;
import org.batfish.symbolic.bdd.BDDRouteFactory.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;

public class ReachabilityDomain implements IAbstractDomain<BDD> {

  private static BDDFactory factory = BDDRouteFactory.factory;

  private static BDDPairing pairing = factory.makePair();

  private BDDRouteFactory _routeFactory;

  private Set<BDD> _projectVariables;

  ReachabilityDomain(BDDRouteFactory routeFactory) {
    _routeFactory = routeFactory;
    _projectVariables = new HashSet<>();
    for (Entry<CommunityVar, BDD> e : _routeFactory.variables().getCommunities().entrySet()) {
      BDD c = e.getValue();
      _projectVariables.add(c);
    }
  }

  private BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    long b = p.getStartIp().asLong();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.andWith(bits[i].not());
      }
    }
    return acc;
  }

  /*
   * Check if a prefix range match is applicable for the packet destination
   * Ip address, given the prefix length variable.
   *
   * Since aggregation is modelled separately, we assume that prefixLen
   * is not modified, and thus will contain only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD isRelevantFor(BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    SubRange r = range.getLengthRange();
    int len = p.getPrefixLength();
    int lower = r.getStart();
    int upper = r.getEnd();

    BDD lowerBitsMatch = firstBitsEqual(record.getPrefix().getBitvec(), p, len);
    BDD acc = factory.zero();
    if (lower == 0 && upper == 32) {
      acc = factory.one();
    } else {
      for (int i = lower; i <= upper; i++) {
        BDD equalLen = record.getPrefixLength().value(i);
        acc = acc.orWith(equalLen);
      }
    }
    return acc.andWith(lowerBitsMatch);
  }

  @Override
  public BDD init(String router, Protocol proto, Set<Prefix> prefixes) {
    BDD acc = factory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        SubRange r = new SubRange(32, 32);
        PrefixRange range = new PrefixRange(prefix, r);
        BDD pfx = isRelevantFor(_routeFactory.variables(), range);
        acc = acc.orWith(pfx);
      }
    }
    BDD prot = _routeFactory.variables().getProtocolHistory().value(proto);
    BDD dst = _routeFactory.variables().getDstRouter().value(router);
    return acc.and(dst).and(prot);
  }

  private BDD project(BDD val) {
    for (BDD var : _projectVariables) {
      val = val.exist(var);
    }
    return val;
  }

  @Override
  public BDD transform(BDD input, EdgeTransformer t) {
    BDDTransferFunction f = t.getBgpTransfer();
    // Filter routes that can not pass through the transformer
    BDD acc;
    BDD allow = f.getFilter();
    if (_projectVariables.isEmpty()) {
      acc = input.and(allow);
    } else {
      BDD block = allow.not();
      BDD blockedInputs = input.andWith(block);
      BDD blockedInputsWithoutCommunities = project(blockedInputs);
      acc = input.andWith(blockedInputsWithoutCommunities.not());
    }
    // Modify the result
    BDDRoute mods = f.getRoute();
    pairing.reset();
    if (mods.getConfig().getKeepCommunities()) {
      for (Entry<CommunityVar, BDD> e : _routeFactory.variables().getCommunities().entrySet()) {
        CommunityVar cvar = e.getKey();
        BDD x = e.getValue();
        BDD temp = _routeFactory.variables().getTemporary(x);
        BDD expr = mods.getCommunities().get(cvar);
        BDD equal = temp.biimpWith(expr);
        acc = acc.andWith(equal);
        pairing.set(x.var(), temp.var());
      }
    }

    acc = acc.replace(pairing);
    return acc;
  }

  @Override
  public BDD merge(BDD x, BDD y) {
    return x.or(y);
  }

  @Override
  public BDD finalize(BDD value) {
    return value;
  }

  public BDDRoute getVariables() {
    return _routeFactory.variables();
  }
}
