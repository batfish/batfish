package org.batfish.representation.juniper;

import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class DhcpRelayServerGroup extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final SortedSet<Ip> _servers;

  public DhcpRelayServerGroup(String name) {
    super(name);
    _servers = new TreeSet<>();
  }

  public SortedSet<Ip> getServers() {
    return _servers;
  }
}
