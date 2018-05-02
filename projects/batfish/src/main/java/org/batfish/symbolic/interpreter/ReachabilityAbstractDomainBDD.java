package org.batfish.symbolic.interpreter;

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
import org.batfish.symbolic.bdd.BDDRouteFactory;
import org.batfish.symbolic.bdd.BDDRouteFactory.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;

public class ReachabilityAbstractDomainBDD implements IAbstractDomain<BDD> {

  private static BDDFactory factory = BDDRouteFactory.factory;

  private static BDDPairing pairing = factory.makePair();

  private BDDRouteFactory _routeFactory;

  ReachabilityAbstractDomainBDD(BDDRouteFactory routeFactory) {
    _routeFactory = routeFactory;
  }

  private BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    long b = p.getStartIp().asLong();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.and(bits[i].not());
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
        acc = acc.or(equalLen);
      }
    }
    return acc.and(lowerBitsMatch);
  }

  @Override
  public BDD init(String router, Set<Prefix> prefixes) {
    BDD acc = factory.zero();
    for (Prefix prefix : prefixes) {
      SubRange r = new SubRange(32, 32);
      PrefixRange range = new PrefixRange(prefix, r);
      BDD pfx = isRelevantFor(_routeFactory.variables(), range);
      acc = acc.or(pfx);
    }
    return acc;
  }

  @Override
  public BDD transform(BDD input, EdgeTransformer t) {
    BDDTransferFunction f = t.getBgpTransfer();
    BDD passThrough = f.getFilter();
    BDD acc = input.and(passThrough);
    BDDRoute mods = f.getRoute();
    pairing.reset();
    if (mods.getConfig().getKeepCommunities()) {
      for (Entry<CommunityVar, BDD> e : _routeFactory.variables().getCommunities().entrySet()) {
        CommunityVar cvar = e.getKey();
        BDD x = e.getValue();
        BDD temp = _routeFactory.variables().getTemporary(x);
        BDD expr = mods.getCommunities().get(cvar);
        BDD equal = temp.biimp(expr);
        acc = acc.and(equal);
        pairing.set(x.var(), temp.var());
      }
    }
    // TODO for other fields
    acc = acc.replace(pairing);
    return acc;
  }

  @Override
  public BDD join(BDD x, BDD y) {
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
