package org.batfish.symbolic.ainterpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.utils.Tuple;

public class ReachabilityDomain implements IAbstractDomain<RouteAclStateSetPair> {

  // Reference to the BDD network factory
  protected BDDNetFactory _netFactory;

  // Reference to BDD Route variables
  protected BDDRoute _variables;

  // Helper object to encapsulate common functionality
  protected DomainHelper _domainHelper;

  protected BDDNetwork _network;

  public ReachabilityDomain(Graph graph, BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _variables = _netFactory.routeVariables();
    _domainHelper = new DomainHelper(netFactory);
    _network = BDDNetwork.create(graph, netFactory, false);
  }

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

  protected BDD initialAcl() {
    throw new BatfishException("unimplemented");
  }

  protected BDD combineAcls(BDD x, BDD y) {
    throw new BatfishException("unimplemented");
  }

  protected BDD notBlocked(BDD acl) {
    throw new BatfishException("unimplemented");
  }

  @Override
  public RouteAclStateSetPair bot() {
    BDD zero = _netFactory.zero();
    return new RouteAclStateSetPair(zero, initialAcl());
  }

  @Override
  public RouteAclStateSetPair value(
      Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
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
    return new RouteAclStateSetPair(acc, _netFactory.zero());
  }

  @Override
  public RouteAclStateSetPair transform(RouteAclStateSetPair input, EdgeTransformer t) {
    throw new BatfishException("unimplemented");
  }

  @Override
  public RouteAclStateSetPair merge(RouteAclStateSetPair x, RouteAclStateSetPair y) {
    BDD routes = x.getRoutes().or(y.getRoutes());
    BDD acls = combineAcls(x.getAcls(), y.getAcls());
    return new RouteAclStateSetPair(routes, acls);
  }

  @Override
  public RouteAclStateSetPair selectBest(RouteAclStateSetPair x) {
    return x;
  }

  @Override
  public RouteAclStateSetPair aggregate(
      Configuration conf, List<AggregateTransformer> aggregates, RouteAclStateSetPair x) {
    return new RouteAclStateSetPair(
        _domainHelper.aggregates(_network, conf.getName(), x.getRoutes(), aggregates), x.getAcls());
  }

  @Override
  public List<Route> toRoutes(RouteAclStateSetPair value) {
    return _domainHelper.toRoutes(value.getRoutes());
  }

  // TODO: ensure unique reachability (i.e., no other destinations)
  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, RouteAclStateSetPair> ribs) {
    Map<String, BDD> ret = new HashMap<>();
    for (Entry<String, RouteAclStateSetPair> e : ribs.entrySet()) {
      String router = e.getKey();
      RouteAclStateSetPair rib = e.getValue();
      BDD fib = toFibSingleRouter(rib);
      ret.put(router, fib);
    }
    return new Tuple<>(_netFactory, _domainHelper.transitiveClosure(ret));
  }

  private BDD toFibSingleRouter(RouteAclStateSetPair value) {
    BDD reachablePackets = value.getRoutes();
    BDD notAcls = notBlocked(value.getAcls());
    reachablePackets = reachablePackets.and(notAcls);
    return reachablePackets;
  }

  @Override
  @Nullable
  public String nextHop(RouteAclStateSetPair rib, String node, Flow flow) {
    BDD f = BDDUtils.flowToBdd(_netFactory, flow);
    BDD fib = toFibSingleRouter(rib);
    BDD fibForFlow = fib.and(f);
    SatAssignment assignment = BDDUtils.satOne(_netFactory, fibForFlow);
    if (assignment == null) {
      return null;
    }
    return assignment.getDstRouter();
  }

  @Override
  public String debug(RouteAclStateSetPair x) {
    List<Route> ribs = _domainHelper.toRoutes(x.getRoutes());
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (Route route : ribs) {
      sb.append("{")
          .append(route.getNetwork())
          .append(",")
          .append(route.getProtocol().protocolName())
          .append(",")
          .append(route.getMetric())
          .append(",")
          .append(route.getAdministrativeCost())
          .append(",")
          .append(route.getNode())
          .append(",")
          .append(route.getNextHopIp())
          .append("},");
    }
    sb.append("]");
    return sb.toString();
  }
}
