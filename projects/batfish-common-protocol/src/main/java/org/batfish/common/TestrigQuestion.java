package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.questions.Question;

public class TestrigQuestion {
  private static final String NAME_VAR = "name";
  private static final String ENV_VAR = "environment";
  private static final String QUESTION_VAR = "question";

  private String _name;
  private String _environment;
  private Question _question;

  @JsonCreator
  public static TestrigQuestion of(
      @JsonProperty(NAME_VAR) String name,
      @JsonProperty(ENV_VAR) String environment,
      @JsonProperty(QUESTION_VAR) Question question) {
    return new TestrigQuestion(name, environment, question);
  }

  private TestrigQuestion(String name, String environment, Question question) {
    this._name = name;
    this._environment = environment;
    this._question = question;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(ENV_VAR)
  public String getEnvironment() {
    return _environment;
  }

  @JsonProperty(QUESTION_VAR)
  public Question getQuestion() {
    return _question;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(ENV_VAR)
  public void setEnvironment(String environment) {
    _environment = environment;
  }

  @JsonProperty(QUESTION_VAR)
  public void setQuestion(Question question) {
    _question = question;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TestrigQuestion.class)
        .add(NAME_VAR, _name)
        .add(ENV_VAR, _environment)
        .add(QUESTION_VAR, _question)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TestrigQuestion)) {
      return false;
    }
    TestrigQuestion other = (TestrigQuestion) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_environment, other._environment)
        && Objects.equals(_question, other._question);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _environment, _question);
  }
}
