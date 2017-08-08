package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Testrig {

  private static final String NAME_VAR = "name";
  private static final String CONFIGS_VAR = "configs";
  private static final String ENV_VAR = "environments";

  private String _name;
  private List<String> _configs;
  private Path _environment;

  @JsonCreator
  public static Testrig of(
      @JsonProperty(NAME_VAR) String name,
      @JsonProperty(CONFIGS_VAR) List<String> configs) {
    return new Testrig(name, configs);
  }

  private Testrig(String name, List<String> configs) {
    this._name = name;
    this._configs = configs;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(CONFIGS_VAR)
  public List<String> getConfigs() {
    return _configs;
  }

  @JsonProperty(ENV_VAR)
  public Path getEnvironment() {
    return _environment;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(CONFIGS_VAR)
  public void setConfigs(List<String> configs) {
    _configs = configs;
  }

  @JsonProperty(ENV_VAR)
  public void setEnvironment(Path environment) {
    _environment = environment;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Testrig.class)
        .add(NAME_VAR, _name)
        .add(CONFIGS_VAR, _configs)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Testrig)) {
      return false;
    }
    Testrig other = (Testrig) o;
    return Objects.equals(_name, other._name) && Objects.equals(_configs, other._configs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _configs);
  }

}
