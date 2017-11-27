package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class RoutesQuestionPlugin extends QuestionPlugin {

  public static class RoutesAnswerElement implements AnswerElement {

    private static final String PROP_ADDED = "added";

    private static final String PROP_AGAINST_ENVIRONMENT = "againstEnvironment";

    private static final String PROP_DETAIL_ROUTES_BY_HOSTNAME = "detailRoutesByHostname";

    private static final String PROP_DETAIL = "detail";

    private static final String PROP_REMOVED = "removed";

    private static final String PROP_ROUTES_BY_HOSTNAME = "routesByHostname";

    private SortedSet<Route> _added;

    private boolean _againstEnvironment;

    private boolean _detail;

    private SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> _detailRoutesByHostname;

    private SortedSet<Route> _removed;

    private SortedMap<String, RoutesByVrf> _routesByHostname;

    @JsonCreator
    public RoutesAnswerElement() {
      _detailRoutesByHostname = new TreeMap<>();
      _routesByHostname = new TreeMap<>();
      _added = new TreeSet<>();
      _removed = new TreeSet<>();
    }

    public RoutesAnswerElement(RoutesAnswerElement base, RoutesAnswerElement delta) {
      this();
      Set<String> hosts = new LinkedHashSet<>();
      hosts.addAll(base.getRoutesByHostname().keySet());
      hosts.addAll(delta.getRoutesByHostname().keySet());
      for (String host : hosts) {
        RoutesByVrf routesByVrf = new RoutesByVrf();
        _routesByHostname.put(host, routesByVrf);
        RoutesByVrf baseRoutesByVrf = base._routesByHostname.get(host);
        RoutesByVrf deltaRoutesByVrf = delta._routesByHostname.get(host);
        if (baseRoutesByVrf == null) {
          for (Entry<String, SortedSet<Route>> e : deltaRoutesByVrf.entrySet()) {
            String vrfName = e.getKey();
            SortedSet<Route> deltaRoutes = e.getValue();
            SortedSet<Route> routes = new TreeSet<>();
            routesByVrf.put(vrfName, routes);
            for (Route deltaRoute : deltaRoutes) {
              _added.add(deltaRoute);
              routes.add(deltaRoute);
            }
          }
        } else if (deltaRoutesByVrf == null) {
          for (Entry<String, SortedSet<Route>> e : baseRoutesByVrf.entrySet()) {
            String vrfName = e.getKey();
            SortedSet<Route> baseRoutes = e.getValue();
            SortedSet<Route> routes = new TreeSet<>();
            routesByVrf.put(vrfName, routes);
            for (Route baseRoute : baseRoutes) {
              _removed.add(baseRoute);
              routes.add(baseRoute);
            }
          }
        } else {
          Set<String> vrfNames = new LinkedHashSet<>();
          vrfNames.addAll(baseRoutesByVrf.keySet());
          vrfNames.addAll(deltaRoutesByVrf.keySet());
          for (String vrfName : vrfNames) {
            SortedSet<Route> routes = new TreeSet<>();
            routesByVrf.put(vrfName, routes);
            SortedSet<Route> baseRoutes = baseRoutesByVrf.get(vrfName);
            SortedSet<Route> deltaRoutes = deltaRoutesByVrf.get(vrfName);
            if (baseRoutes == null) {
              for (Route deltaRoute : deltaRoutes) {
                _added.add(deltaRoute);
                routes.add(deltaRoute);
              }
            } else if (deltaRoutes == null) {
              for (Route baseRoute : baseRoutes) {
                _removed.add(baseRoute);
                routes.add(baseRoute);
              }
            } else {
              Set<Route> tmpBaseRoutes = new LinkedHashSet<>(baseRoutes);
              baseRoutes.removeAll(deltaRoutes);
              deltaRoutes.removeAll(tmpBaseRoutes);
              for (Route baseRoute : baseRoutes) {
                _removed.add(baseRoute);
                routes.add(baseRoute);
              }
              for (Route deltaRoute : deltaRoutes) {
                _added.add(deltaRoute);
                routes.add(deltaRoute);
              }
            }
          }
        }
      }
    }

    public RoutesAnswerElement(
        SortedMap<String, RoutesByVrf> environmentRoutesByHostname,
        Pattern nodeRegex,
        Set<RoutingProtocol> protocols,
        PrefixSpace prefixSpace) {
      this();
      for (Entry<String, RoutesByVrf> e : environmentRoutesByHostname.entrySet()) {
        String hostname = e.getKey();
        if (!nodeRegex.matcher(hostname).matches()) {
          continue;
        }
        RoutesByVrf environmentRoutesByVrf = e.getValue();
        RoutesByVrf routesByVrf = new RoutesByVrf();
        _routesByHostname.put(hostname, routesByVrf);
        for (Entry<String, SortedSet<Route>> e2 : environmentRoutesByVrf.entrySet()) {
          String vrfName = e2.getKey();
          SortedSet<Route> routes = e2.getValue();
          SortedSet<Route> filteredRoutes;
          if (protocols.isEmpty() && prefixSpace.isEmpty()) {
            filteredRoutes = routes;
          } else {
            filteredRoutes = new TreeSet<>();
            for (Route environmentRoute : routes) {
              boolean matchProtocol =
                  protocols.isEmpty() || protocols.contains(environmentRoute.getProtocol());
              boolean matchPrefixSpace =
                  prefixSpace.isEmpty()
                      || prefixSpace.containsPrefix(environmentRoute.getNetwork());
              if (matchProtocol && matchPrefixSpace) {
                filteredRoutes.add(environmentRoute);
              }
            }
          }
          routesByVrf.put(vrfName, filteredRoutes);
        }
      }
    }

    public RoutesAnswerElement(
        SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> inputRoutesByHostname,
        Map<Ip, String> ipOwners,
        Pattern nodeRegex,
        Set<RoutingProtocol> protocols,
        PrefixSpace prefixSpace) {
      this();
      for (Entry<String, SortedMap<String, SortedSet<AbstractRoute>>> e :
          inputRoutesByHostname.entrySet()) {
        String hostname = e.getKey();
        if (!nodeRegex.matcher(hostname).matches()) {
          continue;
        }
        SortedMap<String, SortedSet<AbstractRoute>> inputRoutesByVrf = e.getValue();
        SortedMap<String, SortedSet<AbstractRoute>> detailRoutesByVrf = new TreeMap<>();
        RoutesByVrf routesByVrf = new RoutesByVrf();
        _routesByHostname.put(hostname, routesByVrf);
        _detailRoutesByHostname.put(hostname, detailRoutesByVrf);
        for (Entry<String, SortedSet<AbstractRoute>> e2 : inputRoutesByVrf.entrySet()) {
          String vrfName = e2.getKey();
          SortedSet<AbstractRoute> inputDetailRoutes = e2.getValue();
          SortedSet<Route> filteredRoutes = new TreeSet<>();
          SortedSet<AbstractRoute> filteredDetailRoutes;
          routesByVrf.put(vrfName, filteredRoutes);
          if (protocols.isEmpty() && prefixSpace.isEmpty()) {
            filteredDetailRoutes = inputDetailRoutes;
          } else {
            filteredDetailRoutes = new TreeSet<>();
            for (AbstractRoute detailRoute : inputDetailRoutes) {
              boolean matchProtocol =
                  protocols.isEmpty() || protocols.contains(detailRoute.getProtocol());
              boolean matchPrefixSpace =
                  prefixSpace.isEmpty() || prefixSpace.containsPrefix(detailRoute.getNetwork());
              if (matchProtocol && matchPrefixSpace) {
                filteredDetailRoutes.add(detailRoute);
              }
            }
          }
          detailRoutesByVrf.put(vrfName, filteredDetailRoutes);
          for (AbstractRoute detailRoute : filteredDetailRoutes) {
            Route route = detailRoute.toSummaryRoute(hostname, vrfName, ipOwners);
            detailRoute.setNextHop(route.getNextHop());
            detailRoute.setNode(hostname);
            detailRoute.setVrf(vrfName);
            filteredRoutes.add(route);
          }
        }
      }
    }

    @JsonProperty(PROP_ADDED)
    public SortedSet<Route> getAdded() {
      return _added;
    }

    @JsonProperty(PROP_AGAINST_ENVIRONMENT)
    public boolean getAgainstEnvironment() {
      return _againstEnvironment;
    }

    @JsonProperty(PROP_DETAIL)
    public boolean getDetail() {
      return _detail;
    }

    @JsonProperty(PROP_DETAIL_ROUTES_BY_HOSTNAME)
    public SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>>
        getDetailRoutesByHostname() {
      return _detailRoutesByHostname;
    }

    @JsonProperty(PROP_REMOVED)
    public SortedSet<Route> getRemoved() {
      return _removed;
    }

    @JsonProperty(PROP_ROUTES_BY_HOSTNAME)
    public SortedMap<String, RoutesByVrf> getRoutesByHostname() {
      return _routesByHostname;
    }

    @Override
    public String prettyPrint() {
      String addedSymbol = _againstEnvironment ? "BAT " : "+";
      String removedSymbol = _againstEnvironment ? "ENV " : "-";
      StringBuilder sb = new StringBuilder();
      if (!_detail) {
        for (Entry<String, RoutesByVrf> e : _routesByHostname.entrySet()) {
          RoutesByVrf routesByVrf = e.getValue();
          for (SortedSet<Route> routes : routesByVrf.values()) {
            for (Route route : routes) {
              String diffSymbol = null;
              if (_added.contains(route)) {
                diffSymbol = addedSymbol;
              } else if (_removed.contains(route)) {
                diffSymbol = removedSymbol;
              }
              String routeStr = route.prettyPrint(diffSymbol);
              sb.append(routeStr);
            }
          }
        }
      } else {
        for (Entry<String, SortedMap<String, SortedSet<AbstractRoute>>> e :
            _detailRoutesByHostname.entrySet()) {
          SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = e.getValue();
          for (Entry<String, SortedSet<AbstractRoute>> e2 : routesByVrf.entrySet()) {
            SortedSet<AbstractRoute> routes = e2.getValue();
            for (AbstractRoute route : routes) {
              String diffSymbol = null;
              if (_added.contains(route)) {
                diffSymbol = addedSymbol;
              } else if (_removed.contains(route)) {
                diffSymbol = removedSymbol;
              }
              String diffStr = diffSymbol != null ? diffSymbol + " " : "";
              String routeStr = route.fullString();
              String newStr = String.format("%s%s\n", diffStr, routeStr);
              sb.append(newStr);
            }
          }
        }
      }
      return sb.toString();
    }

    @JsonProperty(PROP_ADDED)
    public void setAdded(SortedSet<Route> added) {
      _added = added;
    }

    @JsonProperty(PROP_AGAINST_ENVIRONMENT)
    public void setAgainstEnvironment(boolean againstEnvironment) {
      _againstEnvironment = againstEnvironment;
    }

    @JsonProperty(PROP_DETAIL)
    public void setDetail(boolean detail) {
      _detail = detail;
    }

    @JsonProperty(PROP_DETAIL_ROUTES_BY_HOSTNAME)
    public void setDetailRoutesByHostname(
        SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> detailRoutesByHostname) {
      _detailRoutesByHostname = detailRoutesByHostname;
    }

    @JsonProperty(PROP_REMOVED)
    public void setRemoved(SortedSet<Route> removed) {
      _removed = removed;
    }

    @JsonProperty(PROP_ROUTES_BY_HOSTNAME)
    public void setRoutesByHostname(SortedMap<String, RoutesByVrf> routesByHostname) {
      _routesByHostname = routesByHostname;
    }
  }

  public static class RoutesAnswerer extends Answerer {

    public RoutesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public RoutesAnswerElement answer() {
      RoutesQuestion question = (RoutesQuestion) _question;
      if (question._againstEnvironment && question._fromEnvironment) {
        throw new BatfishException(
            String.format(
                "%s and %s flags are mutually exclusive",
                RoutesQuestion.PROP_AGAINST_ENVIRONMENT, RoutesQuestion.PROP_FROM_ENVIRONMENT));
      }
      if (question._againstEnvironment && question._detail) {
        throw new BatfishException(
            String.format(
                "%s and %s flags together are currently unsupported",
                RoutesQuestion.PROP_AGAINST_ENVIRONMENT, RoutesQuestion.PROP_DETAIL));
      }
      if (question._fromEnvironment && question._detail) {
        throw new BatfishException(
            String.format(
                "%s and %s flags together are currently unsupported",
                RoutesQuestion.PROP_FROM_ENVIRONMENT, RoutesQuestion.PROP_DETAIL));
      }
      Pattern nodeRegex;
      try {
        nodeRegex = Pattern.compile(question.getNodeRegex());
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            "Supplied regex for nodes is not a valid java regex: \""
                + question.getNodeRegex()
                + "\"",
            e);
      }
      RoutesAnswerElement answerElement;
      RoutesAnswerElement environmentAnswerElement = null;
      RoutesAnswerElement batfishAnswerElement = null;
      if (question._fromEnvironment || question._againstEnvironment) {
        SortedMap<String, RoutesByVrf> environmentRoutes = _batfish.loadEnvironmentRoutingTables();
        environmentAnswerElement =
            new RoutesAnswerElement(
                environmentRoutes, nodeRegex, question._protocols, question._prefixSpace);
      }
      if (!question._fromEnvironment) {
        SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByHostname =
            _batfish.getRoutes();
        Map<String, Configuration> configurations = _batfish.loadConfigurations();
        Map<Ip, String> ipOwnersSimple = CommonUtil.computeIpOwnersSimple(configurations, true);
        batfishAnswerElement =
            new RoutesAnswerElement(
                routesByHostname,
                ipOwnersSimple,
                nodeRegex,
                question._protocols,
                question._prefixSpace);
      }
      if (question._fromEnvironment) {
        answerElement = environmentAnswerElement;
      } else if (!question._againstEnvironment) {
        answerElement = batfishAnswerElement;
      } else {
        answerElement = new RoutesAnswerElement(environmentAnswerElement, batfishAnswerElement);
      }
      answerElement.setAgainstEnvironment(question._againstEnvironment);
      answerElement.setDetail(question._detail);
      return answerElement;
    }

    @Override
    public AnswerElement answerDiff() {
      RoutesQuestion question = (RoutesQuestion) _question;
      if (question.getAgainstEnvironment()) {
        throw new BatfishException(
            "Differential "
                + _question.getName()
                + " routes question unsupported when '"
                + RoutesQuestion.PROP_AGAINST_ENVIRONMENT
                + "' is set, due to unclear semantics of result.");
      }
      _batfish.pushBaseEnvironment();
      RoutesAnswerElement base = answer();
      _batfish.popEnvironment();
      _batfish.pushDeltaEnvironment();
      RoutesAnswerElement delta = answer();
      _batfish.popEnvironment();
      return new RoutesAnswerElement(base, delta);
    }
  }

  // <question_page_comment>

  /**
   * Outputs all routes (RIB) at nodes in the network.
   *
   * <p>It produces routes from all protocols (e.g., BGP, OSPF, static, and connected).
   *
   * @type Routes dataplane
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("Nodes", nodeRegex="as1.*") Outputs the routes for all nodes whose names
   *     begin with "as1".
   */
  public static class RoutesQuestion extends Question {

    private static final String PROP_AGAINST_ENVIRONMENT = "againstEnvironment";

    private static final String PROP_DETAIL = "detail";

    private static final String PROP_FROM_ENVIRONMENT = "fromEnvironment";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private static final String PROP_PREFIX_SPACE = "prefixSpace";

    private static final String PROP_PROTOCOLS = "protocols";

    private boolean _againstEnvironment;

    private boolean _detail;

    private boolean _fromEnvironment;

    private String _nodeRegex;

    private PrefixSpace _prefixSpace;

    private SortedSet<RoutingProtocol> _protocols;

    public RoutesQuestion() {
      _nodeRegex = ".*";
      _prefixSpace = new PrefixSpace();
      _protocols = new TreeSet<>();
    }

    @JsonProperty(PROP_AGAINST_ENVIRONMENT)
    public boolean getAgainstEnvironment() {
      return _againstEnvironment;
    }

    @Override
    public boolean getDataPlane() {
      return !_fromEnvironment;
    }

    @JsonProperty(PROP_DETAIL)
    public boolean getDetail() {
      return _detail;
    }

    @JsonProperty(PROP_FROM_ENVIRONMENT)
    public boolean getFromEnvironment() {
      return _fromEnvironment;
    }

    @Override
    public String getName() {
      return "routes";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_PREFIX_SPACE)
    public PrefixSpace getPrefixSpace() {
      return _prefixSpace;
    }

    @JsonProperty(PROP_PROTOCOLS)
    public SortedSet<RoutingProtocol> getProtocols() {
      return _protocols;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(PROP_AGAINST_ENVIRONMENT)
    public void setAgainstEnvironment(boolean againstEnvironment) {
      _againstEnvironment = againstEnvironment;
    }

    @JsonProperty(PROP_DETAIL)
    public void setDetail(boolean detail) {
      _detail = detail;
    }

    @JsonProperty(PROP_FROM_ENVIRONMENT)
    public void setFromEnvironment(boolean fromEnvironment) {
      _fromEnvironment = fromEnvironment;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(String nodeRegex) {
      _nodeRegex = nodeRegex;
    }

    @JsonProperty(PROP_PREFIX_SPACE)
    public void setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
    }

    @JsonProperty(PROP_PROTOCOLS)
    public void setProtocols(SortedSet<RoutingProtocol> protocols) {
      _protocols = protocols;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new RoutesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new RoutesQuestion();
  }
}
