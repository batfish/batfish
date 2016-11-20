package org.batfish.question;

import java.util.HashSet;
import java.util.Iterator;
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
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RoutesQuestionPlugin extends QuestionPlugin {

   public static class RoutesAnswerElement implements AnswerElement {

      private SortedMap<String, SortedSet<Route>> _routesByHostname;

      @JsonCreator
      public RoutesAnswerElement() {
      }

      public RoutesAnswerElement(Map<String, Configuration> configurations,
            Pattern nodeRegex) {
         _routesByHostname = new TreeMap<>();
         for (Entry<String, Configuration> e : configurations.entrySet()) {
            String hostname = e.getKey();
            if (!nodeRegex.matcher(hostname).matches()) {
               continue;
            }
            Configuration c = e.getValue();
            SortedSet<Route> routes = c.getRoutes();
            _routesByHostname.put(hostname, routes);
         }
      }

      public RoutesAnswerElement(RoutesAnswerElement base,
            RoutesAnswerElement delta) {
         _routesByHostname = new TreeMap<>();
         Set<String> hosts = new LinkedHashSet<>();
         hosts.addAll(base.getRoutesByHostname().keySet());
         hosts.addAll(delta.getRoutesByHostname().keySet());
         for (String host : hosts) {
            SortedSet<Route> routes = new TreeSet<>();
            _routesByHostname.put(host, routes);
            SortedSet<Route> baseRoutes = base._routesByHostname.get(host);
            SortedSet<Route> deltaRoutes = delta._routesByHostname.get(host);
            if (baseRoutes == null) {
               for (Route route : deltaRoutes) {
                  route.setDiffSymbol("+");
                  routes.add(route);
               }
            }
            else if (deltaRoutes == null) {
               for (Route route : baseRoutes) {
                  route.setDiffSymbol("-");
                  routes.add(route);
               }
            }
            else {
               Set<Route> tmpBaseRoutes = new HashSet<>(baseRoutes);
               baseRoutes.removeAll(deltaRoutes);
               deltaRoutes.removeAll(tmpBaseRoutes);
               for (Route route : baseRoutes) {
                  route.setDiffSymbol("-");
                  routes.add(route);
               }
               for (Route route : deltaRoutes) {
                  route.setDiffSymbol("+");
                  routes.add(route);
               }
            }
         }
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Route>> getRoutesByHostname() {
         return _routesByHostname;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setRoutesByHostname(
            SortedMap<String, SortedSet<Route>> routesByHostname) {
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
         _batfish.checkDataPlaneQuestionDependencies();
         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();
         _batfish.initRoutes(configurations);
         RoutesAnswerElement answerElement = new RoutesAnswerElement(
               configurations, nodeRegex);
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
    * @type Routes
    *
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    *
    * @example bf_answer("Nodes", nodeRegex="as1.*") Outputs the routes for all
    *          nodes whose names begin with "as1".
    */
   public static class RoutesQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public RoutesQuestion() {
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return true;
      }

      @Override
      public String getName() {
         return "routes";
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public void setJsonParameters(JSONObject parameters) {
         super.setJsonParameters(parameters);
         Iterator<?> paramKeys = parameters.keys();
         while (paramKeys.hasNext()) {
            String paramKey = (String) paramKeys.next();
            if (isBaseParamKey(paramKey)) {
               continue;
            }
            try {
               switch (paramKey) {
               case NODE_REGEX_VAR:
                  setNodeRegex(parameters.getString(paramKey));
                  break;
               default:
                  throw new BatfishException("Unknown key in "
                        + getClass().getSimpleName() + ": " + paramKey);
               }
            }
            catch (JSONException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      public void setNodeRegex(String nodeRegex) {
         _nodeRegex = nodeRegex;
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
