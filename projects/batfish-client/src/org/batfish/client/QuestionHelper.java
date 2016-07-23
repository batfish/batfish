package org.batfish.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.questions.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;

public class QuestionHelper {

   public enum MacroType {
      ISOLATION("isolation"),
      REACHABILITY("reachability"),
      TRACEROUTE("traceroute");
      
      private final static Map<String, MacroType> _map = buildMap();

      private static Map<String, MacroType> buildMap() {
         Map<String, MacroType> map = new HashMap<String, MacroType>();
         for (MacroType value : MacroType.values()) {
            String name = value._name;
            map.put(name, value);
         }
         return Collections.unmodifiableMap(map);
      }

      @JsonCreator
      public static MacroType fromName(String name) {
         MacroType instance = _map.get(name.toLowerCase());
         if (instance == null) {
            throw new BatfishException("Not a valid MacroType: \"" + name
                  + "\"");
         }
         return instance;
      }

      private final String _name;

      private MacroType(String name) {
         _name = name;
      }

      public String macroTypeName() {
         return _name;
      }

   }
   
   public static String getParametersString(Map<String, String> parameters)
         throws Exception {
      String retString = "{\n";

      for (String paramKey : parameters.keySet()) {
         retString += String.format("\"%s\" : %s,\n", paramKey,
               parameters.get(paramKey));
      }

      retString += "}\n";

      return retString;
   }

   public static Question getQuestion(QuestionType questionType) {

      switch (questionType) {
      case ACL_REACHABILITY:
         return new AclReachabilityQuestion();
      case BGP_ADVERTISEMENTS:
         return new BgpAdvertisementsQuestion();
      case BGP_SESSION_CHECK:
         return new BgpSessionCheckQuestion();
      case COMPARE_SAME_NAME:
         return new CompareSameNameQuestion();
      case ERROR:
         return new ErrorQuestion();
      case IPSEC_VPN_CHECK:
         return new IpsecVpnCheckQuestion();
      case ISIS_LOOPBACKS:
         return new IsisLoopbacksQuestion();
      case NEIGHBORS:
         return new NeighborsQuestion();
      case NODES:
         return new NodesQuestion();
      case OSPF_LOOPBACKS:
         return new OspfLoopbacksQuestion();
      case PAIRWISE_VPN_CONNECTIVITY:
         return new PairwiseVpnConnectivityQuestion();
      case PROTOCOL_DEPENDENCIES:
         return new ProtocolDependenciesQuestion();
      case REACHABILITY:
         return new ReachabilityQuestion();
      case ROUTES:
         return new RoutesQuestion();
      case SELF_ADJACENCIES:
         return new SelfAdjacenciesQuestion();
      case TRACEROUTE:
         return new TracerouteQuestion();
      case UNDEFINED_REFERENCES:
         return new UndefinedReferencesQuestion();
      case UNIQUE_BGP_PREFIX_ORIGINATION:
         return new UniqueBgpPrefixOriginationQuestion();
      case UNIQUE_IP_ASSIGNMENTS:
         return new UniqueIpAssignmentsQuestion();
      case UNUSED_STRUCTURES:
         return new UnusedStructuresQuestion();
      default:
         break;
      }

      throw new BatfishException("Unsupported question type " + questionType);
   }

   public static Question getQuestion(String questionTypeStr) {
      QuestionType qType = QuestionType.fromName(questionTypeStr);
      return getQuestion(qType);
   }

   public static String getQuestionString(QuestionType questionType) 
         throws JsonProcessingException {      
      return getQuestion(questionType).toJsonString();
   }

   public static String getQuestionString(String questionTypeStr, boolean isDiff)
         throws JsonProcessingException {
      Question question = getQuestion(questionTypeStr);
      question.setDifferential(isDiff);
      return question.toJsonString();
   }

   public static String resolveMacro(String macroName, String paramsLine) 
         throws JsonProcessingException {
      String macro = macroName.replace("#", "");
      MacroType macroType = MacroType.fromName(macro);
      
      switch(macroType) {
      case ISOLATION:
      case REACHABILITY:
         throw new BatfishException("Unimplemented macrotype: " + macroType);
         
      case TRACEROUTE:
         String[] words = paramsLine.split(" ");
         if (words.length < 2) {
            throw new BatfishException("Incorrect usage for traceroute macro. " + 
                  "Should be:\n #traceroute <srcNode> <dstip> [<protocol> [<port>]]");
         }
         ReachabilityQuestion question = new ReachabilityQuestion();
         question.setReachabilityType(ReachabilityType.STANDARD);
         String srcNode = words[0];
         String dstIp = words[1];
         
         question.setIngressNodeRegex(srcNode);
         Set<Prefix> prefixSet = new HashSet<Prefix>();
         prefixSet.add(new Prefix(new Ip(dstIp), 32));
         question.setDstPrefixes(prefixSet);
         
         return question.toJsonString();

      default:
         throw new BatfishException("Unknown macrotype: " + macroType);
      }
   }
}
