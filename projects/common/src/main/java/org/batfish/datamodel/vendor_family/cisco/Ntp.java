package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Ntp implements Serializable {

  private boolean _authenticate;

  private SortedMap<Long, NtpAuthenticationKey> _authenticationKeys;

  private SortedMap<String, NtpServer> _servers;

  private SortedSet<Long> _trustedKeys;

  public Ntp() {
    _authenticationKeys = new TreeMap<>();
    _servers = new TreeMap<>();
    _trustedKeys = new TreeSet<>();
  }

  public boolean getAuthenticate() {
    return _authenticate;
  }

  public void setAuthenticate(boolean authenticate) {
    _authenticate = authenticate;
  }

  public SortedMap<Long, NtpAuthenticationKey> getAuthenticationKeys() {
    return _authenticationKeys;
  }

  public void setAuthenticationKeys(SortedMap<Long, NtpAuthenticationKey> authenticationKeys) {
    _authenticationKeys = authenticationKeys;
  }

  public SortedMap<String, NtpServer> getServers() {
    return _servers;
  }

  public void setServers(SortedMap<String, NtpServer> servers) {
    _servers = servers;
  }

  public SortedSet<Long> getTrustedKeys() {
    return _trustedKeys;
  }

  public void setTrustedKeys(SortedSet<Long> trustedKeys) {
    _trustedKeys = trustedKeys;
  }

  public boolean isServerAuthenticated(NtpServer server) {
    Long key = server.getKey();
    return _authenticate
        && key != null
        && _authenticationKeys.containsKey(key)
        && _trustedKeys.contains(key);
  }
}
