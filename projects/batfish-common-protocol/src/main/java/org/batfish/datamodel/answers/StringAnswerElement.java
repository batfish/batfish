package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonInclude;

public class StringAnswerElement extends AnswerElement {

  private String _answer;

  public StringAnswerElement() {}

  public StringAnswerElement(String answer) {
    this();
    setAnswer(answer);
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getAnswer() {
    return _answer;
  }

  @Override
  public String prettyPrint() {
    return _answer;
  }

  public void setAnswer(String answer) {
    this._answer = answer;
  }
}
