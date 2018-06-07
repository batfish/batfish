package org.batfish.symbolic.ainterpreter;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.ainterpreter.DomainType;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetConfig;
import org.batfish.symbolic.bdd.BDDNetFactory;

public class AbstractDomainFactory {

  public static IAbstractDomain<?> createDomain(Graph graph, DomainType dtype) {
    BDDNetConfig config;
    BDDNetFactory netFactory;
    switch (dtype) {
      case EXACT:
        config = new BDDNetConfig(false);
        netFactory = new BDDNetFactory(graph, config);
        return new ConcreteDomain(graph, netFactory);
      case REACHABILITY:
        config = new BDDNetConfig(true);
        netFactory = new BDDNetFactory(graph, config);
        return new ReachabilityUnderDomain(graph, netFactory);
      default:
        throw new BatfishException("Invalid domain type: " + dtype);
    }
  }
}
