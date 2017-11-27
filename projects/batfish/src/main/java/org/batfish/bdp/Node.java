package org.batfish.bdp;

import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Configuration;

public final class Node extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  final Configuration _c;

  SortedMap<String, VirtualRouter> _virtualRouters;

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

  public Configuration getConfiguration() {
    return _c;
  }
}
