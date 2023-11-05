package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6Space;
import org.batfish.datamodel.matchers.Ip6SpaceMatchersImpl.ContainsIp;

public class Ip6SpaceMatchers {
  private Ip6SpaceMatchers() {}

  /** Provides a matcher that matches if the {@link Ip6Space} contains the specified {@link Ip6}. */
  public static ContainsIp containsIp6(@Nonnull Ip6 ip6) {
    return new ContainsIp(ip6, ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches if the {@link Ip6Space} contains the specified {@link Ip6}
   * given the specified named Ip6Space definitions.
   */
  public static ContainsIp containsIp6(
      @Nonnull Ip6 ip6, @Nonnull Map<String, Ip6Space> namedIp6Spaces) {
    return new ContainsIp(ip6, namedIp6Spaces);
  }
}
