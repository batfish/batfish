package org.batfish.datamodel;

/** Represents the integer constants used for ICMP Codes. For more information, see RFC-4443. */
public final class Icmp6Code {

  // For Icmp6Type#DESTINATION_UNREACHABLE
  public static final int NO_ROUTE_TO_DESTINATION = 0;
  public static final int COMMUNICATION_WITH_DESTINATION_ADMINISTRATIVELY_PROHIBITED = 1;
  public static final int BEYOND_SCOPE_OF_SOURCE_ADDRESS = 2;
  public static final int ADDRESS_UNREACHABLE = 3;
  public static final int PORT_UNREACHABLE = 4;
  public static final int SOURCE_ADDRESS_FAILED_INGRESS_EGRESS_POLICY = 5;
  public static final int REJECT_ROUTE_TO_DESTINATION = 6;

  // TODO: codes for other types

  private Icmp6Code() {}
}
