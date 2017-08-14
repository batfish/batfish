package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * The {@link Container Container} is an Object representation of the container for Batfish service.
 *
 * <p>{@link Container container} contains all information about the testrigs and analyses inside
 * the container {@link Container#_name}.
 */
@JsonInclude(Include.NON_NULL)
public final class Container {
  private static final String PROP_NAME = "name";
  private static final String PROP_TESTRIGS = "testrigs";
  private static final String PROP_ANALYSES = "analyses";

  private String _name;
  private @Nullable List<Testrig> _testrigs;
  private @Nullable List<Analysis> _analyses;

  public static Container of(@JsonProperty(PROP_NAME) String name) {
    return of(name, null, null);
  }

  @JsonCreator
  public static Container of(
      @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_TESTRIGS) List<Testrig> testrigs,
      @Nullable @JsonProperty(PROP_ANALYSES) List<Analysis> analyses) {
    return new Container(name, testrigs, analyses);
  }

  private Container(
      String name, @Nullable List<Testrig> testrigs, @Nullable List<Analysis> analyses) {
    this._name = name;
    this._testrigs = testrigs;
    this._analyses = analyses;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Nullable
  @JsonProperty(PROP_TESTRIGS)
  public List<Testrig> getTestrigs() {
    return _testrigs;
  }

  @Nullable
  @JsonProperty(PROP_ANALYSES)
  public List<Analysis> getAnalyses() {
    return _analyses;
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(PROP_TESTRIGS)
  public void setTestrigs(List<Testrig> testrigs) {
    _testrigs = testrigs;
  }

  @JsonProperty(PROP_ANALYSES)
  public void setAnalyses(List<Analysis> analyses) {
    _analyses = analyses;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Container.class)
        .add(PROP_NAME, _name)
        .add(PROP_TESTRIGS, _testrigs)
        .add(PROP_ANALYSES, _analyses)
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
        && Objects.equals(_analyses, other._analyses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _testrigs, _analyses);
  }
}
