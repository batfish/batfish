package org.batfish.question;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.answerer.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutesQuestionPlugin extends QuestionPlugin {

   public static class RoutesAnswerer extends Answerer {

      public RoutesAnswerer(Question question, Batfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer(TestrigSettings testrigSettings) {

         RoutesQuestion question = (RoutesQuestion) _question;
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

         _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
         Map<String, Configuration> configurations = _batfish
               .loadConfigurations(testrigSettings);
         _batfish.initRoutes(configurations);
         RoutesAnswerElement answerElement = new RoutesAnswerElement(
               configurations, nodeRegex);
         return answerElement;
      }

   }

   public static class RoutesQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public RoutesQuestion() {
         super(null);
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return true;
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
   protected Answerer createAnswerer(Question question, Batfish batfish) {
      return new RoutesAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new RoutesQuestion();
   }

   @Override
   protected String getQuestionClassName() {
      return RoutesQuestion.class.getCanonicalName();
   }

   @Override
   protected String getQuestionName() {
      return "routes";
   }

}
