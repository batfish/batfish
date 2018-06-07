package org.batfish.symbolic.ainterpreter;

import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory;

public class EnvironmentReachabilityDomain extends ReachabilityOverDomain {

  public EnvironmentReachabilityDomain(Graph graph, BDDNetFactory netFactory) {
    super(graph, netFactory);
  }

  @Override
  public RouteAclStateSetPair value(
      Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
    return new RouteAclStateSetPair(_netFactory.zero(), _netFactory.zero());
  }
}
