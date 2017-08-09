package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;

public class Testrig {

  private static final String NAME_VAR = "name";
  private static final String CONFIGS_VAR = "configs";
  private static final String ENV_VAR = "environments";
  private static final String QUESTIONS_VAR = "questions";

  private String _name;
  private Set<String> _configs;
  private Set<String> _environments;
  private Set<String> _questions;

  @JsonCreator
  public static Testrig of(@JsonProperty(NAME_VAR) String name,
      @JsonProperty(CONFIGS_VAR) Set<String> configs,
      @JsonProperty(ENV_VAR) Set<String> environments,
      @JsonProperty(QUESTIONS_VAR) Set<String> questions) {
    return new Testrig(name, configs, environments, questions);
  }

  private Testrig(String name, Set<String> configs, Set<String> environments,
      Set<String> questions) {
    this._name = name;
    this._configs = configs;
    this._environments = environments;
    this._questions = questions;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(CONFIGS_VAR)
  public Set<String> getConfigs() {
    return _configs;
  }

  @JsonProperty(ENV_VAR)
  public Set<String> getEnvironments() {
    return _environments;
  }

  @JsonProperty(QUESTIONS_VAR)
  public Set<String> getQuestions() {
    return _questions;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(CONFIGS_VAR)
  public void setConfigs(Set<String> configs) {
    _configs = configs;
  }

  @JsonProperty(ENV_VAR)
  public void setEnvironment(Set<String> environments) {
    _environments = environments;
  }

  @JsonProperty(QUESTIONS_VAR)
  public void setQuestions(Set<String> questions) {
    _questions = questions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Testrig.class)
        .add(NAME_VAR, _name)
        .add(CONFIGS_VAR, _configs).add(ENV_VAR, _environments).add(QUESTIONS_VAR, _questions)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Testrig)) {
      return false;
    }
    Testrig other = (Testrig) o;
    return Objects.equals(_name, other._name) && Objects.equals(_configs, other._configs) && Objects
        .equals(_environments, other._environments) && Objects.equals(_questions, other._questions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _configs, _environments, _questions);
  }

}
