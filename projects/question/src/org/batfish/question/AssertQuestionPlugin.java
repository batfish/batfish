package org.batfish.question;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

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
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class AssertQuestionPlugin extends QuestionPlugin {

   public static class AssertAnswerElement implements AnswerElement {

      private boolean _fail;
      private SortedMap<Integer, Boolean> _results;

      public AssertAnswerElement() {
         _results = new TreeMap<>();
      }

      public boolean getFail() {
         return _fail;
      }

      public SortedMap<Integer, Boolean> getResults() {
         return _results;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setFail(boolean fail) {
         _fail = fail;
      }

      public void setResults(SortedMap<Integer, Boolean> results) {
         _results = results;
      }

   }

   public static class AssertAnswerer extends Answerer {

      public AssertAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {

         AssertQuestion question = (AssertQuestion) _question;
         List<Assertion> assertions = question.getAssertions();

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
         Object jsonObject = JsonPath.parse(nodesAnswerStr).json();

         Map<Integer, Boolean> results = new ConcurrentHashMap<>();
         List<Integer> indices = new ArrayList<>();
         for (int i = 0; i < assertions.size(); i++) {
            indices.add(i);
         }
         final boolean[] fail = new boolean[1];
         indices.parallelStream().forEach(i -> {
            Assertion assertion = assertions.get(i);
            String path = assertion.getPath();
            Check check = assertion.getCheck();
            List<Object> args = assertion.getArgs();
            Object pathResult = null;

            try {
               pathResult = JsonPath.read(jsonObject, path);
            }
            catch (PathNotFoundException e) {
               pathResult = PathResult.EMPTY;
            }
            catch (Exception e) {
               throw new BatfishException("Error reading JSON path: " + path,
                     e);
            }
            Matcher<?> matcher = check.matcher(args);
            if (matcher.matches(pathResult)) {
               results.put(i, true);
            }
            else {
               results.put(i, false);
               synchronized (fail) {
                  fail[0] = true;
               }
            }
         });
         AssertAnswerElement answerElement = new AssertAnswerElement();
         answerElement.getResults().putAll(results);
         answerElement.setFail(fail[0]);
         return answerElement;
      }
   }

   public static class Assertion {

      private List<Object> _args;

      private Check _check;

      private String _path;

      public List<Object> getArgs() {
         return _args;
      }

      public Check getCheck() {
         return _check;
      }

      public String getPath() {
         return _path;
      }

      public void setArgs(List<Object> args) {
         _args = args;
      }

      public void setCheck(Check check) {
         _check = check;
      }

      public void setPath(String path) {
         _path = path;
      }

   }

   public static class AssertQuestion extends Question {

      private static final String ASSERTIONS_VAR = "assertions";

      private List<Assertion> _assertions;

      public AssertQuestion() {
         _assertions = new ArrayList<>();
      }

      public List<Assertion> getAssertions() {
         return _assertions;
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "assert";
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         String retString = String.format("assert %sassertions=\"%s\"",
               prettyPrintBase(), _assertions.toString());
         return retString;
      }

      public void setAssertions(List<Assertion> assertions) {
         _assertions = assertions;
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
               case ASSERTIONS_VAR:
                  setAssertions(new ObjectMapper().<List<Assertion>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<List<Assertion>>() {
                        }));
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

   }

   public static enum Check {
      ABSENT,
      EQ,
      EXISTS,
      GE,
      GT,
      LE,
      LT,
      SIZE_EQ,
      SIZE_GE,
      SIZE_GT,
      SIZE_LE,
      SIZE_LT;

      public Matcher<?> matcher(List<Object> args) {
         switch (this) {
         case ABSENT: {
            return Matchers.equalTo(PathResult.EMPTY);
         }

         case EQ: {
            if (args.size() != 1) {
               throw new BatfishException("Expected only 1 arg");
            }
            Object arg = args.get(0);
            return Matchers.equalTo(arg);
         }

         case EXISTS: {
            return Matchers.not(Matchers.equalTo(PathResult.EMPTY));
         }

         case GE:
            break;
         case GT:
            break;
         case LE:
            break;
         case LT:
            break;
         case SIZE_EQ:
            break;
         case SIZE_GE:
            break;
         case SIZE_GT:
            break;
         case SIZE_LE:
            break;
         case SIZE_LT:
            break;
         default:
            break;
         }
         throw new BatfishException("Unimplemented check: '" + name() + "'");
      }
   }

   private static enum PathResult {
      EMPTY
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
