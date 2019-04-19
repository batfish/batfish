package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public final class Route implements Serializable {

  public static final long METRIC = 0L;
  private static final long serialVersionUID = 1L;

  private @Nullable Ip _gw;
  private final @Nonnull String _name;
  private @Nullable Prefix _network;

  public Route(String name) {
    _name = name;
  }

  public @Nullable Ip getGw() {
    return _gw;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Prefix getNetwork() {
    return _network;
  }

  public void setGw(@Nullable Ip gw) {
    _gw = gw;
  }

  public void setNetwork(@Nullable Prefix network) {
    _network = network;
  }
}
