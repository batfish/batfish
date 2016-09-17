package org.batfish.question;

import java.util.Collections;
import java.util.Iterator;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerElement;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerer;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameQuestion;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AclReachabilityQuestionPlugin extends QuestionPlugin {

   public static class AclReachabilityAnswerer extends Answerer {

      public AclReachabilityAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         AclReachabilityQuestion question = (AclReachabilityQuestion) _question;
         // get comparesamename results for acls
         CompareSameNameQuestion csnQuestion = new CompareSameNameQuestion();
         csnQuestion.setNodeRegex(question.getNodeRegex());
         csnQuestion.setNamedStructTypes(
               Collections.singleton(IpAccessList.class.getSimpleName()));
         CompareSameNameAnswerer csnAnswerer = new CompareSameNameAnswerer(
               csnQuestion, _batfish);
         CompareSameNameAnswerElement csnAnswer = (CompareSameNameAnswerElement) csnAnswerer
               .answer();
         NamedStructureEquivalenceSets<?> aclEqSets = csnAnswer
               .getEquivalenceSets().get(IpAccessList.class.getSimpleName());

         return _batfish.answerAclReachability(question.getAclNameRegex(),
               aclEqSets);
      }

   }

   public static class AclReachabilityQuestion extends Question {

      private static final String ACL_NAME_REGEX_VAR = "aclNameRegex";

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _aclNameRegex;

      private String _nodeRegex;

      public AclReachabilityQuestion() {
         _nodeRegex = ".*";
         _aclNameRegex = ".*";
      }

      @JsonProperty(ACL_NAME_REGEX_VAR)
      public String getAclNameRegex() {
         return _aclNameRegex;
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "aclreachability";
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      public void setAclNameRegex(String regex) {
         _aclNameRegex = regex;
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
               case ACL_NAME_REGEX_VAR:
                  setAclNameRegex(parameters.getString(paramKey));
                  break;
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

      public void setNodeRegex(String regex) {
         _nodeRegex = regex;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new AclReachabilityAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new AclReachabilityQuestion();
   }

}
