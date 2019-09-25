package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** Configuration for an OSPF routing process */
public final class OspfProcess implements Serializable {

  public OspfProcess(String name) {
    _name = name;
    _neighbors = new HashSet<>();
    _networks = new HashMap<>();
    _passiveInterfaces = new HashSet<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<Ip> getNeighbors() {
    return _neighbors;
  }

  public @Nonnull Map<Prefix, OspfNetwork> getNetworks() {
    return _networks;
  }

  public @Nonnull Set<String> getPassiveInterfaces() {
    return _passiveInterfaces;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  private final @Nonnull String _name;
  private final @Nonnull Set<Ip> _neighbors;
  private final @Nonnull Map<Prefix, OspfNetwork> _networks;
  private final @Nonnull Set<String> _passiveInterfaces;
  private @Nullable Ip _routerId;
}
