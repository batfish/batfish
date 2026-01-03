package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

/**
 * The {@link Analysis Analysis} is an Object representation of the analysis for BatFish service.
 *
 * <p>Each {@link Analysis Analysis} contains a name and a mapping from question name to the Json
 * string representation of the question for questions in the Analysis.
 */
public class Analysis {
  private static final String PROP_NAME = "name";
  private static final String PROP_QUESTIONS = "questions";

  private final String _name;
  private final Map<String, String> _questions;

  @JsonCreator
  public Analysis(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_QUESTIONS) @Nullable Map<String, String> questions) {
    _name = name;
    _questions = questions == null ? new HashMap<>() : questions;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_QUESTIONS)
  public Map<String, String> getQuestions() {
    return _questions;
  }

  public void addQuestion(String questionName, String questionContent) {
    if (_questions.containsKey(questionName)) {
      throw new BatfishException(
          String.format("Question %s already exists for analysis %s", questionName, _name));
    }
    _questions.put(questionName, questionContent);
  }

  public void deleteQuestion(String questionName) {
    if (!_questions.containsKey(questionName)) {
      throw new BatfishException(
          String.format("Question %s does not exist for analysis %s", questionName, _name));
    }
    _questions.remove(questionName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Analysis.class)
        .add(PROP_NAME, _name)
        .add(PROP_QUESTIONS, _questions)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Analysis)) {
      return false;
    }
    Analysis other = (Analysis) o;
    return Objects.equals(_name, other._name) && Objects.equals(_questions, other._questions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _questions);
  }
}
