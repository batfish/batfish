package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Ntp implements Serializable {

  private SortedMap<String, NtpServer> _servers;

  public Ntp() {
    _servers = new TreeMap<>();
  }

  public SortedMap<String, NtpServer> getServers() {
    return _servers;
  }

  public void setServers(SortedMap<String, NtpServer> servers) {
    _servers = servers;
  }
}
