package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class DhcpRelayGroup implements Serializable {

  public static final String MASTER_DHCP_RELAY_GROUP_NAME = "~MASTER_DHCP_RELAY_GROUP~";

  private String _activeServerGroup;

  private boolean _allInterfaces;

  private SortedSet<String> _interfaces;

  public DhcpRelayGroup() {
    _interfaces = new TreeSet<>();
  }

  public String getActiveServerGroup() {
    return _activeServerGroup;
  }

  public boolean getAllInterfaces() {
    return _allInterfaces;
  }

  public SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  public void setActiveServerGroup(String activeServerGroup) {
    _activeServerGroup = activeServerGroup;
  }

  public void setAllInterfaces(boolean allInterfaces) {
    _allInterfaces = allInterfaces;
  }

  public void setInterfaces(SortedSet<String> interfaces) {
    _interfaces = interfaces;
  }
}
