package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Optional;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.batfish.dataplane.rib.Rib;

/** Dataplane-specific encapsulation of {@link Configuration} */
public final class Node implements Serializable {

  private final Configuration _c;

  private final SortedMap<String, VirtualRouter> _virtualRouters;

  /**
   * Create a new node based on the configuration. Initializes virtual routers based on {@link
   * Configuration} VRFs.
   *
   * @param configuration the {@link Configuration} backing this node
   */
  public Node(@Nonnull Configuration configuration) {
    _c = configuration;
    ImmutableSortedMap.Builder<String, VirtualRouter> b = ImmutableSortedMap.naturalOrder();
    for (String vrfName : _c.getVrfs().keySet()) {
      VirtualRouter vr = new VirtualRouter(vrfName, this);
      b.put(vrfName, vr);
    }
    _virtualRouters = b.build();
  }

  /** @return The {@link Configuration} backing this Node */
  @Nonnull
  public Configuration getConfiguration() {
    return _c;
  }

  /**
   * Return the virtual routers at this node
   *
   * @return Set of {@link VirtualRouter}s, keyed by VRF name
   */
  @Nonnull
  SortedMap<String, VirtualRouter> getVirtualRouters() {
    return _virtualRouters;
  }

  @Nonnull
  Optional<Rib> getRib(RibId ribId) {
    if (!_c.getHostname().equals(ribId.getHostname())) {
      return Optional.empty();
    }
    VirtualRouter vr = _virtualRouters.get(ribId.getVrfName());
    return vr == null ? Optional.empty() : vr.getRib(ribId);
  }
}
