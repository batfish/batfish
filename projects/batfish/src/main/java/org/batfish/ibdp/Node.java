package org.batfish.ibdp;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Configuration;

public final class Node extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  final Configuration _c;

  SortedMap<String, VirtualRouter> _virtualRouters;

  /**
   * Create a new node based on the configuration. Initializes virtual routers based on {@link
   * Configuration} VRFs.
   *
   * @param c the {@link Configuration} backing this node
   */
  public Node(Configuration c) {
    super(c.getHostname());
    _c = c;
    _virtualRouters = new TreeMap<>();
    for (String vrfName : _c.getVrfs().keySet()) {
      VirtualRouter vr = new VirtualRouter(vrfName, _c);
      vr.initRibs();
      _virtualRouters.put(vrfName, vr);
    }
  }

  /** @return The {@link Configuration} backing this Node */
  public Configuration getConfiguration() {
    return _c;
  }

  SortedMap<String, VirtualRouter> getVirtualRouters() {
    return ImmutableSortedMap.copyOf(_virtualRouters);
  }
}
