package org.batfish.representation.f5_bigip;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface BuiltinMonitor extends Builtin {

  public static @Nullable BuiltinMonitor getBuiltinMonitor(String name) {
    return Stream.<Function<String, ? extends BuiltinMonitor>>of(
            BuiltinMonitorHttp::forName, BuiltinMonitorHttps::forName)
        .map(f -> f.apply(name))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }
}
