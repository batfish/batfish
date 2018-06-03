package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;
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
        ReachabilityUnderDomain under = new ReachabilityUnderDomain(graph, netFactory);
        ReachabilityOverDomain over = new ReachabilityOverDomain(graph, netFactory);
        IDomainDifferencer<BDD, RouteAclStateSetPair> mixer =
            new ReachabilityMixer(under.getDomainHelper());
        return new DelayedDonutDomain<>(mixer, over, under);
      default:
        throw new BatfishException("Invalid domain type: " + dtype);
    }
  }
}
