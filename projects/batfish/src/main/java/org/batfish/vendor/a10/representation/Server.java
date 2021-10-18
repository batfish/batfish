package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration for a load balancer server. */
public final class Server implements Serializable {

  @Nullable
  public Integer getConnLimit() {
    return _connLimit;
  }

  @Nullable
  public Boolean getEnable() {
    return _enable;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public Map<ServerPort.ServerPortAndType, ServerPort> getPorts() {
    return _ports;
  }

  /** Returns the requested port; creates one and adds it if it doesn't already exist. */
  @Nonnull
  public ServerPort getOrCreatePort(int port, ServerPort.Type type, @Nullable Integer range) {
    return _ports.computeIfAbsent(
        new ServerPort.ServerPortAndType(port, type), pat -> new ServerPort(port, type, range));
  }

  /** Create a {@link ServerPort} and add it to the map of ports for this {@link Server}. */
  public void createPort(int port, ServerPort.Type type) {
    ServerPort.ServerPortAndType key = new ServerPort.ServerPortAndType(port, type);
    assert !_ports.containsKey(key);
    _ports.put(key, new ServerPort(port, type, null));
  }

  @Nullable
  public String getServerTemplate() {
    return _serverTemplate;
  }

  @Nullable
  public Boolean getStatsDataEnable() {
    return _statsDataEnable;
  }

  @Nonnull
  public ServerTarget getTarget() {
    return _target;
  }

  @Nullable
  public Integer getWeight() {
    return _weight;
  }

  public void setConnLimit(int connLimit) {
    _connLimit = connLimit;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public void setServerTemplate(String template) {
    _serverTemplate = template;
  }

  public void setStatsDataEnable(boolean statsDataEnable) {
    _statsDataEnable = statsDataEnable;
  }

  public void setTarget(ServerTarget target) {
    _target = target;
  }

  public void setWeight(int weight) {
    _weight = weight;
  }

  @Nullable
  public String getHealthCheck() {
    return _healthCheck;
  }

  public void setHealthCheck(String healthCheck) {
    _healthCheck = healthCheck;
  }

  @Nullable
  public Boolean getHealthCheckDisable() {
    return _healthCheckDisable;
  }

  public void setHealthCheckDisable(boolean healthCheckDisable) {
    _healthCheckDisable = healthCheckDisable;
  }

  public Server(String name, ServerTarget target) {
    _name = name;
    _ports = new HashMap<>();
    _target = target;
  }

  @Nullable private Integer _connLimit;
  @Nullable private Boolean _enable;
  @Nullable private String _healthCheck;
  @Nullable private Boolean _healthCheckDisable;
  @Nonnull private final String _name;
  @Nonnull private final Map<ServerPort.ServerPortAndType, ServerPort> _ports;
  @Nonnull private ServerTarget _target;
  @Nullable private String _serverTemplate;
  @Nullable private Boolean _statsDataEnable;
  @Nullable private Integer _weight;
}
