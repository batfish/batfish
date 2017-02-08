package org.batfish.question;

import java.util.Iterator;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class InitInfoQuestionPlugin extends QuestionPlugin {

   public static class InitInfoAnswerer extends Answerer {

      public InitInfoAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public InitInfoAnswerElement answer() {
         InitInfoQuestion question = (InitInfoQuestion) _question;
         return _batfish.initInfo(question._summary);
      }
   }

   // <question_page_comment>
   /**
    * Outputs results of test-rig initialization.
    *
    * @type InitInfo onefile
    *
    * @example bf_answer("initinfo", summary=True") Get summary information
    *          about test-rig initialization
    */
   public static class InitInfoQuestion extends Question {

      private static final String SUMMARY_VAR = "summary";

      private boolean _summary;

      public InitInfoQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "initinfo";
      }

      @JsonProperty(SUMMARY_VAR)
      public boolean getSummary() {
         return _summary;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         return getName() + " " + SUMMARY_VAR + "=" + _summary;
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
               case SUMMARY_VAR:
                  setSummary(parameters.getBoolean(paramKey));
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

      @JsonProperty(SUMMARY_VAR)
      public void setSummary(boolean summary) {
         _summary = summary;
      }

   }

   @Override
   protected InitInfoAnswerer createAnswerer(Question question,
         IBatfish batfish) {
      return new InitInfoAnswerer(question, batfish);
   }

   @Override
   protected InitInfoQuestion createQuestion() {
      return new InitInfoQuestion();
   }

}
