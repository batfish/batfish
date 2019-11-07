package org.batfish.datamodel.vendor_family.cisco_xr;

import java.io.Serializable;

public class SshSettings implements Serializable {

  private Integer _version;

  public SshSettings() {}

  public Integer getVersion() {
    return _version;
  }

  public void setVersion(Integer version) {
    _version = version;
  }
}
