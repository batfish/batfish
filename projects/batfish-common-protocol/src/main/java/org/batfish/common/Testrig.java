package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;

public class Testrig {

  private static final String NAME_VAR = "name";
  private static final String CONFIGS_URI_VAR = "configsUri";
  private static final String HOSTS_URI_VAR = "hostsUri";
  private static final String ENV_VAR = "environments";

  private String _name;
  private String _configsUri;
  private String _hostsUri;
  private Path _environment;

  @JsonCreator
  public static Testrig makeTestrig(
      @JsonProperty(NAME_VAR) String name,
      @JsonProperty(CONFIGS_URI_VAR) String configsUri,
      @JsonProperty(HOSTS_URI_VAR) String hostsUri) {
    return new Testrig(name, configsUri, hostsUri);
  }

  private Testrig(String name, String configsUri, String hostsUri) {
    this._name = name;
    this._configsUri = configsUri;
    this._hostsUri = hostsUri;
  }

  @JsonProperty(NAME_VAR)
  public String getName() {
    return _name;
  }

  @JsonProperty(CONFIGS_URI_VAR)
  public String getConfigsUri() {
    return _configsUri;
  }

  @JsonProperty(HOSTS_URI_VAR)
  public String get_hostsUri() {
    return _hostsUri;
  }

  @JsonProperty(ENV_VAR)
  public Path getEnvironment() {
    return _environment;
  }

  @JsonProperty(NAME_VAR)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(CONFIGS_URI_VAR)
  public void setConfigsUri(String configsUri) {
    _configsUri = configsUri;
  }

  @JsonProperty(HOSTS_URI_VAR)
  public void setHostsUri(String hostsUri) {
    _hostsUri = hostsUri;
  }

  @JsonProperty(ENV_VAR)
  public void setEnvironment(Path environment) {
    _environment = environment;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Testrig testrig = (Testrig) o;

    if (!_name.equals(testrig._name)) {
      return false;
    }
    if (!_configsUri.equals(testrig._configsUri)) {
      return false;
    }
    if (!_hostsUri.equals(testrig._hostsUri)) {
      return false;
    }
    return _environment != null
        ? _environment.equals(testrig._environment)
        : testrig._environment == null;
  }

  @Override public int hashCode() {
    int result = _name.hashCode();
    result = 31 * result + _configsUri.hashCode();
    result = 31 * result + _hostsUri.hashCode();
    result = 31 * result + (_environment != null ? _environment.hashCode() : 0);
    return result;
  }
}
