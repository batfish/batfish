package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;

public final class Container {
  private static final String NAME_VAR = "name";
  private static final String TESTRIGS_VAR = "testrigs";
  private static final String ANALYSIS_VAR = "analysis";

  private String _name;
  private Set<String> _testrigs;
  private Set<String> _analysis;

  @JsonCreator
  public static Container of(
      @JsonProperty(NAME_VAR) String name,
      @JsonProperty(TESTRIGS_VAR) Set<String> testrigs,
      @JsonProperty(ANALYSIS_VAR) Set<String> analysis) {
    return new Container(name, testrigs, analysis);
  }

  private Container(String name, Set<String> testrigs, Set<String> analysis) {
    this._name = name;
    this._testrigs = testrigs;
    this._analysis = analysis;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(TESTRIGS_VAR)
  public Set<String> getTestrigs() {
    return _testrigs;
  }

  @JsonProperty(ANALYSIS_VAR)
  public Set<String> getAnalysis() {
    return _analysis;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(TESTRIGS_VAR)
  public void setTestrigs(Set<String> testrigs) {
    _testrigs = testrigs;
  }

  @JsonProperty(ANALYSIS_VAR)
  public void setAnalysis(Set<String> analysis) {
    _analysis = analysis;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Container.class)
        .add(NAME_VAR, _name)
        .add(TESTRIGS_VAR, _testrigs)
        .add(ANALYSIS_VAR, _analysis)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Container)) {
      return false;
    }
    Container other = (Container) o;
    return Objects.equals(_name, other._name)
        && Objects.equals(_testrigs, other._testrigs)
        && Objects.equals(_analysis, other._analysis);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _testrigs, _analysis);
  }
}
