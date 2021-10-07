package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
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

  @Nonnull
  public Map<ServiceGroupMember.NameAndPort, ServiceGroupMember> getMembers() {
    return ImmutableMap.copyOf(_members);
  }

  @Nonnull
  public ServiceGroupMember getOrCreateMember(String name, int port) {
    ServiceGroupMember.NameAndPort nameAndPort = new ServiceGroupMember.NameAndPort(name, port);
    ServiceGroupMember member = _members.get(nameAndPort);
    if (member == null) {
      member = new ServiceGroupMember(nameAndPort.getName(), nameAndPort.getPort());
      _members.put(nameAndPort, member);
    }
    return member;
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

  public ServiceGroup(String name, ServerPort.Type type) {
    _name = name;
    _type = type;
    _members = new HashMap();
  }

  @Nullable private String _healthCheck;
  @Nonnull private final Map<ServiceGroupMember.NameAndPort, ServiceGroupMember> _members;
  @Nonnull private final String _name;
  @Nullable private Method _method;
  @Nonnull private final ServerPort.Type _type;
  @Nullable private Boolean _statsDataEnable;
}
