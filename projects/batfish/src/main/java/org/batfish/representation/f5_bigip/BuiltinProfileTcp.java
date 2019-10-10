package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile tcp configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileTcp implements BuiltinProfile {
  APM_FORWARDING_CLIENT_TCP("apm-forwarding-client-tcp"),
  APM_FORWARDING_SERVER_TCP("apm-forwarding-server-tcp"),
  F5_TCP_LAN("f5-tcp-lan"),
  F5_TCP_MOBILE("f5-tcp-mobile"),
  F5_TCP_PROGRESSIVE("f5-tcp-progressive"),
  F5_TCP_WAN("f5-tcp-wan"),
  MPTCP_MOBILE_OPTIMIZED("mptcp-mobile-optimized"),
  SPLITSESSION_DEFAULT_TCP("splitsession-default-tcp"),
  TCP("tcp"),
  TCP_LAN_OPTIMIZED("tcp-lan-optimized"),
  TCP_LEGACY("tcp-legacy"),
  TCP_MOBILE_OPTIMIZED("tcp-mobile-optimized"),
  TCP_WAN_OPTIMIZED("tcp-wan-optimized"),
  WOM_TCP_LAN_OPTIMIZED("wom-tcp-lan-optimized"),
  WOM_TCP_WAN_OPTIMIZED("wom-tcp-wan-optimized");

  private static final Map<String, BuiltinProfileTcp> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinProfileTcp::getName, Function.identity()));

  public static @Nullable BuiltinProfileTcp forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileTcp(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_TCP;
  }
}
