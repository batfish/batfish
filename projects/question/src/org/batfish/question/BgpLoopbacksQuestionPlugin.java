package org.batfish.question;

import java.util.Iterator;
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
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class BgpLoopbacksQuestionPlugin extends QuestionPlugin {

   public static class BgpLoopbacksAnswerElement implements AnswerElement {

      private SortedMap<String, SortedSet<String>> _exported;

      private SortedMap<String, SortedSet<String>> _missing;

      public BgpLoopbacksAnswerElement() {
         _exported = new TreeMap<>();
         _missing = new TreeMap<>();
      }

      public void add(SortedMap<String, SortedSet<String>> map, String hostname,
            String interfaceName) {
         SortedSet<String> interfacesByHostname = map.get(hostname);
         if (interfacesByHostname == null) {
            interfacesByHostname = new TreeSet<>();
            map.put(hostname, interfacesByHostname);
         }
         interfacesByHostname.add(interfaceName);
      }

      @JsonIgnore
      public SortedMap<String, SortedSet<String>> getExported() {
         return _exported;
      }

      public SortedMap<String, SortedSet<String>> getMissing() {
         return _missing;
      }

      private Object interfacesToString(String indent, String header,
            SortedMap<String, SortedSet<String>> interfaces) {
         StringBuilder sb = new StringBuilder(indent + header + "\n");
         for (String node : interfaces.keySet()) {
            for (String iface : interfaces.get(node)) {
               sb.append(indent + indent + node + " : " + iface + "\n");
            }
         }
         return sb.toString();
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         StringBuilder sb = new StringBuilder(
               "Results for BGP loopbacks check\n");
         // if (_exported.size() > 0) {
         // sb.append(
         // interfacesToString(" ", "Exported loopbacks", _exported));
         // }
         if (_missing.size() > 0) {
            sb.append(interfacesToString("  ", "Missing loopbacks", _missing));
         }
         return sb.toString();

      }

      @JsonIgnore
      public void setExported(SortedMap<String, SortedSet<String>> exported) {
         _exported = exported;
      }

      public void setMissing(SortedMap<String, SortedSet<String>> missing) {
         _missing = missing;
      }

   }

   public static class BgpLoopbacksAnswerer extends Answerer {

      public BgpLoopbacksAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {

         BgpLoopbacksQuestion question = (BgpLoopbacksQuestion) _question;

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

         BgpLoopbacksAnswerElement answerElement = new BgpLoopbacksAnswerElement();

         _batfish.checkConfigurations();
         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();

         for (Entry<String, Configuration> e : configurations.entrySet()) {
            String hostname = e.getKey();
            if (!nodeRegex.matcher(hostname).matches()) {
               continue;
            }
            Configuration c = e.getValue();
            for (Vrf vrf : c.getVrfs().values()) {
               if (vrf.getOspfProcess() != null || vrf.getIsisProcess() != null
                     || vrf.getBgpProcess() == null) {
                  continue;
               }
               BgpProcess proc = vrf.getBgpProcess();
               Set<RoutingPolicy> exportPolicies = new TreeSet<>();
               for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
                  String exportPolicyName = neighbor.getExportPolicy();
                  if (exportPolicyName != null) {
                     RoutingPolicy exportPolicy = c.getRoutingPolicies()
                           .get(exportPolicyName);
                     if (exportPolicy != null) {
                        exportPolicies.add(exportPolicy);
                     }
                  }
               }
               for (Entry<String, Interface> e2 : vrf.getInterfaces()
                     .entrySet()) {
                  String interfaceName = e2.getKey();
                  Interface iface = e2.getValue();
                  if (iface.isLoopback(c.getConfigurationFormat())) {
                     boolean exported = false;

                     outerloop: for (Prefix prefix : iface.getAllPrefixes()) {
                        ConnectedRoute route = new ConnectedRoute(prefix,
                              interfaceName);
                        for (RoutingPolicy exportPolicy : exportPolicies) {
                           if (exportPolicy.process(route, null,
                                 new BgpRoute.Builder(), null, vrf.getName())) {
                              exported = true;
                              break outerloop;
                           }
                        }
                     }
                     if (exported) {
                        answerElement.add(answerElement.getExported(), hostname,
                              interfaceName);
                     }
                     else {
                        answerElement.add(answerElement.getMissing(), hostname,
                              interfaceName);
                     }
                  }
               }
            }
         }

         return answerElement;
      }

   }

   // <question_page_comment>
   /**
    * Lists which loopback interfaces are being announced into BGP.
    * <p>
    * When running BGP without an IGP, one may wish to announce loopback
    * interface IPs into BGP. This question produces the list of nodes for which
    * such announcements are not happening.
    *
    * @type BgpLoopbacks onefile
    *
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    *
    * @example bf_answer("BgpLoopbacks", nodeRegex='as2.*') Answers the question
    *          only for nodes whose names start with 'as2'.
    */
   public static class BgpLoopbacksQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public BgpLoopbacksQuestion() {
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "bgploopbacks";
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
      public String prettyPrint() {
         String retString = String.format("bgpLoopbacks %snodeRegex=\"%s\"",
               prettyPrintBase(), _nodeRegex);
         return retString;
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
      return new BgpLoopbacksAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new BgpLoopbacksQuestion();
   }

}
