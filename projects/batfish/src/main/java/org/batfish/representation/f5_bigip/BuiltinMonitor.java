package org.batfish.representation.f5_bigip;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface BuiltinMonitor extends Builtin {

  public static @Nullable BuiltinMonitor getBuiltinMonitor(String name) {
    String unqualifiedName = Builtin.unqualify(name);
    return Stream.<Function<String, ? extends BuiltinMonitor>>of(
            BuiltinMonitorDns::forName,
            BuiltinMonitorGatewayIcmp::forName,
            BuiltinMonitorHttp::forName,
            BuiltinMonitorHttps::forName,
            BuiltinMonitorLdap::forName,
            BuiltinMonitorTcp::forName)
        .map(f -> f.apply(unqualifiedName))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }
}
