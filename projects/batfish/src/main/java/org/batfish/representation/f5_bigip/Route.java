package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** A static route with one or more next-hop-ips on one or more directly-connected interfaces */
public final class Route implements Serializable {

  public static final long METRIC = 0L;

  private @Nullable Ip _gw;
  private @Nullable Ip6 _gw6;
  private final @Nonnull String _name;
  private @Nullable Prefix _network;
  private @Nullable Prefix6 _network6;

  public Route(String name) {
    _name = name;
  }

  public @Nullable Ip getGw() {
    return _gw;
  }

  public @Nullable Ip6 getGw6() {
    return _gw6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Prefix getNetwork() {
    return _network;
  }

  public @Nullable Prefix6 getNetwork6() {
    return _network6;
  }

  public void setGw(@Nullable Ip gw) {
    _gw = gw;
  }

  public void setGw6(@Nullable Ip6 gw6) {
    _gw6 = gw6;
  }

  public void setNetwork(@Nullable Prefix network) {
    _network = network;
  }

  public void setNetwork6(@Nullable Prefix6 network6) {
    _network6 = network6;
  }
}
