package org.batfish.representation.f5_bigip;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface BuiltinPersistence extends Builtin {

  public static @Nullable BuiltinPersistence getBuiltinPersistence(String name) {
    String unqualifiedName = Builtin.unqualify(name);
    return Stream.<Function<String, ? extends BuiltinPersistence>>of(
            BuiltinPersistenceCookie::forName,
            BuiltinPersistenceSourceAddr::forName,
            BuiltinPersistenceSsl::forName)
        .map(f -> f.apply(unqualifiedName))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }
}
