package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.SwitchportMode;

public class CiscoFamily implements Serializable {
  private static final String PROP_AAA = "aaa";
  private static final String PROP_BANNERS = "banners";
  private static final String PROP_CABLE = "cable";
  private static final String PROP_DEFAULT_SWITCHPORT_MODE = "defaultSwitchportMode";
  private static final String PROP_DEPI_CLASSES = "depiClasses";
  private static final String PROP_DEPI_TUNNELS = "depiTunnels";
  private static final String PROP_ENABLE_SECRET = "enableSecret";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_L2TP_CLASSES = "l2tpClasses";
  private static final String PROP_LINES = "lines";
  private static final String PROP_LOGGING = "logging";
  private static final String PROP_NTP = "ntp";
  private static final String PROP_PKI = "pki";
  private static final String PROP_PROXY_ARP = "proxyArp";
  private static final String PROP_SERVICES = "services";
  private static final String PROP_SNTP = "sntp";
  private static final String PROP_SOURCE_ROUTE = "sourceRoute";
  private static final String PROP_SSH = "ssh";
  private static final String PROP_TELEMETRY = "telemetry";
  private static final String PROP_USERS = "users";

  private Aaa _aaa;

  private SortedMap<String, String> _banners;

  private Cable _cable;

  private SwitchportMode _defaultSwitchportMode;

  private SortedMap<String, DepiClass> _depiClasses;

  private SortedMap<String, DepiTunnel> _depiTunnels;

  private String _enableSecret;

  private String _hostname;

  private SortedMap<String, L2tpClass> _l2tpClasses;

  private SortedMap<String, Line> _lines;

  private Logging _logging;

  private Ntp _ntp;

  private Pki _pki;

  private Boolean _proxyArp;

  private SortedMap<String, Service> _services;

  private Sntp _sntp;

  private Boolean _sourceRoute;

  private SshSettings _ssh;

  private Telemetry _telemetry;

  private SortedMap<String, User> _users;

  public CiscoFamily() {
    _banners = new TreeMap<>();
    _depiClasses = new TreeMap<>();
    _depiTunnels = new TreeMap<>();
    _l2tpClasses = new TreeMap<>();
    _lines = new TreeMap<>();
    _services = new TreeMap<>();
    _users = new TreeMap<>();
  }

  @JsonProperty(PROP_AAA)
  public Aaa getAaa() {
    return _aaa;
  }

  @JsonProperty(PROP_BANNERS)
  public SortedMap<String, String> getBanners() {
    return _banners;
  }

  @JsonProperty(PROP_CABLE)
  public Cable getCable() {
    return _cable;
  }

  @JsonProperty(PROP_DEFAULT_SWITCHPORT_MODE)
  public SwitchportMode getDefaultSwitchportMode() {
    return _defaultSwitchportMode;
  }

  @JsonProperty(PROP_DEPI_CLASSES)
  public SortedMap<String, DepiClass> getDepiClasses() {
    return _depiClasses;
  }

  @JsonProperty(PROP_DEPI_TUNNELS)
  public SortedMap<String, DepiTunnel> getDepiTunnels() {
    return _depiTunnels;
  }

  @JsonProperty(PROP_ENABLE_SECRET)
  public String getEnableSecret() {
    return _enableSecret;
  }

  @JsonProperty(PROP_HOSTNAME)
  public String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_L2TP_CLASSES)
  public SortedMap<String, L2tpClass> getL2tpClasses() {
    return _l2tpClasses;
  }

  @JsonProperty(PROP_LINES)
  public SortedMap<String, Line> getLines() {
    return _lines;
  }

  @JsonProperty(PROP_LOGGING)
  public Logging getLogging() {
    return _logging;
  }

  @JsonProperty(PROP_NTP)
  public Ntp getNtp() {
    return _ntp;
  }

  @JsonProperty(PROP_PKI)
  public Pki getPki() {
    return _pki;
  }

  @JsonProperty(PROP_PROXY_ARP)
  public Boolean getProxyArp() {
    return _proxyArp;
  }

  @JsonProperty(PROP_SERVICES)
  public SortedMap<String, Service> getServices() {
    return _services;
  }

  @JsonProperty(PROP_SNTP)
  public Sntp getSntp() {
    return _sntp;
  }

  @JsonProperty(PROP_SOURCE_ROUTE)
  public Boolean getSourceRoute() {
    return _sourceRoute;
  }

  @JsonProperty(PROP_SSH)
  public SshSettings getSsh() {
    return _ssh;
  }

  @JsonProperty(PROP_TELEMETRY)
  public Telemetry getTelemetry() {
    return _telemetry;
  }

  @JsonProperty(PROP_USERS)
  public SortedMap<String, User> getUsers() {
    return _users;
  }

  @JsonProperty(PROP_AAA)
  public void setAaa(Aaa aaa) {
    _aaa = aaa;
  }

  @JsonProperty(PROP_BANNERS)
  public void setBanners(SortedMap<String, String> banners) {
    _banners = banners;
  }

  @JsonProperty(PROP_CABLE)
  public void setCable(Cable cable) {
    _cable = cable;
  }

  @JsonProperty(PROP_DEFAULT_SWITCHPORT_MODE)
  public void setDefaultSwitchportMode(SwitchportMode defaultSwitchportMode) {
    _defaultSwitchportMode = defaultSwitchportMode;
  }

  @JsonProperty(PROP_DEPI_CLASSES)
  public void setDepiClasses(SortedMap<String, DepiClass> depiClasses) {
    _depiClasses = depiClasses;
  }

  @JsonProperty(PROP_DEPI_TUNNELS)
  public void setDepiTunnels(SortedMap<String, DepiTunnel> depiTunnels) {
    _depiTunnels = depiTunnels;
  }

  @JsonProperty(PROP_ENABLE_SECRET)
  public void setEnableSecret(String enableSecret) {
    _enableSecret = enableSecret;
  }

  @JsonProperty(PROP_HOSTNAME)
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @JsonProperty(PROP_L2TP_CLASSES)
  public void setL2tpClasses(SortedMap<String, L2tpClass> l2tpClasses) {
    _l2tpClasses = l2tpClasses;
  }

  @JsonProperty(PROP_LINES)
  public void setLines(SortedMap<String, Line> lines) {
    _lines = lines;
  }

  @JsonProperty(PROP_LOGGING)
  public void setLogging(Logging logging) {
    _logging = logging;
  }

  @JsonProperty(PROP_NTP)
  public void setNtp(Ntp ntp) {
    _ntp = ntp;
  }

  @JsonProperty(PROP_PKI)
  public void setPki(Pki pki) {
    _pki = pki;
  }

  @JsonProperty(PROP_PROXY_ARP)
  public void setProxyArp(Boolean proxyArp) {
    _proxyArp = proxyArp;
  }

  @JsonProperty(PROP_SERVICES)
  public void setServices(SortedMap<String, Service> services) {
    _services = services;
  }

  @JsonProperty(PROP_SNTP)
  public void setSntp(Sntp sntp) {
    _sntp = sntp;
  }

  @JsonProperty(PROP_SOURCE_ROUTE)
  public void setSourceRoute(Boolean sourceRoute) {
    _sourceRoute = sourceRoute;
  }

  @JsonProperty(PROP_SSH)
  public void setSsh(SshSettings ssh) {
    _ssh = ssh;
  }

  @JsonProperty(PROP_TELEMETRY)
  public void setTelemetry(Telemetry telemetry) {
    _telemetry = telemetry;
  }

  @JsonProperty(PROP_USERS)
  public void setUsers(SortedMap<String, User> users) {
    _users = users;
  }
}
