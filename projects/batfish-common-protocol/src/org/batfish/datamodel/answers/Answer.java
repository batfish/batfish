package org.batfish.datamodel.answers;

import java.util.LinkedList;
import java.util.List;

import org.batfish.datamodel.questions.Question;

public class Answer {

   private Question _question;
   private AnswerStatus _status;
   private List<AnswerElement> _answerElements = new LinkedList<AnswerElement>();
   
   public List<AnswerElement> getAnswerElements() {
      return _answerElements;
   }

   public void addAnswerElement(StringAnswer stringAnswer) {
      _answerElements.add(stringAnswer);
   }   

   public Question getQuestion() {
      return _question;
   }

   public AnswerStatus getStatus() {
      return _status;
   }

   public void setAnswerElements(List<AnswerElement> answerElements) {
      _answerElements = answerElements;
   }

   public void setQuestion(Question question) {
      _question = question;
   }

   public void setStatus(AnswerStatus status) {
      _status = status;
   }

   public static Answer failureAnswer(String message) {
      Answer answer = new Answer();
      answer.setStatus(AnswerStatus.FAILURE);
      answer.addAnswerElement(new StringAnswer(message));
      return answer;
   }
}
