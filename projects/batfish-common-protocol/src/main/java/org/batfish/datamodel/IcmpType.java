package org.batfish.datamodel;

public final class IcmpType {

  public static final int DESTINATION_UNREACHABLE = 0x3;

  public static final int ECHO_REPLY = 0x0;

  public static final int ECHO_REQUEST = 0x8;

  public static final int PARAMETER_PROBLEM = 0xc;

  public static final int REDIRECT_MESSAGE = 0x5;

  public static final int SOURCE_QUENCH = 0x4;

  public static final int TIME_EXCEEDED = 0xb;

  public static final int TRACEROUTE = 0x1e;

  public static final int UNSET = 0xff;

  private IcmpType() {}
}
