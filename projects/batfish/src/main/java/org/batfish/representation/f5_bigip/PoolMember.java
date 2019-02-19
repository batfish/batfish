package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a pool member. */
@ParametersAreNonnullByDefault
public final class PoolMember implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;

  private final @Nonnull String _name;
  private final @Nonnull String _node;
  private final int _port;

  public PoolMember(String name, String node, int port) {
    _name = name;
    _node = node;
    _port = port;
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public int getPort() {
    return _port;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }
}
