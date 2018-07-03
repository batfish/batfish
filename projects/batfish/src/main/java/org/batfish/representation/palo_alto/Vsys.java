package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Vsys implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String _name;

  private final SortedMap<String, SortedMap<String, SyslogServer>> _syslogServerGroups;

  public Vsys(String name) {
    _name = name;
    _syslogServerGroups = new TreeMap<>();
  }

  /** Returns the name of this vsys. */
  public String getName() {
    return _name;
  }

  /**
   * Returns a syslog server with the specified name in the specified server group. If a matching
   * server does not exist, one is created.
   */
  public SyslogServer getSyslogServer(String serverGroupName, String serverName) {
    SortedMap<String, SyslogServer> serverGroup =
        _syslogServerGroups.computeIfAbsent(serverGroupName, g -> new TreeMap<>());
    return serverGroup.computeIfAbsent(serverName, SyslogServer::new);
  }

  /** Returns a list of all syslog server addresses. */
  public SortedSet<String> getSyslogServerAddresses() {
    SortedSet<String> servers = new TreeSet<>();
    _syslogServerGroups.values().forEach(g -> g.values().forEach(s -> servers.add(s.getAddress())));
    return servers;
  }
}
