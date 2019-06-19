package org.batfish.datamodel;

/**
 * Represents the integer constants used for ICMP Codes. For more information, see RFC-792,
 * RFC-1122, RFC-1812, and more.
 */
public final class IcmpCode {

  // For IcmpType#DESTINATION_UNREACHABLE
  public static final int NETWORK_UNREACHABLE = 0;
  public static final int HOST_UNREACHABLE = 1;
  public static final int PROTOCOL_UNREACHABLE = 2;
  public static final int PORT_UNREACHABLE = 3;
  public static final int FRAGMENTATION_NEEDED = 4;
  public static final int SOURCE_ROUTE_FAILED = 5;
  public static final int DESTINATION_NETWORK_UNKNOWN = 6;
  public static final int DESTINATION_HOST_UNKNOWN = 7;
  public static final int SOURCE_HOST_ISOLATED = 8;
  public static final int DESTINATION_NETWORK_PROHIBITED = 9;
  public static final int DESTINATION_HOST_PROHIBITED = 10;
  public static final int NETWORK_UNREACHABLE_FOR_TOS = 11;
  public static final int HOST_UNREACHABLE_FOR_TOS = 12;
  public static final int COMMUNICATION_ADMINISTRATIVELY_PROHIBITED = 13;
  public static final int HOST_PRECEDENCE_VIOLATION = 14;
  public static final int PRECEDENCE_CUTOFF_IN_EFFECT = 15;

  // For IcmpType#PARAMETER_PROBLEM
  public static final int INVALID_IP_HEADER = 0;
  public static final int REQUIRED_OPTION_MISSING = 1;
  public static final int BAD_LENGTH = 2;

  // For IcmpType#REDIRECT_MESSAGE
  public static final int NETWORK_ERROR = 0;
  public static final int HOST_ERROR = 1;
  public static final int TOS_AND_NETWORK_ERROR = 2;
  public static final int TOS_AND_HOST_ERROR = 3;

  // For IcmpType#TIME_EXCEEDED
  public static final int TTL_EQ_ZERO_DURING_TRANSIT = 0;
  public static final int TIME_EXCEEDED_DURING_FRAGMENT_REASSEMBLY = 1;

  public static final int UNSET = 0xff;

  private IcmpCode() {}
}
