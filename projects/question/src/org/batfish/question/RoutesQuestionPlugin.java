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
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutesQuestionPlugin extends QuestionPlugin {

   public static class RoutesAnswerElement implements AnswerElement {

      private transient boolean _diff;

      private SortedMap<String, RoutesByVrf> _routesByHostname;

      @JsonCreator
      public RoutesAnswerElement() {
         _routesByHostname = new TreeMap<>();
      }

      public RoutesAnswerElement(Map<String, Configuration> configurations,
            Pattern nodeRegex, Set<RoutingProtocol> protocols,
            PrefixSpace prefixSpace) {
         _routesByHostname = new TreeMap<>();
         for (Entry<String, Configuration> e : configurations.entrySet()) {
            String hostname = e.getKey();
            if (!nodeRegex.matcher(hostname).matches()) {
               continue;
            }
            Configuration c = e.getValue();
            RoutesByVrf routesByVrf = new RoutesByVrf();
            _routesByHostname.put(hostname, routesByVrf);
            for (Entry<String, Vrf> e2 : c.getVrfs().entrySet()) {
               String vrfName = e2.getKey();
               Vrf vrf = e2.getValue();
               SortedSet<Route> routes = vrf.getRoutes();
               SortedSet<Route> filteredRoutes;
               if (protocols.isEmpty() && prefixSpace.isEmpty()) {
                  filteredRoutes = routes;
               }
               else {
                  filteredRoutes = new TreeSet<>();
                  for (Route route : routes) {
                     boolean matchProtocol = protocols.isEmpty()
                           || protocols.contains(route.getProtocol());
                     boolean matchPrefixSpace = prefixSpace.isEmpty()
                           || prefixSpace.containsPrefix(route.getNetwork());
                     if (matchProtocol && matchPrefixSpace) {
                        filteredRoutes.add(route);
                     }
                  }
               }
               routesByVrf.put(vrfName, filteredRoutes);
            }
         }
      }

      public RoutesAnswerElement(RoutesAnswerElement base,
            RoutesAnswerElement delta) {
         _diff = true;
         _routesByHostname = new TreeMap<>();
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
                     deltaRoute.setDiffSymbol("+");
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
                     baseRoute.setDiffSymbol("-");
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
                        deltaRoute.setDiffSymbol("+");
                        routes.add(deltaRoute);
                     }
                  }
                  else if (deltaRoutes == null) {
                     for (Route baseRoute : baseRoutes) {
                        baseRoute.setDiffSymbol("-");
                        routes.add(baseRoute);
                     }
                  }
                  else {
                     Set<Route> tmpBaseRoutes = new LinkedHashSet<>(baseRoutes);
                     baseRoutes.removeAll(deltaRoutes);
                     deltaRoutes.removeAll(tmpBaseRoutes);
                     for (Route baseRoute : baseRoutes) {
                        baseRoute.setDiffSymbol("-");
                        routes.add(baseRoute);
                     }
                     for (Route deltaRoute : deltaRoutes) {
                        deltaRoute.setDiffSymbol("+");
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

      public SortedMap<String, RoutesByVrf> getRoutesByHostname() {
         return _routesByHostname;
      }

      @Override
      public String prettyPrint() {
         StringBuilder sb = new StringBuilder();
         for (Entry<String, RoutesByVrf> e : _routesByHostname.entrySet()) {
            RoutesByVrf routesByVrf = e.getValue();
            for (SortedSet<Route> routes : routesByVrf.values()) {
               for (Route route : routes) {
                  String routeStr = route.prettyPrint(_diff);
                  sb.append(routeStr);
               }
            }
         }
         return sb.toString();
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
            _batfish.checkDataPlaneQuestionDependencies();
            Map<String, Configuration> configurations = _batfish
                  .loadConfigurations();
            _batfish.initRoutes(configurations);
            answerElement = new RoutesAnswerElement(configurations, nodeRegex,
                  question._protocols, question._prefixSpace);
         }
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

      private static final String FROM_ENVIRONMENT_VAR = "fromEnvironment";

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private static final String PREFIX_SPACE_VAR = "prefixSpace";

      private static final String PROTOCOLS_VAR = "protocols";

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
