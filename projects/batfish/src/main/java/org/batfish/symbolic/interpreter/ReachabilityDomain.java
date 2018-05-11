package org.batfish.symbolic.interpreter;

import java.util.Arrays;
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
import org.batfish.symbolic.bdd.BDDFiniteDomain;
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

    _projectVariables.addAll(Arrays.asList(_routeFactory.variables()
        .getProtocolHistory()
        .getInteger()
        .getBitvec()));
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
  public BDD init() {
    return factory.zero();
  }

  @Override
  public BDD init(String router, Protocol proto, Set<Prefix> prefixes) {
    BDD acc = factory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        SubRange r = new SubRange(prefix.getPrefixLength(), prefix.getPrefixLength());
        PrefixRange range = new PrefixRange(prefix, r);
        BDD pfx = isRelevantFor(_routeFactory.variables(), range);
        acc = acc.or(pfx);
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
      BDD blockedInputs = input.and(block);
      BDD blockedPrefixes = project(blockedInputs);
      acc = input.and(blockedPrefixes.not());
    }

    // Modify the result
    BDDRoute mods = f.getRoute();
    pairing.reset();
    if (mods.getConfig().getKeepCommunities()) {
      for (Entry<CommunityVar, BDD> e : _routeFactory.variables().getCommunities().entrySet()) {
        CommunityVar cvar = e.getKey();
        BDD x = e.getValue();
        BDD temp = _routeFactory.variables().getCommunitiesTemp().get(cvar);
        BDD expr = mods.getCommunities().get(cvar);
        BDD equal = temp.biimp(expr);
        acc = acc.and(equal);
        pairing.set(temp.var(), x.var());
      }
    }

    if (mods.getConfig().getKeepHistory()) {
      BDDFiniteDomain<Protocol> prot =
          new BDDFiniteDomain<>(_routeFactory.variables().getProtocolHistory());
      prot.setValue(Protocol.BGP);

      BDD[] vec = _routeFactory.variables().getProtocolHistory().getInteger().getBitvec();
      for (int i = 0; i < vec.length; i++) {
        BDD x = vec[i];
        BDD temp = _routeFactory.variables().getProtocolHistoryTemp().getInteger().getBitvec()[i];
        BDD expr = prot.getInteger().getBitvec()[i];
        BDD equal = temp.biimp(expr);
        acc = acc.and(equal);
        pairing.set(temp.var(), x.var());
      }
    }

    acc = project(acc);
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
