package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile dhcpv6 configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileDhcpv4 implements BuiltinProfile {
  DHCPV4("dhcpv4"),
  DHCPV4_FWD("dhcpv4_fwd"),
  DHCPV6("dhcpv6"),
  DHCPV6_FWD("dhcpv6_fwd");

  private static final Map<String, BuiltinProfileDhcpv4> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinProfileDhcpv4::getName, Function.identity()));

  public static @Nullable BuiltinProfileDhcpv4 forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileDhcpv4(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_DHCPV6;
  }
}
