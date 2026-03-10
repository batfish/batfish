package org.batfish.datamodel.applications;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.NamedPort;

/** An enum for common application names */
public enum NamedApplication {
  DNS("dns", new UdpApplication(NamedPort.DOMAIN.number())),
  ECHO_REPLY("echo-reply", new IcmpTypeCodesApplication(IcmpType.ECHO_REPLY, 0)),
  ECHO_REQUEST("echo-request", new IcmpTypeCodesApplication(IcmpType.ECHO_REQUEST, 0)),
  HTTP("http", new TcpApplication(NamedPort.HTTP.number())),
  HTTPS("https", new TcpApplication(NamedPort.HTTPS.number())),
  MYSQL("mysql", new TcpApplication(NamedPort.MYSQL_SERVER.number())),
  SMTP("smtp", new TcpApplication(NamedPort.SMTP.number())),
  SNMP("snmp", new UdpApplication(NamedPort.SNMP.number())),
  SSH("ssh", new TcpApplication(NamedPort.SSH.number())),
  TELNET("telnet", new TcpApplication(NamedPort.TELNET.number()));

  private static final Map<String, NamedApplication> MAP = initMap();

  @JsonCreator
  public static NamedApplication fromString(@Nullable String name) {
    requireNonNull(name, "Cannot instantiate protocol from null");
    NamedApplication value = MAP.get(name.toLowerCase());
    if (value == null) {
      throw new IllegalArgumentException("No NamedApplication found with name: '" + name);
    }
    return value;
  }

  private static Map<String, NamedApplication> initMap() {
    ImmutableMap.Builder<String, NamedApplication> map = ImmutableMap.builder();
    for (NamedApplication value : NamedApplication.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  private final @Nonnull Application _application;

  private final @Nonnull String _name;

  NamedApplication(@Nonnull String name, @Nonnull Application application) {
    _name = name;
    _application = application;
  }

  @JsonValue
  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Application getApplication() {
    return _application;
  }

  @Override
  public @Nonnull String toString() {
    return getName();
  }
}
