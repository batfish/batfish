package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

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

  // Note that ImmutableMap will throw on key collision, so this guarantees one name per type.
  @VisibleForTesting
  static final Map<Integer, String> NAMES =
      ImmutableMap.<Integer, String>builder()
          .put(ALTERNATE_ADDRESS, "ALTERNATE-ADDRESS")
          .put(CONVERSION_ERROR, "CONVERSION-ERROR")
          .put(DESTINATION_UNREACHABLE, "DESTINATION-UNREACHABLE")
          .put(ECHO_REPLY, "ECHO-REPLY")
          .put(ECHO_REQUEST, "ECHO-REQUEST")
          .put(INFO_REPLY, "INFO-REPLY")
          .put(INFO_REQUEST, "INFO-REQUEST")
          .put(MASK_REPLY, "MASK-REPLY")
          .put(MASK_REQUEST, "MASK-REQUEST")
          .put(MOBILE_REDIRECT, "MOBILE-REDIRECT")
          .put(PARAMETER_PROBLEM, "PARAMETER-PROBLEM")
          .put(REDIRECT_MESSAGE, "REDIRECT-MESSAGE")
          .put(ROUTER_ADVERTISEMENT, "ROUTER-ADVERTISEMENT")
          .put(ROUTER_SOLICITATION, "ROUTER-SOLICITATION")
          .put(SOURCE_QUENCH, "SOURCE-QUENCH")
          .put(TIME_EXCEEDED, "TIME-EXCEEDED")
          .put(TIMESTAMP_REPLY, "TIMESTAMP-REPLY")
          .put(TIMESTAMP_REQUEST, "TIMESTAMP-REQUEST")
          .put(TRACEROUTE, "TRACEROUTE")
          .put(REQUEST_EXTENDED_ECHO, "REQUEST-EXTENDED-ECHO")
          .put(REQUEST_EXTENDED_REPLY, "REQUEST-EXTENDED-REPLY")
          .build();

  /** Returns the RFC-assigned name of the given ICMP Type, if present. */
  public static @Nonnull Optional<String> getName(int type) {
    return Optional.ofNullable(NAMES.get(type));
  }

  private IcmpType() {}
}
