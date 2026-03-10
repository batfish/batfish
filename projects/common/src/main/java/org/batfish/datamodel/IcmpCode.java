package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Represents the RFC-assigned ICMP Codes and names. For more information, see RFC-792, RFC-1122,
 * RFC-1812, and more.
 */
public final class IcmpCode {

  // For IcmpType#DESTINATION_UNREACHABLE
  public static final IcmpCode NETWORK_UNREACHABLE =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 0, "NETWORK-UNREACHABLE");
  public static final IcmpCode HOST_UNREACHABLE =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 1, "HOST-UNREACHABLE");
  public static final IcmpCode PROTOCOL_UNREACHABLE =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 2, "PROTOCOL-UNREACHABLE");
  public static final IcmpCode PORT_UNREACHABLE =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 3, "PORT-UNREACHABLE");
  public static final IcmpCode FRAGMENTATION_NEEDED =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 4, "FRAGMENTATION-NEEDED");
  public static final IcmpCode SOURCE_ROUTE_FAILED =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 5, "SOURCE-ROUTE-FAILED");
  public static final IcmpCode DESTINATION_NETWORK_UNKNOWN =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 6, "DESTINATION-NETWORK-UNKNOWN");
  public static final IcmpCode DESTINATION_HOST_UNKNOWN =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 7, "DESTINATION-HOST-UNKNOWN");
  public static final IcmpCode SOURCE_HOST_ISOLATED =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 8, "SOURCE-HOST-ISOLATED");
  public static final IcmpCode DESTINATION_NETWORK_PROHIBITED =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 9, "DESTINATION-NETWORK-PROHIBITED");
  public static final IcmpCode DESTINATION_HOST_PROHIBITED =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 10, "DESTINATION-HOST-PROHIBITED");
  public static final IcmpCode NETWORK_UNREACHABLE_FOR_TOS =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 11, "NETWORK-UNREACHABLE-FOR-TOS");
  public static final IcmpCode HOST_UNREACHABLE_FOR_TOS =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 12, "HOST-UNREACHABLE-FOR-TOS");
  public static final IcmpCode COMMUNICATION_ADMINISTRATIVELY_PROHIBITED =
      new IcmpCode(
          IcmpType.DESTINATION_UNREACHABLE, 13, "COMMUNICATION-ADMINISTRATIVELY-PROHIBITED");
  public static final IcmpCode HOST_PRECEDENCE_VIOLATION =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 14, "HOST-PRECEDENCE-VIOLATION");
  public static final IcmpCode PRECEDENCE_CUTOFF_IN_EFFECT =
      new IcmpCode(IcmpType.DESTINATION_UNREACHABLE, 15, "PRECEDENCE-CUTOFF-IN-EFFECT");

  // For IcmpType#ECHO_REPLY
  // TODO: there's only one allocated code, should it have the same or separate name?
  public static final IcmpCode ECHO_REPLY = new IcmpCode(IcmpType.ECHO_REPLY, 0, "ECHO-REPLY");
  public static final IcmpCode ECHO_REQUEST =
      new IcmpCode(IcmpType.ECHO_REQUEST, 0, "ECHO-REQUEST");

  // For IcmpType#PARAMETER_PROBLEM
  public static final IcmpCode INVALID_IP_HEADER =
      new IcmpCode(IcmpType.PARAMETER_PROBLEM, 0, "INVALID-IP-HEADER");
  public static final IcmpCode REQUIRED_OPTION_MISSING =
      new IcmpCode(IcmpType.PARAMETER_PROBLEM, 1, "REQUIRED-OPTION-MISSING");
  public static final IcmpCode BAD_LENGTH =
      new IcmpCode(IcmpType.PARAMETER_PROBLEM, 2, "BAD-LENGTH");

  // For IcmpType#REDIRECT_MESSAGE
  public static final IcmpCode NETWORK_ERROR =
      new IcmpCode(IcmpType.REDIRECT_MESSAGE, 0, "NETWORK-ERROR");
  public static final IcmpCode HOST_ERROR =
      new IcmpCode(IcmpType.REDIRECT_MESSAGE, 1, "HOST-ERROR");
  public static final IcmpCode TOS_AND_NETWORK_ERROR =
      new IcmpCode(IcmpType.REDIRECT_MESSAGE, 2, "TOS-AND-NETWORK-ERROR");
  public static final IcmpCode TOS_AND_HOST_ERROR =
      new IcmpCode(IcmpType.REDIRECT_MESSAGE, 3, "TOS-AND-HOST-ERROR");

  // For IcmpType#SOURCE_QUENCH
  // TODO: there's only one allocated code, should it have the same or separate name?
  public static final IcmpCode SOURCE_QUENCH =
      new IcmpCode(IcmpType.SOURCE_QUENCH, 0, "SOURCE-QUENCH");

  // For IcmpType#TIME_EXCEEDED
  public static final IcmpCode TTL_EQ_ZERO_DURING_TRANSIT =
      new IcmpCode(IcmpType.TIME_EXCEEDED, 0, "TTL-EQ-ZERO-DURING-TRANSIT");
  public static final IcmpCode TIME_EXCEEDED_DURING_FRAGMENT_REASSEMBLY =
      new IcmpCode(IcmpType.TIME_EXCEEDED, 1, "TIME-EXCEEDED-DURING-FRAGMENT-REASSEMBLY");

  /**
   * Creates a new {@link IcmpCode} with the given type and code, and possibly a known RFC-assigned
   * name.
   */
  public static IcmpCode of(int type, int code) {
    return knownCode(type, code).orElse(new IcmpCode(type, code, null));
  }

  /** Returns the RFC-assigned name of the given ICMP Type and Code combination, if present. */
  public static Optional<String> getName(int type, int code) {
    return knownCode(type, code).flatMap(IcmpCode::getName);
  }

  ////////

  private static Optional<IcmpCode> knownCode(int type, int code) {
    return ALL.stream().filter(c -> c._type == type && c._code == code).findFirst();
  }

  /** All IcmpCode objects created above; manually maintained; tested for uniqueness. */
  @VisibleForTesting
  static final Set<IcmpCode> ALL =
      ImmutableSet.<IcmpCode>builder()
          // IcmpType#DESTINATION_UNREACHABLE
          .add(NETWORK_UNREACHABLE)
          .add(HOST_UNREACHABLE)
          .add(PROTOCOL_UNREACHABLE)
          .add(PORT_UNREACHABLE)
          .add(FRAGMENTATION_NEEDED)
          .add(SOURCE_ROUTE_FAILED)
          .add(DESTINATION_NETWORK_UNKNOWN)
          .add(DESTINATION_HOST_UNKNOWN)
          .add(SOURCE_HOST_ISOLATED)
          .add(DESTINATION_NETWORK_PROHIBITED)
          .add(DESTINATION_HOST_PROHIBITED)
          .add(NETWORK_UNREACHABLE_FOR_TOS)
          .add(HOST_UNREACHABLE_FOR_TOS)
          .add(COMMUNICATION_ADMINISTRATIVELY_PROHIBITED)
          .add(HOST_PRECEDENCE_VIOLATION)
          .add(PRECEDENCE_CUTOFF_IN_EFFECT)
          // IcmpType#ECHO_REPLY
          .add(ECHO_REPLY)
          // IcmpType#ECHO_REQUEST
          .add(ECHO_REQUEST)
          // For IcmpType#PARAMETER_PROBLEM
          .add(INVALID_IP_HEADER)
          .add(REQUIRED_OPTION_MISSING)
          .add(BAD_LENGTH)
          // For IcmpType#REDIRECT_MESSAGE
          .add(NETWORK_ERROR)
          .add(HOST_ERROR)
          .add(TOS_AND_NETWORK_ERROR)
          .add(TOS_AND_HOST_ERROR)
          // IcmpType#SOURCE_QUENCH
          .add(SOURCE_QUENCH)
          // For IcmpType#TIME_EXCEEDED
          .add(TTL_EQ_ZERO_DURING_TRANSIT)
          .add(TIME_EXCEEDED_DURING_FRAGMENT_REASSEMBLY)
          .build();

  public int getType() {
    return _type;
  }

  public int getCode() {
    return _code;
  }

  public Optional<String> getName() {
    return Optional.ofNullable(_name);
  }

  private IcmpCode(int type, int code, @Nullable String name) {
    _type = type;
    _code = code;
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpCode)) {
      return false;
    }
    IcmpCode icmpCode = (IcmpCode) o;
    return _type == icmpCode._type
        && _code == icmpCode._code
        && Objects.equals(_name, icmpCode._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _code, _name);
  }

  private final int _type;
  private final int _code;
  private final @Nullable String _name;
}
