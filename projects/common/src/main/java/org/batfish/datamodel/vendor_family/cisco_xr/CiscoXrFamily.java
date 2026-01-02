package org.batfish.datamodel.vendor_family.cisco_xr;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.SwitchportMode;

public class CiscoXrFamily implements Serializable {
  private Aaa _aaa;

  private SortedMap<String, String> _banners;

  private SwitchportMode _defaultSwitchportMode;

  private String _enableSecret;

  private String _hostname;

  private SortedMap<String, L2tpClass> _l2tpClasses;

  private SortedMap<String, Line> _lines;

  private Logging _logging;

  private Ntp _ntp;

  private Boolean _proxyArp;

  private SortedMap<String, Service> _services;

  private Sntp _sntp;

  private Boolean _sourceRoute;

  private SshSettings _ssh;

  private SortedMap<String, User> _users;

  public CiscoXrFamily() {
    _banners = new TreeMap<>();
    _l2tpClasses = new TreeMap<>();
    _lines = new TreeMap<>();
    _services = new TreeMap<>();
    _users = new TreeMap<>();
  }

  public Aaa getAaa() {
    return _aaa;
  }

  public SortedMap<String, String> getBanners() {
    return _banners;
  }

  public SwitchportMode getDefaultSwitchportMode() {
    return _defaultSwitchportMode;
  }

  public String getEnableSecret() {
    return _enableSecret;
  }

  public String getHostname() {
    return _hostname;
  }

  public SortedMap<String, L2tpClass> getL2tpClasses() {
    return _l2tpClasses;
  }

  public SortedMap<String, Line> getLines() {
    return _lines;
  }

  public Logging getLogging() {
    return _logging;
  }

  public Ntp getNtp() {
    return _ntp;
  }

  public Boolean getProxyArp() {
    return _proxyArp;
  }

  public SortedMap<String, Service> getServices() {
    return _services;
  }

  public Sntp getSntp() {
    return _sntp;
  }

  public Boolean getSourceRoute() {
    return _sourceRoute;
  }

  public SshSettings getSsh() {
    return _ssh;
  }

  public SortedMap<String, User> getUsers() {
    return _users;
  }

  public void setAaa(Aaa aaa) {
    _aaa = aaa;
  }

  public void setBanners(SortedMap<String, String> banners) {
    _banners = banners;
  }

  public void setDefaultSwitchportMode(SwitchportMode defaultSwitchportMode) {
    _defaultSwitchportMode = defaultSwitchportMode;
  }

  public void setEnableSecret(String enableSecret) {
    _enableSecret = enableSecret;
  }

  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  public void setL2tpClasses(SortedMap<String, L2tpClass> l2tpClasses) {
    _l2tpClasses = l2tpClasses;
  }

  public void setLines(SortedMap<String, Line> lines) {
    _lines = lines;
  }

  public void setLogging(Logging logging) {
    _logging = logging;
  }

  public void setNtp(Ntp ntp) {
    _ntp = ntp;
  }

  public void setProxyArp(Boolean proxyArp) {
    _proxyArp = proxyArp;
  }

  public void setServices(SortedMap<String, Service> services) {
    _services = services;
  }

  public void setSntp(Sntp sntp) {
    _sntp = sntp;
  }

  public void setSourceRoute(Boolean sourceRoute) {
    _sourceRoute = sourceRoute;
  }

  public void setSsh(SshSettings ssh) {
    _ssh = ssh;
  }

  public void setUsers(SortedMap<String, User> users) {
    _users = users;
  }
}
