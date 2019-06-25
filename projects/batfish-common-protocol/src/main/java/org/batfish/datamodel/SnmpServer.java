package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public class SnmpServer implements Serializable {

  private SortedMap<String, SnmpCommunity> _communities;

  private SortedMap<String, SnmpHost> _hosts;

  private SortedMap<String, SortedSet<String>> _traps;

  private String _vrf;

  public SnmpServer() {
    _communities = new TreeMap<>();
    _hosts = new TreeMap<>();
    _traps = new TreeMap<>();
  }

  public SortedMap<String, SnmpCommunity> getCommunities() {
    return _communities;
  }

  public SortedMap<String, SnmpHost> getHosts() {
    return _hosts;
  }

  public SortedMap<String, SortedSet<String>> getTraps() {
    return _traps;
  }

  public String getVrf() {
    return _vrf;
  }

  public void setCommunities(SortedMap<String, SnmpCommunity> communities) {
    _communities = communities;
  }

  public void setHosts(SortedMap<String, SnmpHost> hosts) {
    _hosts = hosts;
  }

  public void setTraps(SortedMap<String, SortedSet<String>> traps) {
    _traps = traps;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
