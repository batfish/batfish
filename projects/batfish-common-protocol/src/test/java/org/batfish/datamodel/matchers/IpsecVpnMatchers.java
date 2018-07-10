package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.matchers.IpsecVpnMatchersImpl.HasBindInterface;
import org.batfish.datamodel.matchers.IpsecVpnMatchersImpl.HasIkeGatewaay;
import org.batfish.datamodel.matchers.IpsecVpnMatchersImpl.HasIpsecPolicy;
import org.batfish.datamodel.matchers.IpsecVpnMatchersImpl.HasPolicy;
import org.hamcrest.Matcher;

public final class IpsecVpnMatchers {

  /**
   * Provides a matcher that matches if the IPSec VPN's value of {@code subMatcher} matches
   * specified {@code bindInterface}
   */
  public static @Nonnull HasBindInterface hasBindInterface(
      @Nonnull Matcher<? super Interface> subMatcher) {
    return new HasBindInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches if the IPSec VPN's value of {@code subMatcher} matches
   * specified {@code ikeGateway}
   */
  public static @Nonnull HasIkeGatewaay hasIkeGatewaay(
      @Nonnull Matcher<? super IkeGateway> subMatcher) {
    return new HasIkeGatewaay(subMatcher);
  }

  /**
   * Provides a matcher that matches if the IPSec VPN's value of {@code subMatcher} matches
   * specified {@code ipsecPolicy}
   */
  public static @Nonnull HasIpsecPolicy hasIpsecPolicy(
      @Nonnull Matcher<? super IpsecPolicy> subMatcher) {
    return new HasIpsecPolicy(subMatcher);
  }

  /**
   * Provides a matcher that matches if the IPSec VPN's value of {@code subMatcher} matches
   * specified {@code policy}
   */
  public static @Nonnull HasPolicy hasPolicy(@Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasPolicy(subMatcher);
  }

  private IpsecVpnMatchers() {}
}
