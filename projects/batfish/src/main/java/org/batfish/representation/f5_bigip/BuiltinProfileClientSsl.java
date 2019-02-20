package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile client-ssl configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileClientSsl implements BuiltinProfile {
  CLIENTSSL("clientssl"),
  CLIENTSSL_INSECURE_COMPATIBLE("clientssl-insecure-compatible"),
  CLIENTSSL_SECURE("clientssl-secure"),
  CRYPTO_SERVER_DEFAULT_CLIENTSSL("crypto-server-default-clientssl"),
  SPLITSESSION_DEFAULT_CLIENTSSL("splitsession-default-clientssl"),
  WOM_DEFAULT_CLIENTSSL("wom-default-clientssl");

  private static final Map<String, BuiltinProfileClientSsl> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(BuiltinProfileClientSsl::getName, Function.identity()));

  public static @Nullable BuiltinProfileClientSsl forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileClientSsl(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_CLIENT_SSL;
  }
}
