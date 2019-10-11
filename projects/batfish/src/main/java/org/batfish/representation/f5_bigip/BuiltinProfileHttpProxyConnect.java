package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile http-proxy-connect configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileHttpProxyConnect implements BuiltinProfile {
  HTTP_PROXY_CONNECT("http-proxy-connect");

  private static final Map<String, BuiltinProfileHttpProxyConnect> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(
                  BuiltinProfileHttpProxyConnect::getName, Function.identity()));

  public static @Nullable BuiltinProfileHttpProxyConnect forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileHttpProxyConnect(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_HTTP_PROXY_CONNECT;
  }
}
