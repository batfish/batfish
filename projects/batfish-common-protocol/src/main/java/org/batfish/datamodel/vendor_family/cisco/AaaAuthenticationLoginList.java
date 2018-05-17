package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;

public class AaaAuthenticationLoginList implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_METHODS = "methods";

  private final List<String> _methods;

  @JsonCreator
  public AaaAuthenticationLoginList(@JsonProperty(PROP_METHODS) List<String> methods) {
    _methods = methods == null ? ImmutableList.of() : ImmutableList.copyOf(methods);
  }

  @JsonProperty(PROP_METHODS)
  public List<String> getMethods() {
    return _methods;
  }
}
