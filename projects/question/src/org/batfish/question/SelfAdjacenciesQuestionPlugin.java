package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SelfAdjacenciesQuestionPlugin extends QuestionPlugin {

   public static class SelfAdjacenciesAnswerElement implements AnswerElement {

      public static class InterfaceIpPair extends Pair<String, Ip> {

         private static final String INTERFACE_NAME_VAR = "interfaceName";

         private static final String IP_VAR = "ip";
         /**
          *
          */
         private static final long serialVersionUID = 1L;

         @JsonCreator
         public InterfaceIpPair(@JsonProperty(INTERFACE_NAME_VAR) String t1,
               @JsonProperty(IP_VAR) Ip t2) {
            super(t1, t2);
         }

         @JsonProperty(INTERFACE_NAME_VAR)
         public String getInterfaceName() {
            return _first;
         }

         @JsonProperty(IP_VAR)
         public Ip getIp() {
            return _second;
         }

      }

      private SortedMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>> _selfAdjacencies;

      public SelfAdjacenciesAnswerElement() {
         _selfAdjacencies = new TreeMap<>();
      }

      public void add(String hostname, Prefix prefix, String interfaceName,
            Ip address) {
         SortedMap<Prefix, SortedSet<InterfaceIpPair>> prefixMap = _selfAdjacencies
               .get(hostname);
         if (prefixMap == null) {
            prefixMap = new TreeMap<>();
            _selfAdjacencies.put(hostname, prefixMap);
         }
         SortedSet<InterfaceIpPair> interfaces = prefixMap.get(prefix);
         if (interfaces == null) {
            interfaces = new TreeSet<>();
            prefixMap.put(prefix, interfaces);
         }
         interfaces.add(new InterfaceIpPair(interfaceName, address));
      }

      public SortedMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>> getSelfAdjacencies() {
         return _selfAdjacencies;
      }

      public void setSelfAdjacencies(
            SortedMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>> selfAdjacencies) {
         _selfAdjacencies = selfAdjacencies;
      }

   }

   public static class SelfAdjacenciesAnswerer extends Answerer {

      public SelfAdjacenciesAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {

         SelfAdjacenciesQuestion question = (SelfAdjacenciesQuestion) _question;

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

         SelfAdjacenciesAnswerElement answerElement = new SelfAdjacenciesAnswerElement();
         _batfish.checkConfigurations();
         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();
         configurations.forEach((hostname, c) -> {
            if (nodeRegex.matcher(hostname).matches()) {
               for (Vrf vrf : c.getVrfs().values()) {
                  MultiSet<Prefix> nodePrefixes = new TreeMultiSet<>();
                  for (Interface iface : vrf.getInterfaces().values()) {
                     Set<Prefix> ifaceBasePrefixes = new HashSet<>();
                     if (iface.getActive()) {
                        for (Prefix prefix : iface.getAllPrefixes()) {
                           Prefix basePrefix = prefix.getNetworkPrefix();
                           if (!ifaceBasePrefixes.contains(basePrefix)) {
                              ifaceBasePrefixes.add(basePrefix);
                              nodePrefixes.add(basePrefix);
                           }
                        }
                     }
                  }
                  for (Interface iface : vrf.getInterfaces().values()) {
                     for (Prefix prefix : iface.getAllPrefixes()) {
                        Prefix basePrefix = prefix.getNetworkPrefix();
                        if (nodePrefixes.count(basePrefix) > 1) {
                           Ip address = prefix.getAddress();
                           String interfaceName = iface.getName();
                           answerElement.add(hostname, basePrefix,
                                 interfaceName, address);
                        }
                     }
                  }
               }
            }
         });
         return answerElement;
      }
   }

   // <question_page_comment>
   /**
    * Outputs cases where two interfaces on the same node are in the same
    * subnet.
    * <p>
    * This occurrence likely indicates an error in IP address assignment.
    *
    * @type SelfAdjacencies onefile
    *
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    *
    * @example bf_answer("SelfAdjacencies", nodeRegex="as1.*") Analyze nodes
    *          whose names begin with "as1".
    */
   public static class SelfAdjacenciesQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public SelfAdjacenciesQuestion() {
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "selfadjacencies";
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @JsonProperty(NODE_REGEX_VAR)
      public void setNodeRegex(String nodeRegex) {
         _nodeRegex = nodeRegex;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new SelfAdjacenciesAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new SelfAdjacenciesQuestion();
   }

}
