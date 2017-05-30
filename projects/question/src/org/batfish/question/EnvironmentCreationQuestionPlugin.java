package org.batfish.question;

import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.IEnvironmentCreationQuestion;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EnvironmentCreationQuestionPlugin extends QuestionPlugin {

   public static class EnvironmentCreationAnswerer extends Answerer {

      public EnvironmentCreationAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         EnvironmentCreationQuestion question = (EnvironmentCreationQuestion) _question;
         // TODO: add flag to question determining whether or not to compute
         // data
         // plane
         boolean dp = false;
         return _batfish.createEnvironment(question.getEnvironmentName(),
               question.getNodeBlacklist(), question.getInterfaceBlacklist(),
               question.getEdgeBlacklist(), dp);
      }

   }

   /**
    * Since this is not really a question; we do not document it as such.
    */
   public static class EnvironmentCreationQuestion extends Question
         implements IEnvironmentCreationQuestion {

      private static final String EDGE_BLACKLIST_VAR = "edgeBlacklist";

      private static final String ENVIRONMENT_NAME_VAR = ENVIRONMENT_NAME_KEY;

      private static final String INTERFACE_BLACKLIST_VAR = "interfaceBlacklist";

      private static final String NODE_BLACKLIST_VAR = "nodeBlacklist";

      private Topology _edgeBlacklist;

      private String _environmentName;

      private SortedSet<NodeInterfacePair> _interfaceBlacklist;

      private NodeSet _nodeBlacklist;

      @JsonCreator
      public EnvironmentCreationQuestion() {
         _nodeBlacklist = new NodeSet();
         _interfaceBlacklist = new TreeSet<>();
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      @JsonIgnore
      public boolean getDifferential() {
         return false;
      }

      @JsonProperty(EDGE_BLACKLIST_VAR)
      public Topology getEdgeBlacklist() {
         return _edgeBlacklist;
      }

      @JsonProperty(ENVIRONMENT_NAME_VAR)
      public String getEnvironmentName() {
         return _environmentName;
      }

      @JsonProperty(INTERFACE_BLACKLIST_VAR)
      public SortedSet<NodeInterfacePair> getInterfaceBlacklist() {
         return _interfaceBlacklist;
      }

      @Override
      public String getName() {
         return NAME;
      }

      @JsonProperty(NODE_BLACKLIST_VAR)
      public NodeSet getNodeBlacklist() {
         return _nodeBlacklist;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @JsonProperty(EDGE_BLACKLIST_VAR)
      public void setEdgeBlacklist(Topology edgeBlacklist) {
         _edgeBlacklist = edgeBlacklist;
      }

      @JsonProperty(ENVIRONMENT_NAME_VAR)
      public void setEnvironmentName(String environmentName) {
         _environmentName = environmentName;
      }

      @JsonProperty(INTERFACE_BLACKLIST_VAR)
      public void setInterfaceBlacklist(
            SortedSet<NodeInterfacePair> blacklist) {
         _interfaceBlacklist = blacklist;
      }

      @JsonProperty(NODE_BLACKLIST_VAR)
      public void setNodeBlacklist(NodeSet blacklist) {
         _nodeBlacklist = blacklist;
      }
   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new EnvironmentCreationAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new EnvironmentCreationQuestion();
   }

}
