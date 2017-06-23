package org.batfish.datamodel.answers;

import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.QuestionException;
import org.batfish.datamodel.questions.Question;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Answer {

   public static Answer failureAnswer(String message, Question question) {
      Answer answer = new Answer();
      answer.setQuestion(question);
      answer.setStatus(AnswerStatus.FAILURE);
      answer.addAnswerElement(new StringAnswerElement(message));
      return answer;
   }

   protected List<AnswerElement> _answerElements = new LinkedList<>();

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
         if (answerElement instanceof BatfishStackTrace) {
            BatfishException e = ((BatfishStackTrace) answerElement)
                  .getException();
            throw new QuestionException("Exception answering question", e,
                  this);
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

   public String prettyPrint() {
      StringBuilder string = new StringBuilder();
      if (_status != null) {
         string.append("Status: " + _status + "\n");
      }
      if (_question != null) {
         string.append("Question: " + _question.prettyPrint() + "\n");
      }
      for (AnswerElement ae : _answerElements) {
         string.append(ae.prettyPrint() + "\n");
      }
      return string.toString();
   }

   public Answer prettyPrintAnswer() throws JsonProcessingException {
      Answer answer = new Answer();
      answer.setQuestion(_question);
      answer.setStatus(_status);

      for (AnswerElement ae : _answerElements) {
         String aePrettyStr = ae.prettyPrint();
         AnswerElement prettyAnswerElement = new StringAnswerElement(
               aePrettyStr);
         answer.addAnswerElement(prettyAnswerElement);
      }

      return answer;
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
