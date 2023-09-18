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
    LEAST_CONNECTION,
    LEAST_REQUEST,
    ROUND_ROBIN,
    ROUND_ROBIN_STRICT,
    SERVICE_LEAST_CONNECTION,
  }

  public @Nullable String getHealthCheck() {
    return _healthCheck;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /**
   * Returns a map of {@link ServiceGroupMember.NameAndPort} (which uniquely identifies a {@link
   * ServiceGroupMember}) to {@link ServiceGroupMember}.
   */
  public @Nonnull Map<ServiceGroupMember.NameAndPort, ServiceGroupMember> getMembers() {
    return Collections.unmodifiableMap(_members);
  }

  /**
   * Get an existing {@link ServiceGroupMember} with the specified {@code name} and {@code port}, or
   * create and add one if it doesn't already exist.
   */
  public @Nonnull ServiceGroupMember getOrCreateMember(String name, int port) {
    return _members.computeIfAbsent(
        new ServiceGroupMember.NameAndPort(name, port), nap -> new ServiceGroupMember(name, port));
  }

  public @Nullable Method getMethod() {
    return _method;
  }

  public @Nullable Boolean getStatsDataEnable() {
    return _statsDataEnable;
  }

  public @Nonnull ServerPort.Type getType() {
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

  public @Nullable Boolean getHealthCheckDisable() {
    return _healthCheckDisable;
  }

  public void setHealthCheckDisable(boolean healthCheckDisable) {
    _healthCheckDisable = healthCheckDisable;
  }

  public @Nullable Integer getMinActiveMember() {
    return _minActiveMember;
  }

  public void setMinActiveMember(@Nullable Integer minActiveMember) {
    _minActiveMember = minActiveMember;
  }

  public @Nullable String getTemplatePort() {
    return _templatePort;
  }

  public void setTemplatePort(@Nullable String templatePort) {
    _templatePort = templatePort;
  }

  public ServiceGroup(String name, ServerPort.Type type) {
    _name = name;
    _type = type;
    _members = new HashMap<>();
  }

  private @Nullable String _healthCheck;
  private @Nullable Boolean _healthCheckDisable;
  private final @Nonnull Map<ServiceGroupMember.NameAndPort, ServiceGroupMember> _members;
  private final @Nonnull String _name;
  private @Nullable Method _method;
  private @Nullable Integer _minActiveMember;
  private final @Nonnull ServerPort.Type _type;
  private @Nullable Boolean _statsDataEnable;
  private @Nullable String _templatePort;
}
