package org.batfish.question;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutesQuestionPlugin extends QuestionPlugin {

   public static class RoutesAnswerElement implements AnswerElement {

      private static final String DETAIL_VAR = "detail";

      private SortedSet<Route> _added;

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

      public RoutesAnswerElement(RoutesAnswerElement base,
            RoutesAnswerElement delta) {
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
               for (Entry<String, SortedSet<Route>> e : deltaRoutesByVrf
                     .entrySet()) {
                  String vrfName = e.getKey();
                  SortedSet<Route> deltaRoutes = e.getValue();
                  SortedSet<Route> routes = new TreeSet<>();
                  routesByVrf.put(vrfName, routes);
                  for (Route deltaRoute : deltaRoutes) {
                     _added.add(deltaRoute);
                     routes.add(deltaRoute);
                  }
               }
            }
            else if (deltaRoutesByVrf == null) {
               for (Entry<String, SortedSet<Route>> e : baseRoutesByVrf
                     .entrySet()) {
                  String vrfName = e.getKey();
                  SortedSet<Route> baseRoutes = e.getValue();
                  SortedSet<Route> routes = new TreeSet<>();
                  routesByVrf.put(vrfName, routes);
                  for (Route baseRoute : baseRoutes) {
                     _removed.add(baseRoute);
                     routes.add(baseRoute);
                  }
               }
            }
            else {
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
                  }
                  else if (deltaRoutes == null) {
                     for (Route baseRoute : baseRoutes) {
                        _removed.add(baseRoute);
                        routes.add(baseRoute);
                     }
                  }
                  else {
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
            Pattern nodeRegex, Set<RoutingProtocol> protocols) {
         _routesByHostname = new TreeMap<>();
         for (Entry<String, RoutesByVrf> e : environmentRoutesByHostname
               .entrySet()) {
            String hostname = e.getKey();
            if (!nodeRegex.matcher(hostname).matches()) {
               continue;
            }
            RoutesByVrf environmentRoutesByVrf = e.getValue();
            RoutesByVrf routesByVrf = new RoutesByVrf();
            _routesByHostname.put(hostname, routesByVrf);
            for (Entry<String, SortedSet<Route>> e2 : environmentRoutesByVrf
                  .entrySet()) {
               String vrfName = e2.getKey();
               SortedSet<Route> routes = e2.getValue();
               if (!protocols.isEmpty()) {
                  SortedSet<Route> filteredRoutes = new TreeSet<>();
                  for (Route environmentRoute : routes) {
                     if (protocols.contains(environmentRoute.getProtocol())) {
                        filteredRoutes.add(environmentRoute);
                     }
                  }
                  routes = filteredRoutes;
               }
               routesByVrf.put(vrfName, routes);
            }
         }
      }

      public RoutesAnswerElement(
            SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> inputRoutesByHostname,
            Map<Ip, String> ipOwners, Pattern nodeRegex,
            Set<RoutingProtocol> protocols, PrefixSpace prefixSpace) {
         this();
         for (Entry<String, SortedMap<String, SortedSet<AbstractRoute>>> e : inputRoutesByHostname
               .entrySet()) {
            String hostname = e.getKey();
            if (!nodeRegex.matcher(hostname).matches()) {
               continue;
            }
            SortedMap<String, SortedSet<AbstractRoute>> inputRoutesByVrf = e
                  .getValue();
            SortedMap<String, SortedSet<AbstractRoute>> detailRoutesByVrf = new TreeMap<>();
            RoutesByVrf routesByVrf = new RoutesByVrf();
            _routesByHostname.put(hostname, routesByVrf);
            _detailRoutesByHostname.put(hostname, detailRoutesByVrf);
            for (Entry<String, SortedSet<AbstractRoute>> e2 : inputRoutesByVrf
                  .entrySet()) {
               String vrfName = e2.getKey();
               SortedSet<AbstractRoute> inputDetailRoutes = e2.getValue();
               SortedSet<Route> filteredRoutes = new TreeSet<>();
               SortedSet<AbstractRoute> filteredDetailRoutes;
               routesByVrf.put(vrfName, filteredRoutes);
               if (protocols.isEmpty() && prefixSpace.isEmpty()) {
                  filteredDetailRoutes = inputDetailRoutes;
               }
               else {
                  filteredDetailRoutes = new TreeSet<>();
                  for (AbstractRoute detailRoute : inputDetailRoutes) {
                     boolean matchProtocol = protocols.isEmpty()
                           || protocols.contains(detailRoute.getProtocol());
                     boolean matchPrefixSpace = prefixSpace.isEmpty()
                           || prefixSpace
                                 .containsPrefix(detailRoute.getNetwork());
                     if (matchProtocol && matchPrefixSpace) {
                        filteredDetailRoutes.add(detailRoute);
                     }
                  }
               }
               detailRoutesByVrf.put(vrfName, filteredDetailRoutes);
               for (AbstractRoute detailRoute : filteredDetailRoutes) {
                  Route route = detailRoute.toSummaryRoute(hostname, vrfName,
                        ipOwners);
                  detailRoute.setNextHop(route.getNextHop());
                  detailRoute.setNode(hostname);
                  detailRoute.setVrf(vrfName);
                  filteredRoutes.add(route);
               }
            }
         }
      }

      public SortedSet<Route> getAdded() {
         return _added;
      }

      @JsonProperty(DETAIL_VAR)
      public boolean getDetail() {
         return _detail;
      }

      public SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> getDetailRoutesByHostname() {
         return _detailRoutesByHostname;
      }

      public SortedSet<Route> getRemoved() {
         return _removed;
      }

      public SortedMap<String, RoutesByVrf> getRoutesByHostname() {
         return _routesByHostname;
      }

      @Override
      public String prettyPrint() {
         StringBuilder sb = new StringBuilder();
         if (!_detail) {
            for (Entry<String, RoutesByVrf> e : _routesByHostname.entrySet()) {
               RoutesByVrf routesByVrf = e.getValue();
               for (SortedSet<Route> routes : routesByVrf.values()) {
                  for (Route route : routes) {
                     String diffSymbol = null;
                     if (_added.contains(route)) {
                        diffSymbol = "+";
                     }
                     else if (_removed.contains(route)) {
                        diffSymbol = "-";
                     }
                     String routeStr = route.prettyPrint(diffSymbol);
                     sb.append(routeStr);
                  }
               }
            }
         }
         else {
            for (Entry<String, SortedMap<String, SortedSet<AbstractRoute>>> e : _detailRoutesByHostname
                  .entrySet()) {
               SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = e
                     .getValue();
               for (Entry<String, SortedSet<AbstractRoute>> e2 : routesByVrf
                     .entrySet()) {
                  SortedSet<AbstractRoute> routes = e2.getValue();
                  for (AbstractRoute route : routes) {
                     String diffSymbol = null;
                     if (_added.contains(route)) {
                        diffSymbol = "+";
                     }
                     else if (_removed.contains(route)) {
                        diffSymbol = "-";
                     }
                     String diffStr = diffSymbol != null ? diffSymbol + " "
                           : "";
                     String routeStr = route.fullString();
                     String newStr = String.format("%s%s\n", diffStr, routeStr);
                     sb.append(newStr);
                  }
               }
            }
         }
         return sb.toString();
      }

      public void setAdded(SortedSet<Route> added) {
         _added = added;
      }

      @JsonProperty(DETAIL_VAR)
      public void setDetail(boolean detail) {
         _detail = detail;
      }

      public void setDetailRoutesByHostname(
            SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> detailRoutesByHostname) {
         _detailRoutesByHostname = detailRoutesByHostname;
      }

      public void setRemoved(SortedSet<Route> removed) {
         _removed = removed;
      }

      public void setRoutesByHostname(
            SortedMap<String, RoutesByVrf> routesByHostname) {
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
         Pattern nodeRegex;
         try {
            nodeRegex = Pattern.compile(question.getNodeRegex());
         }
         catch (PatternSyntaxException e) {
            throw new BatfishException(
                  "Supplied regex for nodes is not a valid java regex: \""
                        + question.getNodeRegex() + "\"",
                  e);
         }
         RoutesAnswerElement answerElement;
         if (question._fromEnvironment) {
            SortedMap<String, RoutesByVrf> environmentRoutes = _batfish
                  .loadEnvironmentRoutingTables();
            answerElement = new RoutesAnswerElement(environmentRoutes,
                  nodeRegex, question._protocols);

         }
         else {
            SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByHostname = _batfish
                  .getRoutes();
            Map<String, Configuration> configurations = _batfish
                  .loadConfigurations();
            Map<Ip, Set<String>> ipOwners = _batfish
                  .computeIpOwners(configurations, true);
            Map<Ip, String> ipOwnersSimple = _batfish
                  .computeIpOwnersSimple(ipOwners);
            answerElement = new RoutesAnswerElement(routesByHostname,
                  ipOwnersSimple, nodeRegex, question._protocols,
                  question._prefixSpace);
         }
         answerElement.setDetail(question._detail);
         return answerElement;
      }

      @Override
      public AnswerElement answerDiff() {
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
    * <p>
    * It produces routes from all protocols (e.g., BGP, OSPF, static, and
    * connected).
    *
    * @type Routes dataplane
    *
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    *
    * @example bf_answer("Nodes", nodeRegex="as1.*") Outputs the routes for all
    *          nodes whose names begin with "as1".
    */
   public static class RoutesQuestion extends Question {

      private static final String DETAIL_VAR = "detail";

      private static final String FROM_ENVIRONMENT_VAR = "fromEnvironment";

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private static final String PREFIX_SPACE_VAR = "prefixSpace";

      private static final String PROTOCOLS_VAR = "protocols";

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

      @Override
      public boolean getDataPlane() {
         return !_fromEnvironment;
      }

      @JsonProperty(DETAIL_VAR)
      public boolean getDetail() {
         return _detail;
      }

      @JsonProperty(FROM_ENVIRONMENT_VAR)
      public boolean getFromEnvironment() {
         return _fromEnvironment;
      }

      @Override
      public String getName() {
         return "routes";
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @JsonProperty(PREFIX_SPACE_VAR)
      public PrefixSpace getPrefixSpace() {
         return _prefixSpace;
      }

      @JsonProperty(PROTOCOLS_VAR)
      public SortedSet<RoutingProtocol> getProtocols() {
         return _protocols;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @JsonProperty(DETAIL_VAR)
      public void setDetail(boolean detail) {
         _detail = detail;
      }

      @JsonProperty(FROM_ENVIRONMENT_VAR)
      public void setFromEnvironment(boolean fromEnvironment) {
         _fromEnvironment = fromEnvironment;
      }

      @JsonProperty(NODE_REGEX_VAR)
      public void setNodeRegex(String nodeRegex) {
         _nodeRegex = nodeRegex;
      }

      @JsonProperty(PREFIX_SPACE_VAR)
      public void setPrefixSpace(PrefixSpace prefixSpace) {
         _prefixSpace = prefixSpace;
      }

      @JsonProperty(PROTOCOLS_VAR)
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
