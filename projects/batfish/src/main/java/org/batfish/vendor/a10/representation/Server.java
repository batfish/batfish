package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration for a load balancer server. */
public final class Server implements Serializable {

  public @Nullable Integer getConnLimit() {
    return _connLimit;
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<ServerPort.ServerPortAndType, ServerPort> getPorts() {
    return _ports;
  }

  /** Returns the requested port; creates one and adds it if it doesn't already exist. */
  public @Nonnull ServerPort getOrCreatePort(
      int port, ServerPort.Type type, @Nullable Integer range) {
    return _ports.computeIfAbsent(
        new ServerPort.ServerPortAndType(port, type), pat -> new ServerPort(port, type, range));
  }

  /** Create a {@link ServerPort} and add it to the map of ports for this {@link Server}. */
  public void createPort(int port, ServerPort.Type type) {
    ServerPort.ServerPortAndType key = new ServerPort.ServerPortAndType(port, type);
    assert !_ports.containsKey(key);
    _ports.put(key, new ServerPort(port, type, null));
  }

  public @Nullable String getServerTemplate() {
    return _serverTemplate;
  }

  public @Nullable Boolean getStatsDataEnable() {
    return _statsDataEnable;
  }

  public @Nonnull ServerTarget getTarget() {
    return _target;
  }

  public @Nullable Integer getWeight() {
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

  public @Nullable String getHealthCheck() {
    return _healthCheck;
  }

  public void setHealthCheck(String healthCheck) {
    _healthCheck = healthCheck;
  }

  public @Nullable Boolean getHealthCheckDisable() {
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

  private @Nullable Integer _connLimit;
  private @Nullable Boolean _enable;
  private @Nullable String _healthCheck;
  private @Nullable Boolean _healthCheckDisable;
  private final @Nonnull String _name;
  private final @Nonnull Map<ServerPort.ServerPortAndType, ServerPort> _ports;
  private @Nonnull ServerTarget _target;
  private @Nullable String _serverTemplate;
  private @Nullable Boolean _statsDataEnable;
  private @Nullable Integer _weight;
}
