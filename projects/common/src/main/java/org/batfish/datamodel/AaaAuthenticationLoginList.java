package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AaaAuthenticationLoginList implements Serializable {

  private static final String PROP_METHODS = "methods";

  private boolean _isDefault = false;

  private final List<AuthenticationMethod> _methods = new ArrayList<>();

  public AaaAuthenticationLoginList(List<AuthenticationMethod> methods, boolean isDefault) {
    if (methods != null) {
      _methods.addAll(methods);
    }
    _isDefault = isDefault;
  }

  @JsonCreator
  public AaaAuthenticationLoginList(
      @JsonProperty(PROP_METHODS) List<AuthenticationMethod> methods) {
    if (methods != null) {
      _methods.addAll(methods);
    }
  }

  public void addMethod(AuthenticationMethod method) {
    _methods.add(method);
  }

  @JsonIgnore
  public boolean isDefault() {
    return _isDefault;
  }

  @JsonProperty(PROP_METHODS)
  public List<AuthenticationMethod> getMethods() {
    return ImmutableList.copyOf(_methods);
  }
}
