package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;

/** A virtual routing and forwarding instance */
public class Vrf implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull List<InterfaceAddress> _addresses;
  private final @Nonnull String _name;
  private final @Nonnull Set<StaticRoute> _staticRoutes;
  private @Nullable Integer _vni;

  public Vrf(String name) {
    _name = name;
    _addresses = new LinkedList<>();
    _staticRoutes = new HashSet<>();
  }

  public @Nonnull List<InterfaceAddress> getAddresses() {
    return _addresses;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public @Nullable Integer getVni() {
    return _vni;
  }

  public void setVni(@Nullable Integer vni) {
    _vni = vni;
  }
}
