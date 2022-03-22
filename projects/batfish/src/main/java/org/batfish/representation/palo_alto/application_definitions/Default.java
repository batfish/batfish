package org.batfish.representation.palo_alto.application_definitions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data model class containing information about a Palo Alto application's default properties,
 * specifically port(s) and IP protocol(s).
 */
public class Default {
  @Nullable
  public Port getPort() {
    return _port;
  }

  private static final String PROP_PORT = "port";

  @JsonCreator
  private static @Nonnull Default create(@JsonProperty(PROP_PORT) @Nullable Port port) {
    // checkArgument(port != null, "Missing %s", PROP_PORT);
    if (port == null) {
      return new Default(null);
    }
    return new Default(port);
  }

  @VisibleForTesting
  Default(@Nonnull Port port) {
    _port = port;
  }

  @Nullable private final Port _port;
}
