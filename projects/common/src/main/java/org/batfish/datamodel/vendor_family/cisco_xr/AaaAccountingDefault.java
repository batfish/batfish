package org.batfish.datamodel.vendor_family.cisco_xr;

import java.io.Serializable;
import java.util.List;

public class AaaAccountingDefault implements Serializable {

  private List<String> _groups;

  private Boolean _local;

  public List<String> getGroups() {
    return _groups;
  }

  public Boolean getLocal() {
    return _local;
  }

  public void setGroups(List<String> groups) {
    _groups = groups;
  }

  public void setLocal(Boolean local) {
    _local = local;
  }
}
