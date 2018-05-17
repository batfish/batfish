package org.batfish.datamodel.vendor_family.cisco;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;

public class AaaAuthenticationLoginList implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final List<String> _methods;

  public AaaAuthenticationLoginList(List<String> methods) {
    _methods = ImmutableList.copyOf(methods);
  }

  public List<String> getMethods() {
    return _methods;
  }
}
