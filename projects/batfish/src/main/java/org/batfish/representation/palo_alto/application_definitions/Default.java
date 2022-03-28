package org.batfish.representation.palo_alto.application_definitions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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

  /** Get the ICMP type used for identifying the application. */
  @Nullable
  public String getIdentByIcmpType() {
    return _identByIcmpType;
  }

  private static final String PROP_PORT = "port";
  private static final String PROP_IDENT_BY_IP_PROTOCOL = "ident-by-ip-protocol";
  private static final String PROP_IDENT_BY_ICMP_TYPE = "ident-by-icmp-type";

  @JsonCreator
  private static @Nonnull Default create(
      @JsonProperty(PROP_PORT) @Nullable Port port,
      @JsonProperty(PROP_IDENT_BY_IP_PROTOCOL) @Nullable String identByIpProtocol,
      @JsonProperty(PROP_IDENT_BY_ICMP_TYPE) @Nullable JsonNode identByIcmpType) {
    String type =
        identByIcmpType == null || identByIcmpType instanceof NullNode
            ? null
            : icmpTypeToString(identByIcmpType);
    return create(port, identByIpProtocol, type);
  }

  @VisibleForTesting
  static @Nonnull Default create(
      @Nullable Port port, @Nullable String identByIpProtocol, @Nullable String identByIcmpType) {
    return new Default(port, identByIpProtocol, identByIcmpType);
  }

  /** Extract the type string from an ICMP type. */
  private static @Nonnull String icmpTypeToString(@Nonnull JsonNode type) {
    assert type instanceof ObjectNode; // Map/Object
    ObjectNode objectNode = (ObjectNode) type;
    JsonNode text = objectNode.findValue("type");
    assert text instanceof TextNode;
    return text.textValue();
  }

  private Default(
      @Nullable Port port, @Nullable String identByIpProtocol, @Nullable String identByIcmpType) {
    _port = port;
    _identByIpProtocol = identByIpProtocol;
    _identByIcmpType = identByIcmpType;
  }

  @Nullable private final Port _port;
  @Nullable private final String _identByIpProtocol;
  @Nullable private final String _identByIcmpType;
}
