package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.BfConsts;
import org.batfish.common.QuestionException;
import org.batfish.datamodel.questions.Question;

public class Answer {

  public static Answer failureAnswer(String message, @Nullable Question question) {
    Answer answer = new Answer();
    answer.setQuestion(question);
    answer.setStatus(AnswerStatus.FAILURE);
    answer.addAnswerElement(new StringAnswerElement(message));
    return answer;
  }

  protected List<AnswerElement> _answerElements = new LinkedList<>();

  private Question _question;

  private AnswerStatus _status;

  private AnswerSummary _summary = new AnswerSummary();

  public void addAnswerElement(AnswerElement answerElement) {
    _answerElements.add(answerElement);
    _summary.combine(answerElement.getSummary());
  }

  public void append(Answer answer) {
    if (answer._question != null) {
      _question = answer._question;
    }
    _answerElements.addAll(answer._answerElements);
    _status = answer._status;
    _summary.combine(answer.getSummary());
    for (AnswerElement answerElement : answer._answerElements) {
      if (answerElement instanceof BatfishStackTrace) {
        BatfishException e = ((BatfishStackTrace) answerElement).getException();
        throw new QuestionException("Exception answering question", e, this);
      }
    }
  }

  @JsonProperty(BfConsts.PROP_ANSWER_ELEMENTS)
  public List<AnswerElement> getAnswerElements() {
    return _answerElements;
  }

  @JsonProperty(BfConsts.PROP_QUESTION)
  public Question getQuestion() {
    return _question;
  }

  @JsonProperty(BfConsts.PROP_SUMMARY)
  public AnswerSummary getSummary() {
    return _summary;
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public AnswerStatus getStatus() {
    return _status;
  }

  @JsonProperty(BfConsts.PROP_ANSWER_ELEMENTS)
  public void setAnswerElements(List<AnswerElement> answerElements) {
    _answerElements = answerElements;
  }

  @JsonProperty(BfConsts.PROP_QUESTION)
  public void setQuestion(Question question) {
    _question = question;
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public void setStatus(AnswerStatus status) {
    _status = status;
  }

  @JsonProperty(BfConsts.PROP_SUMMARY)
  public void setSummary(AnswerSummary summary) {
    _summary = summary;
  }
}
