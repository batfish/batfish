package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.List;

public class AaaAuthenticationLoginList implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private List<String> _methods;

  /** TODO: Remove when meaningful fields are added to this class */
  public Object _placeholder;

  public AaaAuthenticationLoginList(List<String> methods) {
    _methods = methods;
  }

  public List<String> getMethods() {
    return _methods;
  }
}
