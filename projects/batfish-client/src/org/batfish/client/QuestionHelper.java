package org.batfish.client;

import java.util.Map;

import org.batfish.datamodel.questions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

   public static Question getQuestion(String questionType) throws Exception {

      QuestionType qType = QuestionType.valueOf(questionType);

      switch (qType) {
      case ACL_REACHABILITY:
         return new AclReachabilityQuestion();
      case BGP_ADVERTISEMENTS:
         return new BgpAdvertisementsQuestion();
      case BGP_SESSION_CHECK:
         return new BgpSessionCheckQuestion();
      case COMPARE_SAME_NAME:
         return new CompareSameNameQuestion();
      case IPSEC_VPN_CHECK:
         return new IpsecVpnCheckQuestion();
      case ISIS_LOOPBACKS:
         return new IsisLoopbacksQuestion();
      case LOCAL_PATH:
         return new LocalPathQuestion();
      case MULTIPATH:
         return new MultipathQuestion();
      case NEIGHBORS:
         return new NeighborsQuestion();
      case NODES:
         return new NodesQuestion();
      case OSPF_LOOPBACKS:
         return new OspfLoopbacksQuestion();
      case PROTOCOL_DEPENDENCIES:
         return new ProtocolDependenciesQuestion();
      case REACHABILITY:
         return new ReachabilityQuestion();
      case REDUCED_REACHABILITY:
         return new ReducedReachabilityQuestion();
      case ROUTES:
         return new RoutesQuestion();
      case SELF_ADJACENCIES:
         return new SelfAdjacenciesQuestion();
      case TRACEROUTE:
         return new TracerouteQuestion();
      default:
         break;
      }

      throw new Exception("Unsupported question type " + questionType);
   }

   public static String getQuestionString(String questionType) throws Exception {
      Question question = getQuestion(questionType);

      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);

      return mapper.writeValueAsString(question);
   }
}
