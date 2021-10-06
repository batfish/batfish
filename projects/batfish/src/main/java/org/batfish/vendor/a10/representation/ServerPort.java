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

  @Nullable
  public Integer getConnLimit() {
    return _connLimit;
  }

  @Nullable
  public Boolean getEnable() {
    return _enable;
  }

  public int getNumber() {
    return _number;
  }

  @Nullable
  public String getPortTemplate() {
    return _portTemplate;
  }

  @Nullable
  public Integer getRange() {
    return _range;
  }

  @Nullable
  public Boolean getStatsDataEnable() {
    return _statsDataEnable;
  }

  @Nonnull
  public Type getType() {
    return _type;
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

  public ServerPort(int number, Type type, @Nullable Integer range) {
    _number = number;
    _type = type;
    _range = range;
  }

  @Nullable private Integer _connLimit;
  @Nullable private Boolean _enable;
  private final int _number;
  @Nullable private String _portTemplate;
  @Nullable private Integer _range;
  @Nullable private Boolean _statsDataEnable;
  @Nonnull private Type _type;
  @Nullable private Integer _weight;
}
