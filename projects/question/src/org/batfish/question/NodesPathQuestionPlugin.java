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
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

public class NodesPathQuestionPlugin extends QuestionPlugin {

   public static class NodesPathAnswerElement implements AnswerElement {

      private Object _result;

      public Object getResult() {
         return _result;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         ObjectMapper mapper = new BatfishObjectMapper();
         StringBuilder sb = new StringBuilder();
         if (_result instanceof Iterable<?>) {
            sb.append("Result: \n[");
            Iterable<?> results = (Iterable<?>) _result;
            Iterator<?> i = results.iterator();
            while (i.hasNext()) {
               Object result = i.next();
               sb.append(mapper.writeValueAsString(result));
               if (i.hasNext()) {
                  sb.append(",");
               }
               sb.append("\n");
            }
            sb.append("]\n");
         }
         else {
            sb.append(mapper.writeValueAsString(this));
         }
         String output = sb.toString();
         return output;
      }

      public void setResult(Object result) {
         _result = result;
      }

   }

   public static class NodesPathAnswerer extends Answerer {

      public NodesPathAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         NodesPathQuestion question = (NodesPathQuestion) _question;
         String path = question.getPath();

         ConfigurationBuilder b = new ConfigurationBuilder();
         b.jsonProvider(new JacksonJsonNodeJsonProvider());
         if (question.getAsPathList()) {
            b.options(Option.AS_PATH_LIST);
         }
         Configuration c = b.build();

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
         Object jsonObject = JsonPath.parse(nodesAnswerStr, c).json();
         Object pathResult = null;
         JsonPath jsonPath = JsonPath.compile(path);

         try {
            pathResult = jsonPath.read(jsonObject, c);
         }
         catch (PathNotFoundException e) {
            pathResult = "[]";
         }
         catch (Exception e) {
            throw new BatfishException("Error reading JSON path: " + path, e);
         }
         NodesPathAnswerElement answerElement = new NodesPathAnswerElement();
         answerElement.setResult(pathResult);
         return answerElement;
      }
   }

   public static class NodesPathQuestion extends Question {

      private static final String AS_PATH_LIST_VAR = "asPathList";

      private static final String PATH_VAR = "path";

      private boolean _asPathList;

      private String _path;

      public boolean getAsPathList() {
         return _asPathList;
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "nodespath";
      }

      public String getPath() {
         return _path;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         String retString = String.format("%s %s%s=\"%s\"", getName(),
               prettyPrintBase(), PATH_VAR, _path);
         return retString;
      }

      public void setAsPathList(boolean asPathList) {
         _asPathList = asPathList;
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
               case AS_PATH_LIST_VAR:
                  setAsPathList(parameters.getBoolean(paramKey));
                  break;
               case PATH_VAR:
                  setPath(parameters.getString(paramKey));
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

      public void setPath(String path) {
         _path = path;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new NodesPathAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new NodesPathQuestion();
   }

}
