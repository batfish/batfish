package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Sntp implements Serializable {

  private SortedMap<String, SntpServer> _servers;

  public Sntp() {
    _servers = new TreeMap<>();
  }

  public SortedMap<String, SntpServer> getServers() {
    return _servers;
  }

  public void setServers(SortedMap<String, SntpServer> servers) {
    _servers = servers;
  }
}
