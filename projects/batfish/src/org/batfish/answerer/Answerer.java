package org.batfish.answerer;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.JsonDiff;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.JsonDiffAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Answerer {

   public static Answerer Create(Question question, Batfish batfish) {

      switch (question.getType()) {
      case ACL_REACHABILITY:
         return new AclReachabilityAnswerer(question, batfish);
      case BGP_ADVERTISEMENTS:
         return new BgpAdvertisementsAnswerer(question, batfish);
      case BGP_SESSION_CHECK:
         return new BgpSessionCheckAnswerer(question, batfish);
      case COMPARE_SAME_NAME:
         return new CompareSameNameAnswerer(question, batfish);
      case ERROR:
         return new ErrorAnswerer(question, batfish);
      case IPSEC_VPN_CHECK:
         return new IpsecVpnCheckAnswerer(question, batfish);
      case ISIS_LOOPBACKS:
         return new IsisLoopbacksAnswerer(question, batfish);
      case NEIGHBORS:
         return new NeighborsAnswerer(question, batfish);
      case NODES:
         return new NodesAnswerer(question, batfish);
      case OSPF_LOOPBACKS:
         return new OspfLoopbacksAnswerer(question, batfish);
      case PAIRWISE_VPN_CONNECTIVITY:
         return new PairwiseVpnConnectivityAnswerer(question, batfish);
      case PROTOCOL_DEPENDENCIES:
         return new ProtocolDependenciesAnswerer(question, batfish);
      case REACHABILITY:
         return new ReachabilityAnswerer(question, batfish);
      case ROUTES:
         return new RoutesAnswerer(question, batfish);
      case SELF_ADJACENCIES:
         return new SelfAdjacenciesAnswerer(question, batfish);
      case TRACEROUTE:
         return new TracerouteAnswerer(question, batfish);
      case UNDEFINED_REFERENCES:
         return new UndefinedReferencesAnswerer(question, batfish);
      case UNIQUE_BGP_PREFIX_ORIGINATION:
         return new UniqueBgpPrefixOriginationAnswerer(question, batfish);
      case UNIQUE_IP_ASSIGNMENTS:
         return new UniqueIpAssignmentsAnswerer(question, batfish);
      case UNUSED_STRUCTURES:
         return new UnusedStructuresAnswerer(question, batfish);
      default:
         throw new BatfishException("Unknown question type");
      }
   }

   Batfish _batfish;
   BatfishLogger _logger;

   Question _question;

   public Answerer(Question question, Batfish batfish) {
      _batfish = batfish;
      _logger = batfish.getLogger();
      _question = question;
   }

   public abstract AnswerElement answer(TestrigSettings testrigSettings);

   // this is the default differential answerer
   // if you want a custom one for a subclass, override this function in the
   // subclass
   public AnswerElement answerDiff() {
      _batfish.checkEnvironmentExists(_batfish.getBaseTestrigSettings());
      _batfish.checkEnvironmentExists(_batfish.getDeltaTestrigSettings());
      AnswerElement before = Create(_question, _batfish).answer(_batfish
            .getBaseTestrigSettings());
      AnswerElement after = Create(_question, _batfish).answer(_batfish
            .getDeltaTestrigSettings());
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         String beforeJsonStr = mapper.writeValueAsString(before);
         String afterJsonStr = mapper.writeValueAsString(after);
         JSONObject beforeJson = new JSONObject(beforeJsonStr);
         JSONObject afterJson = new JSONObject(afterJsonStr);
         JsonDiff diff = new JsonDiff(beforeJson, afterJson);

         return new JsonDiffAnswerElement(diff);
      }
      catch (JsonProcessingException | JSONException e) {
         throw new BatfishException(
               "Could not convert diff element to json string", e);
      }
   }
}
