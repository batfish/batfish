package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

@JsonInclude(Include.NON_NULL)
public class Testrig {

  private static final String PROP_NAME = "name";
  private static final String PROP_CONFIGS = "configs";
  private static final String PROP_ENV = "environments";
  private static final String PROP_QUESTIONS = "questions";

  private String _name;
  private @Nullable List<String> _configs;
  private @Nullable List<String> _environments;
  private @Nullable List<TestrigQuestion> _questions;

  public static Testrig of(@JsonProperty(PROP_NAME) String name) {
    return of(name, null, null, null);
  }

  @JsonCreator
  public static Testrig of(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_CONFIGS) List<String> configs,
      @JsonProperty(PROP_ENV) List<String> environments,
      @JsonProperty(PROP_QUESTIONS) List<TestrigQuestion> questions) {
    return new Testrig(name, configs, environments, questions);
  }

  private Testrig(
      String name,
      List<String> configs,
      List<String> environments,
      List<TestrigQuestion> questions) {
    this._name = name;
    this._configs = configs;
    this._environments = environments;
    this._questions = questions;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_CONFIGS)
  public List<String> getConfigs() {
    return _configs;
  }

  @JsonProperty(PROP_ENV)
  public List<String> getEnvironments() {
    return _environments;
  }

  @JsonProperty(PROP_QUESTIONS)
  public List<TestrigQuestion> getQuestions() {
    return _questions;
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(PROP_CONFIGS)
  public void setConfigs(List<String> configs) {
    _configs = configs;
  }

  @JsonProperty(PROP_ENV)
  public void setEnvironment(List<String> environments) {
    _environments = environments;
  }

  @JsonProperty(PROP_QUESTIONS)
  public void setQuestions(List<TestrigQuestion> questions) {
    _questions = questions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Testrig.class)
        .add(PROP_NAME, _name)
        .add(PROP_CONFIGS, _configs)
        .add(PROP_ENV, _environments)
        .add(PROP_QUESTIONS, _questions)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Testrig)) {
      return false;
    }
    Testrig other = (Testrig) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_configs, other._configs)
        && Objects.equals(_environments, other._environments)
        && Objects.equals(_questions, other._questions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _configs, _environments, _questions);
  }

}
