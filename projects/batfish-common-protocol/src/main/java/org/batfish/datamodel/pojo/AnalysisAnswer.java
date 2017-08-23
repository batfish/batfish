package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.answers.Answer;

/**
 * The {@link AnalysisAnswer AnalysisAnswer} is an Object representation of the analysis answer for
 * BatFish service.
 *
 * <p>Each {@link AnalysisAnswer AnalysisAnswer} contains the corresponding analysis name, an
 * environment the analysis {@link #_analysisName} were answered, and a list of <@link Answer
 * answer> as the result.
 */
public class AnalysisAnswer {
  private static final String PROP_ANALYSIS_NAME = "analysisName";
  private static final String PROP_ENVIRONMENT = "environment";
  private static final String PROP_ANSWERS = "answers";

  private final String _analysisName;
  private final Environment _environment;
  private final List<Answer> _answers;

  @JsonCreator
  public AnalysisAnswer(
      @JsonProperty(PROP_ANALYSIS_NAME) String analysisName,
      @JsonProperty(PROP_ENVIRONMENT) Environment environment,
      @JsonProperty(PROP_ANSWERS) @Nullable List<Answer> answers) {
    this._analysisName = analysisName;
    this._environment = environment;
    this._answers = answers == null ? new ArrayList<>() : answers;
  }

  @JsonProperty(PROP_ANALYSIS_NAME)
  public String getName() {
    return _analysisName;
  }

  @JsonProperty(PROP_ENVIRONMENT)
  public Environment getEnvironment() {
    return _environment;
  }

  @JsonProperty(PROP_ANSWERS)
  public List<Answer> getAnswers() {
    return _answers;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(AnalysisAnswer.class)
        .add(PROP_ANALYSIS_NAME, _analysisName)
        .add(PROP_ENVIRONMENT, _environment)
        .add(PROP_ANSWERS, _answers)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AnalysisAnswer)) {
      return false;
    }
    AnalysisAnswer other = (AnalysisAnswer) o;
    return Objects.equals(_analysisName, other._analysisName)
        && Objects.equals(_environment, other._environment)
        && Objects.equals(_answers, other._answers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_analysisName, _answers, _environment);
  }
}
