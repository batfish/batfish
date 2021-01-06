package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a pool member. */
public final class PoolMember implements Serializable {

  public PoolMember(String name, String node, int port) {
    checkArgument(port > 0 && port <= 65535, "Invalid port %s", port);
    _name = name;
    _node = node;
    _port = port;
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull String getNode() {
    return _node;
  }

  public int getPort() {
    return _port;
  }

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;
  private @Nullable String _description;
  private final @Nonnull String _name;
  private final @Nonnull String _node;
  private final int _port;
}
