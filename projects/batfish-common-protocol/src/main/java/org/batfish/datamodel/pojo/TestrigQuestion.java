package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.answers.Answer;

/**
 * The {@link TestrigQuestion TestrigQuestion} is an Object representation of the testrig question
 * for BatFish service.
 *
 * <p>Each {@link TestrigQuestion TestrigQuestion} contains the name of the question, the question
 * content, an environment, and the answer of the question in that environment.
 */
public class TestrigQuestion {
  private static final String PROP_NAME = "name";
  private static final String PROP_QUESTION = "question";
  private static final String PROP_ENVIRONMENT = "environment";
  private static final String PROP_ANSWER = "answer";

  private final String _name;
  private final String _question;
  private final Environment _environment;
  private final Answer _answer;

  @JsonCreator
  public TestrigQuestion(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_QUESTION) String question,
      @JsonProperty(PROP_ENVIRONMENT) Environment environment,
      @JsonProperty(PROP_ANSWER) Answer answer) {
    this._name = name;
    this._question = question;
    this._environment = environment;
    this._answer = answer;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_QUESTION)
  public String getQuestion() {
    return _question;
  }

  @JsonProperty(PROP_ENVIRONMENT)
  public Environment getEnvironment() {
    return _environment;
  }

  @JsonProperty(PROP_ANSWER)
  public Answer getAnswer() {
    return _answer;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TestrigQuestion.class)
        .add(PROP_NAME, _name)
        .add(PROP_QUESTION, _question)
        .add(PROP_ENVIRONMENT, _environment)
        .add(PROP_ANSWER, _answer)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TestrigQuestion)) {
      return false;
    }
    TestrigQuestion other = (TestrigQuestion) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_question, other._question)
        && Objects.equals(_environment, other._environment)
        && Objects.equals(_answer, other._answer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _question, _environment, _answer);
  }
}
