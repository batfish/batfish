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

  private final SortedMap<String, Zone> _zones;

  public Vsys(String name) {
    _name = name;
    _syslogServerGroups = new TreeMap<>();
    _zones = new TreeMap<>();
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
  SortedSet<String> getSyslogServerAddresses() {
    SortedSet<String> servers = new TreeSet<>();
    _syslogServerGroups.values().forEach(g -> g.values().forEach(s -> servers.add(s.getAddress())));
    return servers;
  }

  /** Returns a map of zone name to zone for the zones in this vsys. */
  public SortedMap<String, Zone> getZones() {
    return _zones;
  }
}
