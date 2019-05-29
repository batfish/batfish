package org.batfish.representation.palo_alto;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Vsys implements Serializable {
  private static final long serialVersionUID = 1L;

  private final SortedMap<String, AddressGroup> _addressGroups;

  private final SortedMap<String, AddressObject> _addressObjects;

  private final String _name;

  // Using LinkedHashMap to preserve insertion order
  private LinkedHashMap<String, Rule> _rules;

  private final SortedMap<String, Service> _services;

  private final SortedMap<String, ServiceGroup> _serviceGroups;

  private final SortedMap<String, SortedMap<String, SyslogServer>> _syslogServerGroups;

  private final SortedMap<String, Zone> _zones;

  public Vsys(String name) {
    _name = name;
    _addressGroups = new TreeMap<>();
    _addressObjects = new TreeMap<>();
    _rules = new LinkedHashMap<>();
    _services = new TreeMap<>();
    _serviceGroups = new TreeMap<>();
    _syslogServerGroups = new TreeMap<>();
    _zones = new TreeMap<>();
  }

  /** Returns a map of address group name to {@link AddressGroup} object */
  public SortedMap<String, AddressGroup> getAddressGroups() {
    return _addressGroups;
  }

  /** Returns a map of address object name to {@link AddressObject} object */
  public SortedMap<String, AddressObject> getAddressObjects() {
    return _addressObjects;
  }

  /** Returns the name of this vsys. */
  public String getName() {
    return _name;
  }

  /** Returns a {@code LinkedHashMap} of rule name to rule for the rules in this vsys. */
  public LinkedHashMap<String, Rule> getRules() {
    return _rules;
  }

  /** Returns a map of service name to service for the services in this vsys. */
  public SortedMap<String, Service> getServices() {
    return _services;
  }

  /** Returns a map of service group name to serviceGroup for the service groups in this vsys. */
  public SortedMap<String, ServiceGroup> getServiceGroups() {
    return _serviceGroups;
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Vsys.class).add("name", _name).toString();
  }
}
