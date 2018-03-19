package org.batfish.datamodel.answers;

import java.util.ArrayList;
import java.util.List;

public class ReportAnswerElement extends AnswerElement {

  private List<Object> _jsonAnswers;

  public ReportAnswerElement() {
    _jsonAnswers = new ArrayList<>();
  }

  public List<Object> getJsonAnswers() {
    return _jsonAnswers;
  }

  public void setJsonAnswers(List<Object> jsonAnswers) {
    _jsonAnswers = jsonAnswers;
  }
}
