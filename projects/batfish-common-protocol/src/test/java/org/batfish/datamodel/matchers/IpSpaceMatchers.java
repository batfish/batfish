package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.IpSpaceMatchersImpl.ContainsIp;

public class IpSpaceMatchers {

  private IpSpaceMatchers() {}

  /** Provides a matcher that matches if the {@link IpSpace} contains the specified {@link Ip}. */
  public static ContainsIp containsIp(@Nonnull Ip ip) {
    return new ContainsIp(ip);
  }
}
