package org.batfish.vendor.check_point_management;

import com.fasterxml.jackson.annotation.JsonCreator;

/** NAT method for a {@link NatRule}. */
public enum NatMethod {
  HIDE,
  STATIC;

  @JsonCreator
  private static NatMethod create(String jsonText) {
    return NatMethod.valueOf(jsonText.toUpperCase());
  }
}
