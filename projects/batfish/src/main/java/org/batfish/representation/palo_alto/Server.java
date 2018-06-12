package org.batfish.representation.palo_alto;

import org.batfish.common.util.ComparableStructure;

public class Server extends ComparableStructure<String> {
  private static final long serialVersionUID = 1L;

  private String _address;

  public Server(String name) {
    super(name);
  }

  public String getAddress() {
    return _address;
  }

  public void setAddress(String address) {
    _address = address;
  }
}
