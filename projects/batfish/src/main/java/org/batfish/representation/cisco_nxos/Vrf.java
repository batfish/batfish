package org.batfish.representation.cisco_nxos;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** A virtual routing and forwarding instance. */
public final class Vrf implements Serializable {

  private final @Nonnull String _name;
  private boolean _shutdown;
  private final Multimap<Prefix, StaticRoute> _staticRoutes;

  public Vrf(String name) {
    _name = name;
    _staticRoutes = HashMultimap.create();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public @Nonnull Multimap<Prefix, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }
}
