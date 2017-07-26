package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Service implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Boolean _enabled;

  private SortedMap<String, Service> _subservices;

  public Service() {
    _subservices = new TreeMap<>();
  }

  public void disable() {
    for (Service s : _subservices.values()) {
      s.disable();
    }
    _enabled = false;
  }

  public Boolean getEnabled() {
    return _enabled;
  }

  public SortedMap<String, Service> getSubservices() {
    return _subservices;
  }

  public void setEnabled(Boolean enabled) {
    _enabled = enabled;
  }

  public void setSubservices(SortedMap<String, Service> subservices) {
    _subservices = subservices;
  }
}
