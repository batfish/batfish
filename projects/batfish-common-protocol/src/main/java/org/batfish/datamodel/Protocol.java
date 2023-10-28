package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.applications.Application;
import org.batfish.datamodel.applications.TcpApplication;
import org.batfish.datamodel.applications.UdpApplication;

/** Shorthands for application-level protocols (i.e., combos of TCP or UDP and port) */
public enum Protocol {
  DNS("dns", IpProtocol.UDP, NamedPort.DOMAIN.number()),
  HTTP("http", IpProtocol.TCP, NamedPort.HTTP.number()),
  HTTPS("https", IpProtocol.TCP, NamedPort.HTTPS.number()),
  SNMP("snmp", IpProtocol.UDP, NamedPort.SNMP.number()),
  SSH("ssh", IpProtocol.TCP, NamedPort.SSH.number()),
  TELNET("telnet", IpProtocol.TCP, NamedPort.TELNET.number());

  private static final Map<String, Protocol> MAP = initMap();

  @JsonCreator
  public static Protocol fromString(@Nullable String name) {
    requireNonNull(name, "Cannot instantiate protocol from null");
    Protocol value = MAP.get(name.toLowerCase());
    if (value == null) {
      throw new BatfishException(
          "No " + Protocol.class.getSimpleName() + " with name: '" + name + "'");
    }
    return value;
  }

  private static Map<String, Protocol> initMap() {
    ImmutableMap.Builder<String, Protocol> map = ImmutableMap.builder();
    for (Protocol value : Protocol.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  private final @Nonnull IpProtocol _ipProtocol;

  private final @Nonnull String _name;

  private final @Nonnull Integer _port;

  Protocol(@Nonnull String name, @Nonnull IpProtocol ipProtocol, @Nonnull Integer port) {
    _name = name;
    _ipProtocol = ipProtocol;
    _port = port;
  }

  public @Nonnull IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonValue
  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Integer getPort() {
    return _port;
  }

  /** Converts this object to the new Application object. */
  public Application toApplication() {
    switch (_ipProtocol) {
      case TCP:
        return new TcpApplication(_port);
      case UDP:
        return new UdpApplication(_port);
      default:
        throw new IllegalArgumentException(
            "Protocol with IpProtocol " + _ipProtocol + " cannot be converted to Application");
    }
  }
}
