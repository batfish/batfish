package org.batfish.symbolic.ainterpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;

public class ReachabilityDomain implements IAbstractDomain<ReachabilityDomainElement> {

  private enum Transformation {
    UNDER_APPROXIMATION,
    OVER_APPROXIMATION
  }

  // Reference to the BDD network factory
  private BDDNetFactory _netFactory;

  // Reference to BDD Route variables
  private BDDRoute _variables;

  // Helper object to encapsulate common functionality
  private DomainHelper _domainHelper;

  ReachabilityDomain(BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _variables = _netFactory.routeVariables();
    _domainHelper = new DomainHelper(netFactory);
  }

  @Override
  public ReachabilityDomainElement bot() {
    BDD zero = _netFactory.zero();
    return new ReachabilityDomainElement(zero, zero, zero);
  }

  private BDD valueAux(String router, RoutingProtocol proto, Set<Prefix> prefixes) {
    BDD acc = _netFactory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        BDD pfx = BDDUtils.prefixToBdd(_netFactory.getFactory(), _variables, prefix);
        acc.orWith(pfx);
      }
    }
    BDD prot = _variables.getProtocolHistory().value(proto);
    BDD dst = _variables.getDstRouter().value(router);
    acc = acc.andWith(dst);
    acc = acc.andWith(prot);
    return acc;
  }

  @Override
  public ReachabilityDomainElement value(
      Configuration conf, RoutingProtocol proto, Set<Prefix> pfxs) {
    BDD ret = valueAux(conf.getName(), proto, pfxs);
    return new ReachabilityDomainElement(ret, ret, _netFactory.zero());
  }

  @Override
  public ReachabilityDomainElement transform(ReachabilityDomainElement input, EdgeTransformer t) {
    BDD under = transformAux(input.getUnderApproximation(), t, Transformation.UNDER_APPROXIMATION);
    BDD over = transformAux(input.getOverApproximation(), t, Transformation.OVER_APPROXIMATION);
    BDD blocked = input.getBlockedAcls();
    if (t.getBddAcl() != null) {
      BDD h = _domainHelper.headerspace(over);
      BDD toBlock = h.andWith(t.getBddAcl().getBdd().not());
      blocked = blocked.orWith(toBlock);
    }
    return new ReachabilityDomainElement(under, over, blocked);
  }

  private BDD transformAux(BDD input, EdgeTransformer t, Transformation type) {
    BDDTransferFunction f = t.getBddTransfer();
    // Filter routes that can not pass through the transformer
    BDD allow = f.getFilter();
    if (type == Transformation.OVER_APPROXIMATION) {
      input = input.and(allow);
    } else {
      BDD block = allow.not();
      BDD blockedInputs = input.and(block);
      BDD blockedPrefixes = blockedInputs.exist(_domainHelper.getAllQuantifyBits());
      BDD notBlockedPrefixes = blockedPrefixes.not();
      // Not sure why, but andWith does not work here (JavaBDD bug?)
      input = input.and(notBlockedPrefixes);
    }

    return _domainHelper.applyTransformerMods(input, t);
  }

  @Override
  public ReachabilityDomainElement merge(ReachabilityDomainElement x, ReachabilityDomainElement y) {
    BDD newFirst = x.getUnderApproximation().or(y.getUnderApproximation());
    BDD newSecond = x.getOverApproximation().or(y.getOverApproximation());
    BDD newAcls = x.getBlockedAcls().or(y.getBlockedAcls());
    return new ReachabilityDomainElement(newFirst, newSecond, newAcls);
  }

  @Override
  public ReachabilityDomainElement selectBest(ReachabilityDomainElement x) {
    return x;
  }

  @Override
  public ReachabilityDomainElement aggregate(
      Configuration conf, List<AggregateTransformer> aggregates, ReachabilityDomainElement x) {
    BDD under = aggregateAux(conf.getName(), x.getUnderApproximation(), aggregates);
    BDD over = aggregateAux(conf.getName(), x.getOverApproximation(), aggregates);
    return new ReachabilityDomainElement(under, over, x.getBlockedAcls());
  }

  private BDD aggregateAux(String router, BDD x, List<AggregateTransformer> aggregates) {
    BDD acc = x;
    for (AggregateTransformer t : aggregates) {
      BDD aggregated = x.and(t.getTransferFunction().getFilter());
      if (!aggregated.isZero()) {
        GeneratedRoute r = t.getGeneratedRoute();

        // Create a new aggregate route
        BDD prefix = BDDUtils.prefixToBdd(_netFactory.getFactory(), _variables, r.getNetwork());
        BDD proto = _netFactory.one();
        BDD ad = _netFactory.one();
        BDD lp = _netFactory.one();
        BDD dstRouter = _netFactory.one();
        if (_netFactory.getConfig().getKeepProtocol()) {
          proto = _variables.getProtocolHistory().value(RoutingProtocol.AGGREGATE);
        }
        if (_netFactory.getConfig().getKeepAd()) {
          ad = _variables.getAdminDist().value((long) r.getAdministrativeCost());
        }
        if (_netFactory.getConfig().getKeepLp()) {
          lp = _variables.getLocalPref().value(100);
        }
        if (_netFactory.getConfig().getKeepRouters()) {
          dstRouter = _variables.getDstRouter().value(router);
        }

        BDD route = dstRouter.and(ad).and(lp).and(prefix).and(proto);
        acc = acc.orWith(route);
      }
    }
    return acc;
  }

  // TODO: same thing, take away the ones that over approximated
  @Override
  public List<RibEntry> toRoutes(AbstractRib<ReachabilityDomainElement> value) {
    return toRoutesAux(value.getMainRib());
  }

  private List<RibEntry> toRoutesAux(ReachabilityDomainElement value) {
    List<RibEntry> ribEntries = new ArrayList<>();
    List<SatAssignment> entries = BDDUtils.allSat(_netFactory, value.getUnderApproximation(), true);
    for (SatAssignment entry : entries) {
      Prefix p = new Prefix(entry.getDstIp(), entry.getPrefixLen());
      RibEntry r =
          new RibEntry(
              entry.getSrcRouter(),
              p,
              entry.getRoutingProtocol(),
              entry.getMetric(),
              entry.getAdminDist(),
              entry.getNextHopIp().toString(),
              entry.getDstRouter());
      ribEntries.add(r);
    }
    return ribEntries;
  }

  // TODO: ensure unique reachability (i.e., no other destinations)
  @Override
  public BDD toFib(Map<String, AbstractRib<ReachabilityDomainElement>> ribs) {
    Map<String, AbstractFib<ReachabilityDomainElement>> ret = new HashMap<>();
    for (Entry<String, AbstractRib<ReachabilityDomainElement>> e : ribs.entrySet()) {
      String router = e.getKey();
      AbstractRib<ReachabilityDomainElement> rib = e.getValue();
      AbstractFib<ReachabilityDomainElement> fib = new AbstractFib<>(rib, toFibSingleRouter(rib));
      ret.put(router, fib);
    }
    return _domainHelper.transitiveClosure(ret);
  }

  private BDD toFibSingleRouter(AbstractRib<ReachabilityDomainElement> value) {
    ReachabilityDomainElement elt = value.getMainRib();
    BDD under = elt.getUnderApproximation();
    BDD over = elt.getOverApproximation();
    BDD reachablePackets = _domainHelper.toFib(under, over);
    reachablePackets = reachablePackets.andWith(elt.getBlockedAcls().not());
    return reachablePackets;
  }

  // TODO: cache the conversion from rib to fib?
  @Override
  public boolean reachable(
      Map<String, AbstractRib<ReachabilityDomainElement>> ribs, String src, String dst, Flow flow) {
    BDD f = BDDUtils.flowToBdd(_netFactory, flow);
    String current = src;
    while (true) {
      AbstractRib<ReachabilityDomainElement> rib = ribs.get(current);
      BDD fib = toFibSingleRouter(rib);
      BDD fibForFlow = fib.and(f);
      SatAssignment assignment = BDDUtils.satOne(_netFactory, fibForFlow);
      if (assignment == null) {
        return false;
      }
      current = assignment.getDstRouter();
      if (current.equals(dst)) {
        return true;
      }
    }
  }

  @Override
  public String debug(ReachabilityDomainElement value) {
    List<RibEntry> ribs = toRoutesAux(value);
    return ribs.toString();
  }
}
