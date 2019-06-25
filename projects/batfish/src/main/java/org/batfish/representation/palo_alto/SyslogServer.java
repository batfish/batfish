package org.batfish.representation.palo_alto;

import java.io.Serializable;

/** PAN datamodel component containing server information for a syslog server */
public final class SyslogServer implements Serializable {

  private String _address;

  private final String _name;

  public SyslogServer(String name) {
    _name = name;
  }

  public String getAddress() {
    return _address;
  }

  public String getName() {
    return _name;
  }

  public void setAddress(String address) {
    _address = address;
  }
}
