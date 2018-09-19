package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.IpMatchersImpl.ContainedBy;

public class IpMatchers {

  private IpMatchers() {}

  /**
   * Provides a matcher that matches if the {@link Ip} is contained by the specified {@link
   * IpSpace}.
   */
  public static ContainedBy containedBy(@Nonnull IpSpace ipSpace) {
    return new ContainedBy(ipSpace, ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches if the {@link Ip} is contained by the specified {@link IpSpace}
   * given the specified named IpSpace definitions.
   */
  public static ContainedBy containedBy(
      @Nonnull IpSpace ipSpace, @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return new ContainedBy(ipSpace, namedIpSpaces);
  }

  /**
   * Provides a matcher that matches if the {@link Ip} is contained by the specified {@link Prefix}.
   */
  public static ContainedBy containedByPrefix(@Nonnull String prefix) {
    return new ContainedBy(fromPrefix(prefix), ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches if the {@link Ip} is contained by the specified {@link Prefix}
   * given the specified named IpSpace definitions.
   */
  public static ContainedBy containedByPrefix(
      @Nonnull String prefix, @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return new ContainedBy(fromPrefix(prefix), namedIpSpaces);
  }

  private static IpSpace fromPrefix(String prefix) {
    return Prefix.parse(prefix).toIpSpace();
  }
}
