package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.TreeMap;

public class RunAnalysisAnswerElement extends AnswerElement {
  private static final String PROP_ANSWERS = "answers";

  private SortedMap<String, Answer> _answers;

  @JsonCreator
  public RunAnalysisAnswerElement() {
    _answers = new TreeMap<>();
  }

  @JsonProperty(PROP_ANSWERS)
  public SortedMap<String, Answer> getAnswers() {
    return _answers;
  }

  @JsonProperty(PROP_ANSWERS)
  public void setAnswers(SortedMap<String, Answer> answers) {
    _answers = answers;
  }
}
