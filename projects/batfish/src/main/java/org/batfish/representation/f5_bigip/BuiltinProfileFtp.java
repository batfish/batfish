package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm profile ftp configurations */
@ParametersAreNonnullByDefault
public enum BuiltinProfileFtp implements BuiltinProfile {
  FTP("ftp");

  private static final Map<String, BuiltinProfileFtp> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(ImmutableMap.toImmutableMap(BuiltinProfileFtp::getName, Function.identity()));

  public static @Nullable BuiltinProfileFtp forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinProfileFtp(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PROFILE_FTP;
  }
}
