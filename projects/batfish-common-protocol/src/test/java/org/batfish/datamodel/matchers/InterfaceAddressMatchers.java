package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.InterfaceAddressMatchersImpl.HasIp;

public final class InterfaceAddressMatchers {

  /**
   * Provides a matcher that matches if the Interface's value of {@code ip} matches specified {@code
   * ip}
   */
  public static HasIp hasIp(Ip ip) {
    return new HasIp(equalTo(ip));
  }

  private InterfaceAddressMatchers() {}
}
