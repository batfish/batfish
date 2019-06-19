package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.matchers.IpSpaceMatchersImpl.ContainsIp;
import org.batfish.datamodel.matchers.IpSpaceMatchersImpl.Intersects;
import org.batfish.datamodel.matchers.IpSpaceMatchersImpl.SubsetOf;
import org.batfish.datamodel.matchers.IpSpaceMatchersImpl.SupersetOf;

public class IpSpaceMatchers {

  private IpSpaceMatchers() {}

  /** Provides a matcher that matches if the {@link IpSpace} contains the specified {@link Ip}. */
  public static ContainsIp containsIp(@Nonnull Ip ip) {
    return new ContainsIp(ip, ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches if the {@link IpSpace} contains the specified {@link Ip} given
   * the specified named IpSpace definitions.
   */
  public static ContainsIp containsIp(@Nonnull Ip ip, @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return new ContainsIp(ip, namedIpSpaces);
  }

  public static Intersects intersects(@Nonnull IpWildcard ipWildcard) {
    return new Intersects(ipWildcard);
  }

  public static SubsetOf subsetOf(@Nonnull IpWildcard ipWildcard) {
    return new SubsetOf(ipWildcard);
  }

  public static SupersetOf supersetOf(@Nonnull IpWildcard ipWildcard) {
    return new SupersetOf(ipWildcard);
  }
}
