package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;

public class SshSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer _version;

  public SshSettings() {}

  public Integer getVersion() {
    return _version;
  }

  public void setVersion(Integer version) {
    _version = version;
  }
}
