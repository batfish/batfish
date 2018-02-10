package org.batfish.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

enum PacketField {
  DSTIP("dstip"),
  SRCIP("srcip"),
  DSTPORT("dstport"),
  SRCPORT("srcport"),
  IPPROTO("ipprotocol"),
  ICMPTYPE("icmptype"),
  ICMPCODE("icmpcode"),
  TCPACK("tcpack"),
  TCPCWR("tcpcwr"),
  TCPECE("tcpece"),
  TCPFIN("tcpfin"),
  TCPPSH("tcppsh"),
  TCPRST("tcprst"),
  TCPURG("tcpurg"),
  TCPSYN("tcpsyn");

  private static final Map<String, PacketField> _map = buildMap();

  private static Map<String, PacketField> buildMap() {
    ImmutableMap.Builder<String, PacketField> map = ImmutableMap.builder();
    for (PacketField value : PacketField.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  @JsonCreator
  public static PacketField fromName(String name) {
    PacketField instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + PacketField.class.getSimpleName() + " with name: \"" + name + "\"");
    }
    return instance;
  }

  private final String _name;

  PacketField(String name) {
    _name = name;
  }

  @JsonValue
  public String backendTypeName() {
    return _name;
  }
}
