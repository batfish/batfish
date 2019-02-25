package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile server-ssl configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileServerSsl implements BuiltinProfile {
  APM_DEFAULT_SERVERSSL("apm-default-serverssl"),
  CRYPTO_CLIENT_DEFAULT_SERVERSSL("crypto-client-default-serverssl"),
  PCOIP_DEFAULT_SERVERSSL("pcoip-default-serverssl"),
  SERVERSSL("serverssl"),
  SERVERSSL_INSECURE_COMPATIBLE("serverssl-insecure-compatible"),
  SPLITSESSION_DEFAULT_SERVERSSL("splitsession-default-serverssl"),
  WOM_DEFAULT_SERVERSSL("wom-default-serverssl");

  private static final Map<String, BuiltinProfileServerSsl> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(BuiltinProfileServerSsl::getName, Function.identity()));

  public static @Nullable BuiltinProfileServerSsl forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileServerSsl(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_SERVER_SSL;
  }
}
