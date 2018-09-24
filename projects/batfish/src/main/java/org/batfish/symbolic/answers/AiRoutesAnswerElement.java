package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.answers.AnswerElement;

public class AiRoutesAnswerElement extends AnswerElement {

  private static final String PROP_ROUTES = "routes";

  private SortedSet<Route> _routes;

  @JsonProperty(PROP_ROUTES)
  public SortedSet<Route> getRoutes() {
    return _routes;
  }

  @JsonProperty(PROP_ROUTES)
  public void setRoutes(SortedSet<Route> x) {
    this._routes = x;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    for (Route route : _routes) {
      sb.append(route.getNode())
          .append(" vrf:")
          .append(route.getVrf())
          .append(" net:")
          .append(route.getNetwork())
          .append(" nhip:")
          .append(route.getNextHopIp())
          .append(" nhint:")
          .append(route.getNextHopInterface())
          .append(" nhnode:")
          .append(route.getNextHop())
          .append(" admin:")
          .append(route.getAdministrativeCost())
          .append(" cost:")
          .append(route.getMetric())
          .append(" tag:none")
          .append(" prot:")
          .append(route.getProtocol().protocolName())
          .append("\n");
    }
    return sb.toString();
  }
}