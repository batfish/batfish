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
public final class Default {
  /** Get default IP protocol and port information. */
  @Nullable
  public Port getPort() {
    return _port;
  }

  /** Get the IP protocol number used for identifying the application. */
  @Nullable
  public String getIdentByIpProtocol() {
    return _identByIpProtocol;
  }

  private static final String PROP_PORT = "port";
  private static final String PROP_IDENT_BY_IP_PROTOCOL = "ident-by-ip-protocol";

  @JsonCreator
  private static @Nonnull Default create(
      @JsonProperty(PROP_PORT) @Nullable Port port,
      @JsonProperty(PROP_IDENT_BY_IP_PROTOCOL) @Nullable String identByIpProtocol) {
    return new Default(port, identByIpProtocol);
  }

  @VisibleForTesting
  Default(@Nullable Port port, @Nullable String identByIpProtocol) {
    _port = port;
    _identByIpProtocol = identByIpProtocol;
  }

  @Nullable private final Port _port;
  @Nullable private final String _identByIpProtocol;
}
