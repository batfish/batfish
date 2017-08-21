package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.pojo.Analysis;

/**
 * The {@link Container Container} is an Object representation of the container for BatFish service.
 *
 * <p>Each {@link Container Container} contains a name, a list of testrigs, and a mapping from
 * analysis names to {@link Analysis Analysis} inside the container.
 */
public final class Container {
  private static final String PROP_NAME = "name";
  private static final String PROP_TESTRIGS = "testrigs";
  private static final String PROP_ANALYSES = "analyses";

  private final String _name;
  private final List<String> _testrigs;
  private final Map<String, Analysis> _analyses;

  @JsonCreator
  public Container(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_TESTRIGS) @Nullable List<String> testrigs,
      @JsonProperty(PROP_ANALYSES) @Nullable Map<String, Analysis> analyses) {
    this._name = name;
    this._testrigs = testrigs == null ? new ArrayList<>() : testrigs;
    this._analyses = analyses == null ? new HashMap<>() : analyses;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_TESTRIGS)
  public List<String> getTestrigs() {
    return _testrigs;
  }

  @JsonProperty(PROP_ANALYSES)
  public Map<String, Analysis> getAnalyses() {
    return _analyses;
  }

  public void addAnalysis(String analysisName, @Nullable Analysis analysis) {
    if (_analyses.containsKey(analysisName)) {
      throw new BatfishException(
          String.format("Analysis %s already exists for container %s", analysisName, _name));
    }
    _analyses.put(analysisName, analysis);
  }

  public void deleteAnalysis(String analysisName) {
    if (!_analyses.containsKey(analysisName)) {
      throw new BatfishException(
          String.format("Analysis %s does not exist for container %s", analysisName, _name));
    }
    _analyses.remove(analysisName);
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
