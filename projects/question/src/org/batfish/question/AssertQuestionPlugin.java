package org.batfish.question;

import java.io.IOException;
import java.util.Iterator;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.NodesQuestionPlugin.NodesAnswerer;
import org.batfish.question.NodesQuestionPlugin.NodesQuestion;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class AssertQuestionPlugin extends QuestionPlugin {

   public static class AssertAnswerElement implements AnswerElement {

      private Object _answer;

      public AssertAnswerElement() {
      }

      public Object getAnswer() {
         return _answer;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setAnswer(Object answer) {
         _answer = answer;
      }

   }

   public static class AssertAnswerer extends Answerer {

      public AssertAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {

         AssertQuestion question = (AssertQuestion) _question;
         String query = question.getQuery();

         _batfish.checkConfigurations();

         NodesQuestion nodesQuestion = new NodesQuestion();
         nodesQuestion.setSummary(false);
         NodesAnswerer nodesAnswerer = new NodesAnswerer(nodesQuestion,
               _batfish);
         AnswerElement nodesAnswer = nodesAnswerer.answer();
         BatfishObjectMapper mapper = new BatfishObjectMapper();
         String nodesAnswerStr = null;
         try {
            nodesAnswerStr = mapper.writeValueAsString(nodesAnswer);
         }
         catch (IOException e) {
            throw new BatfishException(
                  "Could not get JSON string from nodes answer", e);
         }
         Object answer = JsonPath.read(nodesAnswerStr, query);
         AssertAnswerElement answerElement = new AssertAnswerElement();
         answerElement.setAnswer(answer);
         return answerElement;
      }
   }

   public static class AssertQuestion extends Question {

      private static final String QUERY_VAR = "query";

      private String _query;

      public AssertQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "assert";
      }

      public String getQuery() {
         return _query;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         String retString = String.format("assert %squery=\"%s\"",
               prettyPrintBase(), _query);
         return retString;
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
               case QUERY_VAR:
                  setQuery(parameters.getString(paramKey));
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

      public void setQuery(String query) {
         _query = query;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new AssertAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new AssertQuestion();
   }

}
