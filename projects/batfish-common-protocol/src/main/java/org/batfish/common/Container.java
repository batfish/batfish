package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.Objects;

public final class Container {
  private static final String NAME_VAR = "name";
  private static final String TESTRIGS_URI_VAR = "testrigsUri";
  private static final String ANALYSIS_VAR = "analysis";

  private String _name;
  private String _testrigsUri;
  private Path _analysis;

  @JsonCreator
  public static Container makeContainer(
      @JsonProperty(NAME_VAR) String name, @JsonProperty(TESTRIGS_URI_VAR) String testrigsUrl) {
    return new Container(name, testrigsUrl);
  }

  private Container(String name, String testrigsUri) {
    this._name = name;
    this._testrigsUri = testrigsUri;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(TESTRIGS_URI_VAR)
  public String getTestrigsUri() {
    return _testrigsUri;
  }

  @JsonProperty(ANALYSIS_VAR)
  public Path getAnalysis() {
    return _analysis;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(TESTRIGS_URI_VAR)
  public void setTestrigsUri(String testrigsUri) {
    _testrigsUri = testrigsUri;
  }

  @JsonProperty(ANALYSIS_VAR)
  public void setAnalysis(Path analysis) {
    _analysis = analysis;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Container.class)
        .add(NAME_VAR, _name)
        .add(TESTRIGS_URI_VAR, _testrigsUri)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Container)) {
      return false;
    }
    Container other = (Container) o;
    return Objects.equals(_name, other._name) && Objects.equals(_testrigsUri, other._testrigsUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _testrigsUri);
  }
}
