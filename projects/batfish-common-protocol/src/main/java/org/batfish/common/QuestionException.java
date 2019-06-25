package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.batfish.datamodel.answers.Answer;

public class QuestionException extends BatfishException {

  private final Answer _answer;

  public QuestionException(String msg, Throwable cause, Answer answer) {
    super(msg, cause);
    _answer = answer;
  }

  @JsonIgnore
  public Answer getAnswer() {
    return _answer;
  }
}
