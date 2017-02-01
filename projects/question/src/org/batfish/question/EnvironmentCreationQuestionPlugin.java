package org.batfish.question;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.IEnvironmentCreationQuestion;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
               dp);
      }

   }

   /**
    * Since this is not really a question; we do not document it as such.
    */
   public static class EnvironmentCreationQuestion extends Question
         implements IEnvironmentCreationQuestion {

      private static final String ENVIRONMENT_NAME_VAR = ENVIRONMENT_NAME_KEY;
      private static final String INTERFACE_BLACKLIST_VAR = "interfaceBlacklist";
      private static final String NODE_BLACKLIST_VAR = "nodeBlacklist";

      private String _environmentName;
      private Set<NodeInterfacePair> _interfaceBlacklist;
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

      @JsonProperty(ENVIRONMENT_NAME_VAR)
      public String getEnvironmentName() {
         return _environmentName;
      }

      @JsonProperty(INTERFACE_BLACKLIST_VAR)
      public Set<NodeInterfacePair> getInterfaceBlacklist() {
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

      private void setEnvironmentName(String environmentName) {
         _environmentName = environmentName;
      }

      private void setInterfaceBlacklist(Set<NodeInterfacePair> blacklist) {
         _interfaceBlacklist = blacklist;
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
               case ENVIRONMENT_NAME_VAR:
                  setEnvironmentName(parameters.getString(paramKey));
                  break;
               case INTERFACE_BLACKLIST_VAR:
                  setInterfaceBlacklist(
                        new ObjectMapper().<Set<NodeInterfacePair>> readValue(
                              parameters.getString(paramKey),
                              new TypeReference<Set<NodeInterfacePair>>() {
                              }));
                  break;
               case NODE_BLACKLIST_VAR:
                  setNodeBlacklist(new ObjectMapper().readValue(
                        parameters.getString(paramKey), NodeSet.class));
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

      private void setNodeBlacklist(NodeSet blacklist) {
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
