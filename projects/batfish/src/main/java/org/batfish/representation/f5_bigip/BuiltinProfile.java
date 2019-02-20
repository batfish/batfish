package org.batfish.representation.f5_bigip;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface BuiltinProfile extends Builtin {

  public static @Nullable BuiltinProfile getBuiltinProfile(String name) {
    String unqualifiedName = Builtin.unqualify(name);
    return Stream.<Function<String, ? extends BuiltinProfile>>of(
            BuiltinProfileClientSsl::forName,
            BuiltinProfileHttp::forName,
            BuiltinProfileOneConnect::forName,
            BuiltinProfileTcp::forName,
            BuiltinProfileServerSsl::forName)
        .map(f -> f.apply(unqualifiedName))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }
}
