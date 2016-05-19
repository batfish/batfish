package org.batfish.datamodel.answers;

import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.Question;

public class Answer {

   public static Answer failureAnswer(String message) {
      Answer answer = new Answer();
      answer.setStatus(AnswerStatus.FAILURE);
      answer.addAnswerElement(new StringAnswerElement(message));
      return answer;
   }

   protected List<AnswerElement> _answerElements = new LinkedList<AnswerElement>();
   private Question _question;

   private AnswerStatus _status;

   public void addAnswerElement(AnswerElement answerElement) {
      _answerElements.add(answerElement);
   }

   public void append(Answer answer) {
      if (answer._question != null) {
         _question = answer._question;
      }
      _answerElements.addAll(answer._answerElements);
      _status = answer._status;
      for (AnswerElement answerElement : answer._answerElements) {
         if (answerElement instanceof BatfishException) {
            BatfishException e = (BatfishException) answerElement;
            throw e;
         }
      }
   }

   public List<AnswerElement> getAnswerElements() {
      return _answerElements;
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
}
