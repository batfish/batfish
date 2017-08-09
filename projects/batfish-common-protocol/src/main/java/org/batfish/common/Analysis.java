package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;

public class Analysis {
  private static final String NAME_VAR = "name";
  private static final String QUESTIONS_VAR = "questions";

  private String _name;
  private Set<String> _questions;

  @JsonCreator
  public static Analysis of(
      @JsonProperty(NAME_VAR) String name,
      @JsonProperty(QUESTIONS_VAR) Set<String> questions) {
    return new Analysis(name, questions);
  }

  private Analysis(String name, Set<String> questions) {
    this._name = name;
    this._questions = questions;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(QUESTIONS_VAR)
  public Set<String> getQuestions() {
    return _questions;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(QUESTIONS_VAR)
  public void setQuestions(Set<String> questions) {
    _questions = questions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Analysis.class)
        .add(NAME_VAR, _name)
        .add(QUESTIONS_VAR, _questions)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Analysis)) {
      return false;
    }
    Analysis other = (Analysis) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_questions, other._questions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _questions);
  }
}
