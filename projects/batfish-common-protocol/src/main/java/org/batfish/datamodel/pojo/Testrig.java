package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * The {@link Testrig Testrig} is an Object representation of the testrig for BatFish service.
 *
 * <p>Each {@link Testrig Testrig} contains all information inside the testrig {@link #_name},
 * including answers to analysis, environments, questions been asked and their answers, raw
 * configuration files, etc.
 */
public class Testrig {
  private static final String PROP_NAME = "name";
  private static final String PROP_ANALYSIS_ANSWERS = "analysisAnswers";
  private static final String PROP_ENVIRONMENTS = "environments";
  private static final String PROP_QUESTIONS = "questions";
  private static final String PROP_CONFIGURATIONS = "configurations";

  private final String _name;
  private final List<AnalysisAnswer> _analysisAnswers;
  private final List<Environment> _environments;
  private final List<TestrigQuestion> _questions;
  private final Map<String, String> _configurations;

  @JsonCreator
  public Testrig(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_ANALYSIS_ANSWERS) @Nullable List<AnalysisAnswer> analysisAnswers,
      @JsonProperty(PROP_ENVIRONMENTS) @Nullable List<Environment> environments,
      @JsonProperty(PROP_QUESTIONS) @Nullable List<TestrigQuestion> questions,
      @JsonProperty(PROP_CONFIGURATIONS) @Nullable Map<String, String> configurations) {
    this._name = name;
    this._analysisAnswers = analysisAnswers == null ? new ArrayList<>() : analysisAnswers;
    this._environments = environments == null ? new ArrayList<>() : environments;
    this._questions = questions == null ? new ArrayList<>() : questions;
    this._configurations = configurations == null ? new HashMap<>() : configurations;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_ANALYSIS_ANSWERS)
  public List<AnalysisAnswer> getAnalysisAnswers() {
    return _analysisAnswers;
  }

  @JsonProperty(PROP_ENVIRONMENTS)
  public List<Environment> getEnvironments() {
    return _environments;
  }

  @JsonProperty(PROP_QUESTIONS)
  public List<TestrigQuestion> getQuestions() {
    return _questions;
  }

  @JsonProperty(PROP_CONFIGURATIONS)
  public Map<String, String> getConfigurations() {
    return _configurations;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Testrig.class)
        .add(PROP_NAME, _name)
        .add(PROP_ANALYSIS_ANSWERS, _analysisAnswers)
        .add(PROP_ENVIRONMENTS, _environments)
        .add(PROP_QUESTIONS, _questions)
        .add(PROP_CONFIGURATIONS, _configurations)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Testrig)) {
      return false;
    }
    Testrig other = (Testrig) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_analysisAnswers, other._analysisAnswers)
        && Objects.equals(_environments, other._environments)
        && Objects.equals(_questions, other._questions)
        && Objects.equals(_configurations, other._configurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _analysisAnswers, _environments, _questions, _configurations);
  }
}
