package org.batfish.datamodel;

public final class IcmpCode {

  public static final int DESTINATION_HOST_UNKNOWN = 0x7;

  public static final int DESTINATION_HOST_UNREACHABLE = 0x1;

  public static final int DESTINATION_NETWORK_UNKNOWN = 0x6;

  public static final int DESTINATION_NETWORK_UNREACHABLE = 0x0;

  public static final int DESTINATION_PORT_UNREACHABLE = 0x3;

  public static final int ECHO_REPLY = 0x0;

  public static final int ECHO_REQUEST = 0x0;

  public static final int PACKET_TOO_BIG = 0x4;

  public static final int SOURCE_QUENCH = 0x0;

  public static final int TRACEROUTE = 0x0;

  public static final int TTL_EXCEEDED = 0x0;

  public static final int TTL_EQ_ZERO_DURING_TRANSIT = 0x0;

  public static final int UNSET = 0xff;

  private IcmpCode() {}
}
