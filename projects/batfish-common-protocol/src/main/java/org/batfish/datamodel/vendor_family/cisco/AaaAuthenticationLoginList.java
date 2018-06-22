package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import org.batfish.datamodel.AuthenticationMethod;

public class AaaAuthenticationLoginList implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_METHODS = "methods";

  private final List<AuthenticationMethod> _methods;

  @JsonCreator
  public AaaAuthenticationLoginList(
      @JsonProperty(PROP_METHODS) List<AuthenticationMethod> methods) {
    _methods = methods == null ? ImmutableList.of() : ImmutableList.copyOf(methods);
  }

  @JsonProperty(PROP_METHODS)
  public List<AuthenticationMethod> getMethods() {
    return _methods;
  }
}
