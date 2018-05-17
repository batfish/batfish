package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.batfish.datamodel.matchers.AaaAuthenticationLoginListMatchersImpl.HasMethods;

public class AaaAuthenticationLoginListMatchers {

  public static HasMethods hasMethod(String method) {
    return new HasMethods(hasItem(equalTo(method)));
  }
}
