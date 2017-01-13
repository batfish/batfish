package org.batfish.question;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PairwiseInterfaceConsistencyQuestionPlugin extends QuestionPlugin {

   // TODO:
   // currently this is just a copy of NeighborsAnswerElement.
   // can we reuse that class (or inherit from it)?
   public static class PairwiseInterfaceConsistencyAnswerElement implements AnswerElement {

      private static final String EBGP_NEIGHBORS_VAR = "ebgpNeighbors";

      private static final String IBGP_NEIGHBORS_VAR = "ibgpNeighbors";

      private final static String LAN_NEIGHBORS_VAR = "lanNeighbors";

      private SortedSet<IpEdge> _ebgpNeighbors;

      private SortedSet<IpEdge> _ibgpNeighbors;

      private SortedSet<Edge> _lanNeighbors;

      public void addLanEdge(Edge edge) {
         _lanNeighbors.add(edge);
      }

      @JsonProperty(EBGP_NEIGHBORS_VAR)
      public SortedSet<IpEdge> getEbgpNeighbors() {
         return _ebgpNeighbors;
      }

      @JsonProperty(IBGP_NEIGHBORS_VAR)
      public SortedSet<IpEdge> getIbgpNeighbors() {
         return _ibgpNeighbors;
      }

      @JsonProperty(LAN_NEIGHBORS_VAR)
      public SortedSet<Edge> getLanNeighbors() {
         return _lanNeighbors;
      }

      public void initEbgpNeighbors() {
         _ebgpNeighbors = new TreeSet<>();
      }

      public void initIbgpNeighbors() {
         _ibgpNeighbors = new TreeSet<>();
      }

      public void initLanNeighbors() {
         _lanNeighbors = new TreeSet<>();
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         StringBuilder sb = new StringBuilder("Results for pairwise interface consistency\n");

         if (_lanNeighbors != null) {
            sb.append("  LAN neighbors\n");
            for (Edge edge : _lanNeighbors) {
               sb.append("    " + edge.toString() + "\n");
            }
         }

         if (_ebgpNeighbors != null) {
            sb.append("  eBGP Neighbors\n");
            for (IpEdge ipEdge : _ebgpNeighbors) {
               sb.append("    " + ipEdge.toString() + "\n");
            }
         }

         if (_ibgpNeighbors != null) {
            sb.append("  iBGP Neighbors\n");
            for (IpEdge ipEdge : _ibgpNeighbors) {
               sb.append("    " + ipEdge.toString() + "\n");
            }
         }

         return sb.toString();
      }

      @JsonProperty(EBGP_NEIGHBORS_VAR)
      public void setEbgpNeighbors(SortedSet<IpEdge> ebgpNeighbors) {
         _ebgpNeighbors = ebgpNeighbors;
      }

      @JsonProperty(IBGP_NEIGHBORS_VAR)
      public void setIbgpNeighbors(SortedSet<IpEdge> ibgpNeighbors) {
         _ibgpNeighbors = ibgpNeighbors;
      }

      @JsonProperty(LAN_NEIGHBORS_VAR)
      public void setLanNeighbors(SortedSet<Edge> lanNeighbors) {
         _lanNeighbors = lanNeighbors;
      }
   }

   public static class PairwiseInterfaceConsistencyAnswerer extends Answerer {

      public PairwiseInterfaceConsistencyAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         PairwiseInterfaceConsistencyQuestion question = 
            (PairwiseInterfaceConsistencyQuestion) _question;

         // create a corresponding NeighborsQuestion
         NeighborsQuestionPlugin.NeighborsQuestion nquestion = 
            new NeighborsQuestionPlugin.NeighborsQuestion();
         nquestion.setNeighborTypes(question.getNeighborTypes());
         nquestion.setNode1Regex(question.getNode1Regex());
         nquestion.setNode2Regex(question.getNode2Regex());

         // answer the neighbors question
         NeighborsQuestionPlugin.NeighborsAnswerElement nAnswerElem = 
            (NeighborsQuestionPlugin.NeighborsAnswerElement)
            new NeighborsQuestionPlugin.NeighborsAnswerer(nquestion, _batfish).answer();

         // now perform our consistency checks.
         // currently just checking that the two ends of each edge have the same MTU value
         // TODO: since the neighbors question only gives us strings, we have to walk
         //    back through the configs.  seems like it'd be easier to directly get interfaces
         //    initially and then just check their properties

         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();

         // TODO: for now just filtering LAN neighbors instead of also BGP neighbors
         SortedSet<Edge> lanNeighbors = nAnswerElem.getLanNeighbors();
         SortedSet<Edge> filteredLanNeighbors = new TreeSet<Edge>();
         for (Edge e : lanNeighbors) {
            int mtu1 = configurations.get(e.getNode1()).getInterfaces().get(e.getInt1()).getMtu();
            int mtu2 = configurations.get(e.getNode2()).getInterfaces().get(e.getInt2()).getMtu();
            if (mtu1 != mtu2)
               filteredLanNeighbors.add(e);
         }

         // create a corresponding answer element for our question
         PairwiseInterfaceConsistencyAnswerElement answerElement =
            new PairwiseInterfaceConsistencyAnswerElement();
         answerElement.setEbgpNeighbors(nAnswerElem.getEbgpNeighbors());
         answerElement.setIbgpNeighbors(nAnswerElem.getIbgpNeighbors());
         answerElement.setLanNeighbors(filteredLanNeighbors);

         return answerElement;

      }

   }

   // <question_page_comment>
   /**
    * List pairs of connected interfaces that are inconsistent.
    * <p>
    * Details coming on the definition of inconsistent.
    *
    * @type PairwiseInterfaceConsistency multifile
    *
    * @param neighborType
    *           The type(s) of neighbor relationships to focus on among (eBGP,
    *           iBGP, IP). Default is IP.
    * @param node1Regex
    *           Regular expression to match the nodes names for one end of pair.
    *           Default is '.*' (all nodes).
    * @param node2Regex
    *           Regular expression to match the nodes names for the other end of
    *           the pair. Default is '.*' (all nodes).
    *
    * @example bf_answer("PairwiseInterfaceConsistency", neighborType=["ebgp", "ibgp"]
    *          node1Regex="as1.*", node2Regex="as2.*") Shows all eBGP and iBGP
    *          neighbor relationships that are inconsitenty between nodes that 
    *          start with as1 and those that start with as2.
    *
    */
   public static class PairwiseInterfaceConsistencyQuestion extends Question {

      /* TODO:
         Currently this is just a copy of NeighborsQuestion.
         Can we reuse that class or inherit from it?
      */

      private static final String NEIGHBOR_TYPE_VAR = "neighborType";

      private static final String NODE1_REGEX_VAR = "node1Regex";

      private static final String NODE2_REGEX_VAR = "node2Regex";

      private Set<NeighborType> _neighborTypes;

      private String _node1Regex;

      private String _node2Regex;

      public PairwiseInterfaceConsistencyQuestion() {
         _node1Regex = ".*";
         _node2Regex = ".*";
         _neighborTypes = EnumSet.noneOf(NeighborType.class);
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "pairwiseinterfaceconsistency";
      }

      @JsonProperty(NEIGHBOR_TYPE_VAR)
      public Set<NeighborType> getNeighborTypes() {
         return _neighborTypes;
      }

      @JsonProperty(NODE1_REGEX_VAR)
      public String getNode1Regex() {
         return _node1Regex;
      }

      @JsonProperty(NODE2_REGEX_VAR)
      public String getNode2Regex() {
         return _node2Regex;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         try {
            String retString = String.format(
                  "pairwiseinterfaceconsistency %s%s=%s | %s=%s | %s=%s", prettyPrintBase(),
                  NODE1_REGEX_VAR, _node1Regex, NODE2_REGEX_VAR, _node2Regex,
                  NEIGHBOR_TYPE_VAR, _neighborTypes.toString());
            return retString;
         }
         catch (Exception e) {
            try {
               return "Pretty printing failed. Printing Json\n"
                     + toJsonString();
            }
            catch (JsonProcessingException e1) {
               throw new BatfishException(
                     "Both pretty and json printing failed\n");
            }
         }

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
               case NODE1_REGEX_VAR:
                  setNode1Regex(parameters.getString(paramKey));
                  break;
               case NEIGHBOR_TYPE_VAR:
                  setNeighborTypes(
                        new ObjectMapper().<Set<NeighborType>> readValue(
                              parameters.getString(paramKey),
                              new TypeReference<Set<NeighborType>>() {
                              }));
                  break;
               case NODE2_REGEX_VAR:
                  setNode2Regex(parameters.getString(paramKey));
                  break;
               default:
                  throw new BatfishException("Unknown key in "
                        + getClass().getSimpleName() + ": " + paramKey);
               }
            }
            catch (JSONException | IOException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      public void setNeighborTypes(Set<NeighborType> neighborType) {
         _neighborTypes = neighborType;
      }

      public void setNode1Regex(String regex) {
         _node1Regex = regex;
      }

      public void setNode2Regex(String regex) {
         _node2Regex = regex;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new PairwiseInterfaceConsistencyAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new PairwiseInterfaceConsistencyQuestion();
   }

}
