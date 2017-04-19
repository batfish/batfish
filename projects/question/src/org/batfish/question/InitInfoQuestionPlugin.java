package org.batfish.question;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InitInfoQuestionPlugin extends QuestionPlugin {

   public static class InitInfoAnswerer extends Answerer {

      public InitInfoAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public InitInfoAnswerElement answer() {
         InitInfoQuestion question = (InitInfoQuestion) _question;
         return _batfish.initInfo(question._summary,
               question._environmentRoutes);
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

      private boolean _environmentRoutes;

      private boolean _summary;

      public InitInfoQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      public boolean getEnvironmentRoutes() {
         return _environmentRoutes;
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
      public String prettyPrint() {
         return getName() + " " + SUMMARY_VAR + "=" + _summary;
      }

      public void setEnvironmentRoutes(boolean environmentRoutes) {
         _environmentRoutes = environmentRoutes;
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
