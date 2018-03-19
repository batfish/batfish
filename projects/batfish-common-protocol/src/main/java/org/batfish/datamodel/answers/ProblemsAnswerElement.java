package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class ProblemsAnswerElement extends AnswerElement {

  private static final String PROP_PROBLEMS = "problems";

  private SortedMap<String, Problem> _problems;

  public ProblemsAnswerElement() {
    _problems = new TreeMap<>();
  }

  @JsonProperty(PROP_PROBLEMS)
  public SortedMap<String, Problem> getProblems() {
    return _problems;
  }

  @JsonProperty(PROP_PROBLEMS)
  public void setProblems(SortedMap<String, Problem> problems) {
    _problems = problems;
  }
}
