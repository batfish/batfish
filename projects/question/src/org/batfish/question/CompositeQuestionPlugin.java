package org.batfish.question;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompositeQuestionPlugin extends QuestionPlugin {

   public static class CompositeAnswerElement implements AnswerElement {

      private static final String ANSWERS_VAR = "answers";

      private List<AnswerElement> _answers;

      public CompositeAnswerElement() {
         _answers = new ArrayList<>();
      }

      @JsonProperty(ANSWERS_VAR)
      public List<AnswerElement> getAnswers() {
         return _answers;
      }

      @JsonProperty(ANSWERS_VAR)
      public void setAnswers(List<AnswerElement> answers) {
         _answers = answers;
      }

   }

   public static class CompositeAnswerer extends Answerer {

      public CompositeAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public CompositeAnswerElement answer() {
         CompositeQuestion question = (CompositeQuestion) _question;
         CompositeAnswerElement answerElement = new CompositeAnswerElement();
         for (Question innerQuestion : question._questions) {
            String innerQuestionName = innerQuestion.getName();
            Answerer innerAnswerer = _batfish.getAnswererCreators()
                  .get(innerQuestionName).apply(innerQuestion, _batfish);
            AnswerElement innerAnswer = innerAnswerer.answer();
            answerElement._answers.add(innerAnswer);
         }
         return answerElement;
      }
   }

   public static class CompositeQuestion extends Question {

      private static final String QUESTIONS_VAR = "questions";

      private List<Question> _questions;

      public CompositeQuestion() {
         _questions = new ArrayList<>();
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "composite";
      }

      @JsonProperty(QUESTIONS_VAR)
      public List<Question> getQuestions() {
         return _questions;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @JsonProperty(QUESTIONS_VAR)
      public void setQuestions(List<Question> questions) {
         _questions = questions;
      }

   }

   @Override
   protected CompositeAnswerer createAnswerer(Question question,
         IBatfish batfish) {
      return new CompositeAnswerer(question, batfish);
   }

   @Override
   protected CompositeQuestion createQuestion() {
      return new CompositeQuestion();
   }

}
