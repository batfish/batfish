package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.Objects;
import java.util.SortedSet;

public final class Container {
  private static final String PROP_NAME = "name";
  private static final String PROP_TESTRIGS = "testrigs";
  private static final String PROP_ANALYSIS = "analysis";

  private String _name;
  private SortedSet<String> _testrigs;
  private Path _analysis;

  @JsonCreator
  public static Container of(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_TESTRIGS) SortedSet<String> testrigs) {
    return new Container(name, testrigs);
  }

  private Container(String name, SortedSet<String> testrigs) {
    _name = name;
    _testrigs = testrigs;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_TESTRIGS)
  public SortedSet<String> getTestrigs() {
    return _testrigs;
  }

  @JsonProperty(PROP_ANALYSIS)
  public Path getAnalysis() {
    return _analysis;
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(PROP_TESTRIGS)
  public void setTestrigs(SortedSet<String> testrigs) {
    _testrigs = testrigs;
  }

  @JsonProperty(PROP_ANALYSIS)
  public void setAnalysis(Path analysis) {
    _analysis = analysis;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Container.class)
        .add(PROP_NAME, _name)
        .add(PROP_TESTRIGS, _testrigs)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Container)) {
      return false;
    }
    Container other = (Container) o;
    return Objects.equals(_name, other._name) && Objects.equals(_testrigs, other._testrigs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _testrigs);
  }
}
