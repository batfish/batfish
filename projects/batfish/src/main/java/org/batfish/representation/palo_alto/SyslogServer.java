package org.batfish.representation.palo_alto;

import org.batfish.common.util.ComparableStructure;

/** PAN datamodel component containing server information for a syslog server */
public final class SyslogServer extends ComparableStructure<String> {
  private static final long serialVersionUID = 1L;

  private String _address;

  public SyslogServer(String name) {
    super(name);
  }

  public String getAddress() {
    return _address;
  }

  public void setAddress(String address) {
    _address = address;
  }
}
