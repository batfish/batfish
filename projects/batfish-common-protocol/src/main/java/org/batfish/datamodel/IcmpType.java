package org.batfish.datamodel;

/**
 * Represents the integer constants used for ICMP Types. For more information, see RFC-792,
 * RFC-1122, RFC-1812, and more.
 */
public final class IcmpType {
  public static final int ALTERNATE_ADDRESS = 0x6;

  public static final int CONVERSION_ERROR = 0x1f;

  public static final int DESTINATION_UNREACHABLE = 0x3;

  public static final int ECHO_REPLY = 0x0;

  public static final int ECHO_REQUEST = 0x8;

  public static final int INFO_REPLY = 0x10;

  public static final int INFO_REQUEST = 0xf;

  public static final int MASK_REPLY = 0x12;

  public static final int MASK_REQUEST = 0x11;

  public static final int MOBILE_REDIRECT = 0x20;

  public static final int PARAMETER_PROBLEM = 0xc;

  public static final int REDIRECT_MESSAGE = 0x5;

  public static final int ROUTER_ADVERTISEMENT = 0x9;

  public static final int ROUTER_SOLICITATION = 0xa;

  public static final int SOURCE_QUENCH = 0x4;

  public static final int TIME_EXCEEDED = 0xb;

  public static final int TIMESTAMP_REPLY = 0xe;

  public static final int TIMESTAMP_REQUEST = 0xd;

  public static final int TRACEROUTE = 0x1e;

  public static final int REQUEST_EXTENDED_ECHO = 0x2a; // rfc 8335

  public static final int REQUEST_EXTENDED_REPLY = 0x2b; // rfc 8335

  public static final int UNSET = 0xff;

  private IcmpType() {}
}
