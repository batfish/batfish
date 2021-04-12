package org.batfish.datamodel;

/** Represents the integer constants used for ICMPv6 Types. For more information, see RFC-4443. */
public final class Icmp6Type {

  // error messages
  public static final int DESTINATION_UNREACHABLE = 0x1;
  public static final int PACKET_TOO_BIG = 0x2;
  public static final int TIME_EXCEEDED = 0x3;
  public static final int PARAMETER_PROBLEM = 0x4;

  // informational messages
  public static final int ECHO_REQUEST = 0x80;
  public static final int ECHO_REPLY = 0x81;

  private Icmp6Type() {}
}
