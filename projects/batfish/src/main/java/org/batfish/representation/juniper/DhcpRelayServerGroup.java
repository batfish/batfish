package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.Ip;

public class DhcpRelayServerGroup implements Serializable {

  private final SortedSet<Ip> _servers;

  public DhcpRelayServerGroup() {
    _servers = new TreeSet<>();
  }

  public SortedSet<Ip> getServers() {
    return _servers;
  }
}
