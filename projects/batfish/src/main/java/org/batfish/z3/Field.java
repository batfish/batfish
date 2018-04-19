package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public class Field {
  public static final Field DSCP = new Field("DSCP", 6);
  public static final Field DST_IP = new Field("DST_IP", 32);
  public static final Field DST_PORT = new Field("DST_PORT", 16);
  public static final Field ECN = new Field("ECN", 2);
  public static final Field FRAGMENT_OFFSET = new Field("FRAGMENT_OFFSET", 13);
  public static final Field ICMP_CODE = new Field("ICMP_CODE", 8);
  public static final Field ICMP_TYPE = new Field("ICMP_TYPE", 8);
  public static final Field IP_PROTOCOL = new Field("IP_PROTOCOL", 8);
  public static final Field ORIG_SRC_IP = new Field("ORIG_SRC_IP", 32);
  public static final Field PACKET_LENGTH = new Field("PACKET_LENGTH", 16);
  public static final Field SRC_IP = new Field("SRC_IP", 32);
  public static final Field SRC_PORT = new Field("SRC_PORT", 16);
  public static final Field STATE = new Field("STATE", 2);
  public static final Field TCP_FLAGS_ACK = new Field("TCP_FLAGS_ACK", 1);
  public static final Field TCP_FLAGS_CWR = new Field("TCP_FLAGS_CWR", 1);
  public static final Field TCP_FLAGS_ECE = new Field("TCP_FLAGS_ECE", 1);
  public static final Field TCP_FLAGS_FIN = new Field("TCP_FLAGS_FIN", 1);
  public static final Field TCP_FLAGS_PSH = new Field("TCP_FLAGS_PSH", 1);
  public static final Field TCP_FLAGS_RST = new Field("TCP_FLAGS_RST", 1);
  public static final Field TCP_FLAGS_SYN = new Field("TCP_FLAGS_SYN", 1);
  public static final Field TCP_FLAGS_URG = new Field("TCP_FLAGS_URG", 1);

  public static final List<Field> COMMON_FIELDS =
      ImmutableList.of(
          DSCP,
          DST_IP,
          DST_PORT,
          ECN,
          FRAGMENT_OFFSET,
          ICMP_CODE,
          ICMP_TYPE,
          IP_PROTOCOL,
          ORIG_SRC_IP,
          PACKET_LENGTH,
          SRC_IP,
          SRC_PORT,
          STATE,
          TCP_FLAGS_ACK,
          TCP_FLAGS_CWR,
          TCP_FLAGS_ECE,
          TCP_FLAGS_FIN,
          TCP_FLAGS_PSH,
          TCP_FLAGS_RST,
          TCP_FLAGS_SYN,
          TCP_FLAGS_URG);

  private final String _name;
  private final int _size;

  public Field(String name, int size) {
    _name = name;
    _size = size;
  }

  public String getName() {
    return _name;
  }

  public int getSize() {
    return _size;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Field)) {
      return false;
    }
    Field field = (Field) other;
    return _name.equals(field._name) && _size == field._size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _size);
  }
}
