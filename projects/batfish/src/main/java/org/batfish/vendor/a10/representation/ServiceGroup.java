package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration of a service-group. */
public class ServiceGroup implements Serializable {
  public enum Method {
    LEAST_REQUEST,
    ROUND_ROBIN,
  }

  @Nullable
  public String getHealthCheck() {
    return _healthCheck;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  /**
   * Returns a map of {@link ServiceGroupMember.NameAndPort} (which uniquely identifies a {@link
   * ServiceGroupMember}) to {@link ServiceGroupMember}.
   */
  @Nonnull
  public Map<ServiceGroupMember.NameAndPort, ServiceGroupMember> getMembers() {
    return Collections.unmodifiableMap(_members);
  }

  /**
   * Get an existing {@link ServiceGroupMember} with the specified {@code name} and {@code port}, or
   * create and add one if it doesn't already exist.
   */
  @Nonnull
  public ServiceGroupMember getOrCreateMember(String name, int port) {
    return _members.computeIfAbsent(
        new ServiceGroupMember.NameAndPort(name, port), nap -> new ServiceGroupMember(name, port));
  }

  @Nullable
  public Method getMethod() {
    return _method;
  }

  @Nullable
  public Boolean getStatsDataEnable() {
    return _statsDataEnable;
  }

  @Nonnull
  public ServerPort.Type getType() {
    return _type;
  }

  public void setHealthCheck(String healthCheck) {
    _healthCheck = healthCheck;
  }

  public void setMethod(Method method) {
    _method = method;
  }

  public void setStatsDataEnable(boolean statsDataEnable) {
    _statsDataEnable = statsDataEnable;
  }

  @Nullable
  public Boolean getHealthCheckDisable() {
    return _healthCheckDisable;
  }

  public void setHealthCheckDisable(boolean healthCheckDisable) {
    _healthCheckDisable = healthCheckDisable;
  }

  public ServiceGroup(String name, ServerPort.Type type) {
    _name = name;
    _type = type;
    _members = new HashMap<>();
  }

  @Nullable private String _healthCheck;
  @Nullable private Boolean _healthCheckDisable;
  @Nonnull private final Map<ServiceGroupMember.NameAndPort, ServiceGroupMember> _members;
  @Nonnull private final String _name;
  @Nullable private Method _method;
  @Nonnull private final ServerPort.Type _type;
  @Nullable private Boolean _statsDataEnable;
}
