package org.batfish.client;

import java.util.Map;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class QuestionHelper {

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

   public static Question getQuestion(QuestionType questionType)
         throws Exception {

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

      throw new Exception("Unsupported question type " + questionType);
   }

   public static Question getQuestion(String questionTypeStr) throws Exception {
      QuestionType qType = QuestionType.fromName(questionTypeStr);
      return getQuestion(qType);
   }

   public static String getQuestionString(QuestionType questionType)
         throws Exception {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(getQuestion(questionType));
   }

   public static String getQuestionString(String questionType, boolean isDiff)
         throws Exception {
      ObjectMapper mapper = new BatfishObjectMapper();

      Question question = getQuestion(questionType);
      question.setDifferential(isDiff);

      return mapper.writeValueAsString(question);
   }
}
