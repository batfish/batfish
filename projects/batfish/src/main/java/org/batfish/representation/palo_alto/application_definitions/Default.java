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
  public @Nullable Port getPort() {
    return _port;
  }

  /** Get the IP protocol number used for identifying the application. */
  public @Nullable String getIdentByIpProtocol() {
    return _identByIpProtocol;
  }

  /** Get the ICMP type used for identifying the application. */
  public @Nullable String getIdentByIcmpType() {
    return _identByIcmpType;
  }

  private static final String PROP_PORT = "port";
  private static final String PROP_IDENT_BY_IP_PROTOCOL = "ident-by-ip-protocol";
  private static final String PROP_IDENT_BY_ICMP_TYPE = "ident-by-icmp-type";

  @JsonCreator
  private static @Nonnull Default jsonCreator(
      @JsonProperty(PROP_PORT) @Nullable Port port,
      @JsonProperty(PROP_IDENT_BY_IP_PROTOCOL) @Nullable String identByIpProtocol,
      @JsonProperty(PROP_IDENT_BY_ICMP_TYPE) @Nullable JsonNode identByIcmpType) {
    String type =
        identByIcmpType == null || identByIcmpType instanceof NullNode
            ? null
            : icmpTypeToString(identByIcmpType);
    return new Default(port, identByIpProtocol, type);
  }

  /** Extract the type string from an ICMP type. */
  private static @Nonnull String icmpTypeToString(@Nonnull JsonNode type) {
    assert type instanceof ObjectNode; // Map/Object
    ObjectNode objectNode = (ObjectNode) type;
    JsonNode text = objectNode.findValue("type");
    assert text instanceof TextNode;
    return text.textValue();
  }

  @VisibleForTesting
  Default(
      @Nullable Port port, @Nullable String identByIpProtocol, @Nullable String identByIcmpType) {
    _port = port;
    _identByIpProtocol = identByIpProtocol;
    _identByIcmpType = identByIcmpType;
  }

  private final @Nullable Port _port;
  private final @Nullable String _identByIpProtocol;
  private final @Nullable String _identByIcmpType;
}
