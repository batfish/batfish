package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum Protocol {
  DNS("dns", IpProtocol.UDP, NamedPort.DOMAIN.number()),
  HTTP("http", IpProtocol.TCP, NamedPort.HTTP.number()),
  HTTPS("https", IpProtocol.TCP, NamedPort.HTTPS.number()),
  SSH("ssh", IpProtocol.TCP, NamedPort.SSH.number()),
  TELNET("telnet", IpProtocol.TCP, NamedPort.TELNET.number()),
  TCP("tcp", IpProtocol.TCP, null),
  UDP("udp", IpProtocol.UDP, null);

  private static final Map<String, Protocol> MAP = initMap();

  @JsonCreator
  public static Protocol fromString(String name) {
    Protocol value = MAP.get(name.toLowerCase());
    if (value == null) {
      throw new BatfishException(
          "No " + Protocol.class.getSimpleName() + " with name: '" + name + "'");
    }
    return value;
  }

  private static synchronized Map<String, Protocol> initMap() {
    Map<String, Protocol> map = new HashMap<>();
    for (Protocol value : Protocol.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return Collections.unmodifiableMap(map);
  }

  private final IpProtocol _ipProtocol;

  private final String _name;

  private final Integer _port;

  private Protocol(String name, IpProtocol ipProtocol, Integer port) {
    _name = name;
    _ipProtocol = ipProtocol;
    _port = port;
  }

  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonValue
  public String getName() {
    return _name;
  }

  public Integer getPort() {
    return _port;
  }
}
