package org.batfish.symbolic.interpreter;

import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDRouteConfig;

public class ReachabilityInterpreter {

  private IBatfish _batfish;

  private HeaderLocationQuestion _question;

  private BDDRoute _variables;

  private BDDNetwork _network;

  public ReachabilityInterpreter(IBatfish batfish, HeaderLocationQuestion q) {
    _batfish = batfish;
    _question = q;
  }

  public AnswerElement computeStuff() {
    Graph g = new Graph(_batfish);
    BDDRouteConfig config = new BDDRouteConfig(true);
    NodesSpecifier ns = new NodesSpecifier(_question.getIngressNodeRegex());

    _variables = new BDDRoute(config, g.getAllCommunities());
    _network = BDDNetwork.create(g, ns, config);

    return new StringAnswerElement("Foo");
  }

}
