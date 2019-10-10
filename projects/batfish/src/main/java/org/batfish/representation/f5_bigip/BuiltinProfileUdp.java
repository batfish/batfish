package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile udp configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileUdp implements BuiltinProfile {
  UDP("udp"),
  UDP_DECREMENT_TTL("udp_decrement_ttl"),
  UDP_GTM_DNS("udp_gtm_dns"),
  UDP_PRESERVE_TTL("udp_preserve_ttl");

  private static final Map<String, BuiltinProfileUdp> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinProfileUdp::getName, Function.identity()));

  public static @Nullable BuiltinProfileUdp forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileUdp(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_UDP;
  }
}
