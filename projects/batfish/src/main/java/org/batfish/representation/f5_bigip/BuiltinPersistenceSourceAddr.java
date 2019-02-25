package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Enumeration of built-in ltm persistence source-addr configurations */
@ParametersAreNonnullByDefault
public enum BuiltinPersistenceSourceAddr implements BuiltinPersistence {
  SOURCE_ADDR("source_addr");

  private static final Map<String, BuiltinPersistenceSourceAddr> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(
                  BuiltinPersistenceSourceAddr::getName, Function.identity()));

  public static @Nullable BuiltinPersistenceSourceAddr forName(String name) {
    return FOR_NAME_MAP.get(name);
  }

  private final @Nonnull String _name;

  private BuiltinPersistenceSourceAddr(String name) {
    _name = name;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public F5BigipStructureType getType() {
    return F5BigipStructureType.PERSISTENCE_SOURCE_ADDR;
  }
}
