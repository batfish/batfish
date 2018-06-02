package org.batfish.symbolic.ainterpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.utils.Tuple;

public class ReachabilityOverDomain implements IAbstractDomain<BDD> {

  // Reference to the BDD network factory
  protected BDDNetFactory _netFactory;

  // Reference to BDD Route variables
  protected BDDRoute _variables;

  // Helper object to encapsulate common functionality
  protected DomainHelper _domainHelper;

  protected BDDNetwork _network;

  public BDDNetFactory getNetFactory() {
    return _netFactory;
  }

  public BDDRoute getVariables() {
    return _variables;
  }

  public DomainHelper getDomainHelper() {
    return _domainHelper;
  }

  public BDDNetwork getNetwork() {
    return _network;
  }

  public ReachabilityOverDomain(Graph graph, BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _variables = _netFactory.routeVariables();
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
        acc.orWith(pfx);
      }
    }
    BDD prot = _variables.getProtocolHistory().value(proto);
    BDD dst = _variables.getDstRouter().value(conf.getName());
    acc = acc.andWith(dst);
    acc = acc.andWith(prot);
    return acc;
  }

  @Override
  public BDD transform(BDD input, EdgeTransformer t) {
    BDDTransferFunction f = _domainHelper.lookupTransferFunction(_network, t);
    if (f != null) {
      BDD allow = f.getFilter();
      input = input.and(allow);
      return _domainHelper.applyTransformerMods(input, f.getRoute(), t.getProtocol());
    } else {
      return input;
    }
  }

  @Override
  public BDD merge(BDD x, BDD y) {
    return x.or(y);
  }

  @Override
  public BDD selectBest(BDD x) {
    return x;
  }

  @Override
  public BDD aggregate(Configuration conf, List<AggregateTransformer> aggregates, BDD x) {
    return _domainHelper.aggregates(_network, conf.getName(), x, aggregates);
  }

  @Override
  public List<Route> toRoutes(AbstractRib<BDD> value) {
    return _domainHelper.toRoutes(value.getMainRib());
  }

  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, AbstractRib<BDD>> ribs) {
    Map<String, AbstractFib<BDD>> ret = new HashMap<>();
    for (Entry<String, AbstractRib<BDD>> e : ribs.entrySet()) {
      String router = e.getKey();
      AbstractRib<BDD> rib = e.getValue();
      AbstractFib<BDD> fib = new AbstractFib<>(rib, toFibSingleRouter(rib));
      ret.put(router, fib);
    }
    return new Tuple<>(_netFactory, _domainHelper.transitiveClosure(ret));
  }

  private BDD toFibSingleRouter(AbstractRib<BDD> value) {
    return _domainHelper.headerspace(value.getMainRib());
  }

  @Override
  public boolean reachable(Map<String, AbstractRib<BDD>> ribs, String src, String dst, Flow flow) {
    BDD f = BDDUtils.flowToBdd(_netFactory, flow);
    String current = src;
    while (true) {
      AbstractRib<BDD> rib = ribs.get(current);
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
  public String debug(BDD x) {
    List<Route> ribs = _domainHelper.toRoutes(x);
    return ribs.toString();
  }
}
