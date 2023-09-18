package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration for a load balancer server port. */
public class ServerPort implements Serializable {
  /** A combination of port number and type, which uniquely identifies a {@link ServerPort} */
  public static class ServerPortAndType implements Serializable {
    @Override
    public int hashCode() {
      return Objects.hash(_port, _type);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof ServerPortAndType)) {
        return false;
      }
      ServerPortAndType o = (ServerPortAndType) obj;
      return _port == o._port && _type == o._type;
    }

    public ServerPortAndType(int port, Type type) {
      _port = port;
      _type = type;
    }

    private final int _port;
    private final Type _type;
  }

  public enum Type {
    TCP,
    UDP
  }

  public @Nullable Integer getConnLimit() {
    return _connLimit;
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public int getNumber() {
    return _number;
  }

  public @Nullable String getPortTemplate() {
    return _portTemplate;
  }

  public @Nullable Integer getRange() {
    return _range;
  }

  public @Nullable Boolean getStatsDataEnable() {
    return _statsDataEnable;
  }

  public @Nonnull Type getType() {
    return _type;
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

  public void setPortTemplate(String template) {
    _portTemplate = template;
  }

  public void setRange(@Nullable Integer range) {
    _range = range;
  }

  public void setStatsDataEnable(boolean statsDataEnable) {
    _statsDataEnable = statsDataEnable;
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

  public ServerPort(int number, Type type, @Nullable Integer range) {
    _number = number;
    _type = type;
    _range = range;
  }

  private @Nullable Integer _connLimit;
  private @Nullable Boolean _enable;
  private @Nullable String _healthCheck;
  private @Nullable Boolean _healthCheckDisable;
  private final int _number;
  private @Nullable String _portTemplate;
  private @Nullable Integer _range;
  private @Nullable Boolean _statsDataEnable;
  private @Nonnull Type _type;
  private @Nullable Integer _weight;
}
