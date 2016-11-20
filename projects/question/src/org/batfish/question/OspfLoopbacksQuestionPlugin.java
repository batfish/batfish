package org.batfish.question;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OspfLoopbacksQuestionPlugin extends QuestionPlugin {

   public static class OspfLoopbacksAnswerElement implements AnswerElement {

      private SortedMap<String, SortedSet<String>> _active;

      private SortedMap<String, SortedSet<String>> _exported;

      private SortedMap<String, SortedSet<String>> _inactive;

      private SortedMap<String, SortedSet<String>> _passive;

      private SortedMap<String, SortedSet<String>> _running;

      public OspfLoopbacksAnswerElement() {
         _active = new TreeMap<>();
         _exported = new TreeMap<>();
         _inactive = new TreeMap<>();
         _passive = new TreeMap<>();
         _running = new TreeMap<>();
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

      public SortedMap<String, SortedSet<String>> getActive() {
         return _active;
      }

      public SortedMap<String, SortedSet<String>> getExported() {
         return _exported;
      }

      public SortedMap<String, SortedSet<String>> getInactive() {
         return _inactive;
      }

      public SortedMap<String, SortedSet<String>> getPassive() {
         return _passive;
      }

      public SortedMap<String, SortedSet<String>> getRunning() {
         return _running;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setActive(SortedMap<String, SortedSet<String>> active) {
         _active = active;
      }

      public void setExported(SortedMap<String, SortedSet<String>> exported) {
         _exported = exported;
      }

      public void setInactive(SortedMap<String, SortedSet<String>> inactive) {
         _inactive = inactive;
      }

      public void setPassive(SortedMap<String, SortedSet<String>> passive) {
         _passive = passive;
      }

      public void setRunning(SortedMap<String, SortedSet<String>> running) {
         _running = running;
      }
   }

   public static class OspfLoopbacksAnswerer extends Answerer {

      public OspfLoopbacksAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {

         OspfLoopbacksQuestion question = (OspfLoopbacksQuestion) _question;

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

         OspfLoopbacksAnswerElement answerElement = new OspfLoopbacksAnswerElement();

         _batfish.checkConfigurations();
         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();

         for (Entry<String, Configuration> e : configurations.entrySet()) {
            String hostname = e.getKey();
            if (!nodeRegex.matcher(hostname).matches()) {
               continue;
            }
            Configuration c = e.getValue();
            for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
               String interfaceName = e2.getKey();
               Interface iface = e2.getValue();
               if (iface.isLoopback(c.getConfigurationFormat())) {
                  if (iface.getOspfEnabled()) {
                     // ospf is running either passively or actively
                     answerElement.add(answerElement.getRunning(), hostname,
                           interfaceName);
                     if (iface.getOspfPassive()) {
                        answerElement.add(answerElement.getPassive(), hostname,
                              interfaceName);
                     }
                     else {
                        answerElement.add(answerElement.getActive(), hostname,
                              interfaceName);
                     }
                  }
                  else {
                     // check if exported as external ospf route
                     boolean exported = false;
                     OspfProcess proc = c.getOspfProcess();
                     if (proc != null) {
                        String exportPolicyName = proc.getExportPolicy();
                        if (exportPolicyName != null) {
                           RoutingPolicy exportPolicy = c.getRoutingPolicies()
                                 .get(exportPolicyName);
                           if (exportPolicy != null) {
                              for (Prefix prefix : iface.getAllPrefixes()) {
                                 ConnectedRoute route = new ConnectedRoute(
                                       prefix, interfaceName);
                                 if (exportPolicy.process(route, null,
                                       new OspfExternalRoute.Builder(), null)) {
                                    exported = true;
                                 }
                              }
                           }
                        }
                     }

                     if (exported) {
                        answerElement.add(answerElement.getExported(), hostname,
                              interfaceName);
                     }
                     else {
                        // not exported, so should be inactive
                        answerElement.add(answerElement.getInactive(), hostname,
                              interfaceName);
                     }
                  }
               }
            }
         }

         return answerElement;
      }

   }

   //<question_page_comment>
   /**
    * Lists which loopbacks interfaces are being announced into OSPF.
    * <p>
    * When running OSPF, it is a good practice to announce loopbacks interface IPs into OSPF. 
    * This question produces the list of nodes for which such announcements are happening. 
    * 
    * @type OspfLoopbacks
    * 
    * @param nodeRegex Regular expression for names of nodes to include. 
    *                  Default value is '.*' (all nodes).
    * 
    * @example bf_answer("OspfLoopbacks", nodeRegex='as2.*')
    *          Answers the question only for nodes whose names start with 'as2'.
    */
   public static class OspfLoopbacksQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public OspfLoopbacksQuestion() {
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "ospfloopbacks";
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
         String retString = String.format("ospfLoopbacks %snodeRegex=\"%s\"",
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
      return new OspfLoopbacksAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new OspfLoopbacksQuestion();
   }

}
