package org.batfish.datamodel.matchers;

import org.batfish.datamodel.matchers.AaaAuthenticationLoginListMatchersImpl.HasMethod;

public class AaaAuthenticationLoginListMatchers {

  public static HasMethod hasMethod(String method) {
    return new HasMethod(method);
  }
}
