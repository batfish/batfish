package org.batfish.symbolic.ainterpreter;

import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
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
  public BDD value(Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
    return _netFactory.zero();
  }
}
