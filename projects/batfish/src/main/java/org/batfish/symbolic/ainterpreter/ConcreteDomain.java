package org.batfish.symbolic.ainterpreter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.bdd.BDDFiniteDomain;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.utils.Tuple;

// TODO: this class should be responsible for setting the BDDNetConfig

public class ConcreteDomain implements IAbstractDomain<BDD> {

  private BDDNetFactory _netFactory;

  private BDDRoute _variables;

  private DomainHelper _domainHelper;

  private BDDNetwork _network;

  public ConcreteDomain(Graph graph, BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _variables = netFactory.routeVariables();
    _domainHelper = new DomainHelper(netFactory);
    _network = BDDNetwork.create(graph, netFactory, false);
  }

  @Override
  public BDD bot() {
    return _netFactory.zero();
  }

  @Override
  public BDD value(Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
    BDD acc = _netFactory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        BDD pfx = BDDUtils.prefixToBdd(_netFactory.getFactory(), _variables, prefix);
        acc = acc.orWith(pfx);
      }
    }
    int defaultAd = 0;
    if (proto != RoutingProtocol.LOCAL) {
      defaultAd = proto.getDefaultAdministrativeCost(conf.getConfigurationFormat());
    }
    BDD fromRRClient = _variables.getFromRRClient().not();
    BDD localPref = _variables.getLocalPref().value(100);
    BDD adminDist = _variables.getAdminDist().value((long) defaultAd);
    BDD metric = _variables.getMetric().value(1);
    BDD ospfMetric = _variables.getOspfMetric().value(OspfType.O);
    BDD med = _variables.getMed().value((long) 80);
    BDD prot = _variables.getProtocolHistory().value(proto);
    BDD nhip = _variables.getNextHopIp().value(new Ip(0));

    for (BDD c : _variables.getCommunities().values()) {
      acc = acc.andWith(c.not());
    }
    acc =
        acc.andWith(fromRRClient)
            .andWith(metric)
            .andWith(ospfMetric)
            .andWith(med)
            .andWith(localPref)
            .andWith(adminDist)
            .andWith(nhip)
            .andWith(prot);
    return acc;
  }

  @Override
  public BDD transform(BDD input, EdgeTransformer t) {
    BDDTransferFunction f = _domainHelper.lookupTransferFunction(_network, t);
    BDD ret = input;
    if (f != null) {
      ret = ret.and(f.getFilter());
      ret = ret.exist(_domainHelper.getTransientBits());
      ret = _domainHelper.applyTransformerMods(ret, f.getRoute(), t.getProtocol());
    }
    if (t.getCost() != null || t.getNextHopIp() != null) {
      BDDTransferFunction addedCost = implicitTransfer(t.getCost(), t.getNextHopIp());
      ret = _domainHelper.applyTransformerMods(ret, addedCost.getRoute(), t.getProtocol());
    }
    return ret;
  }

  /*
   * Creates a modification that increments the metric by 'cost'.
   */
  private BDDTransferFunction implicitTransfer(@Nullable Integer cost, @Nullable Ip nextHop) {
    BDDRoute route = _netFactory.routeVariables().deepCopy();
    if (cost != null) {
      // update metric
      BDDInteger met = route.getMetric();
      BDDInteger addedCost = new BDDInteger(met);
      addedCost.setValue(cost);
      route.setMetric(met.add(addedCost));
    }
    if (nextHop != null) {
      // update next hop
      BDDFiniteDomain<Ip> nh = route.getNextHopIp();
      BDDFiniteDomain<Ip> value = new BDDFiniteDomain<>(nh);
      value.setValue(nextHop);
      route.setNextHopIp(value);
    }
    return new BDDTransferFunction(route, _netFactory.one());
  }

  @Override
  public BDD merge(BDD x, BDD y) {
    return x.or(y);
  }

  @Override
  public BDD selectBest(BDD x) {
    List<SatAssignment> bestForSomePrefix =
        new ArrayList<>(new HashSet<>(BDDUtils.bestRoutes(_netFactory, x)));

    // Sort by preference
    bestForSomePrefix.sort(
        (o1, o2) -> {
          if (o1.getAdminDist() < o2.getAdminDist()) {
            return -1;
          } else if (o1.getAdminDist() > o2.getAdminDist()) {
            return 1;
          }
          if (o1.getLocalPref() < o2.getLocalPref()) {
            return -1;
          } else if (o1.getLocalPref() > o2.getLocalPref()) {
            return 1;
          }
          if (o1.getMetric() < o2.getMetric()) {
            return -1;
          } else if (o1.getMetric() > o2.getMetric()) {
            return 1;
          }
          if (o1.getMed() < o2.getMed()) {
            return -1;
          } else if (o1.getMed() > o2.getMed()) {
            return 1;
          }
          if (o1.getOspfMetric().ordinal() < o2.getOspfMetric().ordinal()) {
            return -1;
          } else if (o1.getOspfMetric().ordinal() > o2.getOspfMetric().ordinal()) {
            return 1;
          }
          return 0;
        });

    BDD acc = x;
    BDD best = _netFactory.zero();
    for (SatAssignment a : bestForSomePrefix) {
      BDD ad = _variables.getAdminDist().value((long) a.getAdminDist());
      BDD lp = _variables.getLocalPref().value(a.getLocalPref());
      BDD met = _variables.getMetric().value(a.getMetric());
      BDD med = _variables.getMed().value((long) a.getMed());
      BDD all = med.andWith(met).andWith(lp).andWith(ad);
      BDD withBest = acc.and(all);
      all.free();
      BDD prefixes = withBest.exist(_domainHelper.getAllQuantifyBits());
      best = best.orWith(withBest);
      acc = acc.andWith(prefixes.not());
    }
    return best;
  }

  @Override
  public BDD aggregate(Configuration conf, List<AggregateTransformer> aggregates, BDD x) {
    // BDDTransferFunction f = _network.getGeneratedRoutes().get(neighbor, policyName);
    return x;
  }

  @Override
  public List<Route> toRoutes(BDD value) {
    return _domainHelper.toRoutes(value);
  }

  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, BDD> ribs) {
    BDD allFibs = _netFactory.zero();
    for (Entry<String, BDD> e : ribs.entrySet()) {
      String router = e.getKey();
      BDD rib = e.getValue();
      BDD fib = _domainHelper.headerspace(rib);
      BDD src = _variables.getSrcRouter().value(router);
      BDD combined = fib.andWith(src);
      allFibs = allFibs.orWith(combined);
    }
    return new Tuple<>(_netFactory, allFibs);
  }

  @Override
  @Nullable
  public String nextHop(BDD ribs, String node, Flow flow) {
    return null;
  }

  @Override
  public String debug(BDD x) {
    List<Route> ribs = _domainHelper.toRoutes(x);
    return ribs.toString();
  }
}
