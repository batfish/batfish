package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing the configuration of a service-group member. */
public class ServiceGroupMember implements Serializable {
  /**
   * A combination of server name and port number, which uniquely identifies a {@link ServiceGroup}
   * member.
   */
  public static class NameAndPort implements Serializable {
    @Nonnull
    public String getName() {
      return _name;
    }

    public int getPort() {
      return _port;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_name, _port);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof NameAndPort)) {
        return false;
      }
      NameAndPort o = (NameAndPort) obj;
      return _port == o._port && _name.equals(o._name);
    }

    public NameAndPort(String name, int port) {
      _name = name;
      _port = port;
    }

    private final String _name;
    private final int _port;
  }

  @Nullable
  public Boolean getEnable() {
    return _enable;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public int getPort() {
    return _port;
  }

  @Nullable
  public Integer getPriority() {
    return _priority;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public void setPriority(int priority) {
    _priority = priority;
  }

  public ServiceGroupMember(String name, int port) {
    _name = name;
    _port = port;
  }

  @Nullable private Boolean _enable;
  @Nonnull final String _name;
  private final int _port;
  @Nullable private Integer _priority;
}
