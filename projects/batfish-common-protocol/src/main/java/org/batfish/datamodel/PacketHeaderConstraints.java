package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A set of constraints on an IPv4 packet header, where each field (i.e., constraint) is a {@link
 * Set} of allowed values.
 *
 * <ul>
 *   <li>{@code null} values indicate no constraint.
 *   <li>Multiple values in a single set indicate a logical OR of multiple allowed values.
 *   <li>Different fields are ANDed together
 *   <li>Empty sets for a field will be rejected, as they imply a false value (i.e., nothing is
 *       allowed)
 * </ul>
 *
 * <p>Upon construction, the constraints are validated, although their intersections are not
 * resolved. For resolution, see {@link #resolveIpProtocols}, and {@link #resolveDstPorts}
 */
public class PacketHeaderConstraints {

  private static final String PROP_APPLICATIONS = "applications";
  private static final String PROP_DSCPS = "dscps";
  private static final String PROP_DST_IPS = "dstIps";
  private static final String PROP_DST_PORTS = "dstPorts";
  private static final String PROP_ECNS = "ecns";
  private static final String PROP_FLOW_STATES = "flowStates";
  private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";
  private static final String PROP_ICMP_CODES = "icmpCodes";
  private static final String PROP_ICMP_TYPES = "icmpTypes";
  private static final String PROP_IP_PROTOCOLS = "ipProtocols";
  private static final String PROP_PACKET_LENGTHS = "packetLengths";
  private static final String PROP_SRC_IPS = "srcIps";
  private static final String PROP_SRC_PORTS = "srcPorts";
  private static final String PROP_TCP_FLAGS = "tcpFlags";

  static final Set<IpProtocol> IP_PROTOCOLS_WITH_PORTS =
      ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.DCCP, IpProtocol.SCTP);

  /*
   * All fields are nullable to allow being "not set", and therefore, unconstrained
   */
  // Ip fields, unlikely to be specified
  @Nullable private final IntegerSpace _dscps;
  @Nullable private final IntegerSpace _ecns;
  @Nullable private final IntegerSpace _packetLengths;
  @Nullable private final Set<FlowState> _flowStates;
  @Nullable private final IntegerSpace _fragmentOffsets;

  // Ip fields, likely to be specified
  @Nullable private final Set<IpProtocol> _ipProtocols;
  @Nullable private final String _srcIp;
  @Nullable private final String _dstIp;

  // ICMP fields
  @Nullable private final IntegerSpace _icmpCode;
  @Nullable private final IntegerSpace _icmpType;

  // UDP/TCP fields
  @Nullable private final IntegerSpace _srcPorts;
  @Nullable private final IntegerSpace _dstPorts;

  // Shorthands for UDP/TCP fields
  // TODO: allow specification of more complex applications, the existing Protocol Enum is limiting.
  @Nullable private final Set<Protocol> _applications;
  @Nullable private final Set<TcpFlagsMatchConditions> _tcpFlags;

  @VisibleForTesting
  static final IntegerSpace VALID_DSCP =
      IntegerSpace.builder().including(Range.closed(0, 63)).build();

  private static final IntegerSpace VALID_ECN =
      IntegerSpace.builder().including(Range.closed(0, 3)).build();
  private static final IntegerSpace VALID_ICMP_CODE_TYPE =
      IntegerSpace.builder().including(Range.closed(0, 255)).build();
  private static final IntegerSpace VALID_PACKET_LENGTH =
      IntegerSpace.builder().including(Range.closed(0, 65535)).build(); // 16 bits
  private static final IntegerSpace VALID_FRAGMENT_OFFSET =
      IntegerSpace.builder().including(Range.closed(0, 8191)).build(); // 13 bits

  @JsonCreator
  @VisibleForTesting
  static PacketHeaderConstraints create(
      @Nullable @JsonProperty(PROP_DSCPS) IntegerSpace.Builder dscps,
      @Nullable @JsonProperty(PROP_ECNS) IntegerSpace.Builder ecns,
      @Nullable @JsonProperty(PROP_PACKET_LENGTHS) IntegerSpace.Builder packetLengths,
      @Nullable @JsonProperty(PROP_FLOW_STATES) Set<FlowState> flowStates,
      @Nullable @JsonProperty(PROP_FRAGMENT_OFFSETS) IntegerSpace.Builder fragmentOffsets,
      @Nullable @JsonProperty(PROP_IP_PROTOCOLS) JsonNode ipProtocols,
      @Nullable @JsonProperty(PROP_SRC_IPS) String srcIps,
      @Nullable @JsonProperty(PROP_DST_IPS) String dstIps,
      @Nullable @JsonProperty(PROP_ICMP_CODES) IntegerSpace.Builder icmpCodes,
      @Nullable @JsonProperty(PROP_ICMP_TYPES) IntegerSpace.Builder icmpTypes,
      @Nullable @JsonProperty(PROP_SRC_PORTS) IntegerSpace.Builder srcPorts,
      @Nullable @JsonProperty(PROP_DST_PORTS) IntegerSpace.Builder dstPorts,
      @Nullable @JsonProperty(PROP_APPLICATIONS) Set<Protocol> applications,
      @Nullable @JsonProperty(PROP_TCP_FLAGS) Set<TcpFlagsMatchConditions> tcpFlags)
      throws IllegalArgumentException {
    return new PacketHeaderConstraints(
        processBuilder(dscps, VALID_DSCP),
        processBuilder(ecns, VALID_ECN),
        processBuilder(packetLengths, VALID_PACKET_LENGTH),
        flowStates,
        processBuilder(fragmentOffsets, VALID_FRAGMENT_OFFSET),
        expandProtocols(parseIpProtocols(ipProtocols)),
        srcIps,
        dstIps,
        processBuilder(icmpCodes, VALID_ICMP_CODE_TYPE),
        processBuilder(icmpTypes, VALID_ICMP_CODE_TYPE),
        processBuilder(srcPorts, IntegerSpace.PORTS),
        processBuilder(dstPorts, IntegerSpace.PORTS),
        applications,
        tcpFlags);
  }

  /**
   * Parse IP protocols fields in backwards-compatible way, accepting either a list strings or a
   * comma-separated string.
   *
   * @param node {@link JsonNode} to parse
   * @return valid string representation to be used in {@link #expandProtocols(String)}
   * @throws IllegalArgumentException if the value is not valid
   */
  @Nullable
  private static String parseIpProtocols(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    } else if (node.isTextual()) {
      return node.textValue();
    } else if (node.isArray()) {
      return String.join(
          ",",
          Streams.stream(node.elements())
              .map(JsonNode::textValue)
              .collect(ImmutableSet.toImmutableSet()));
    } else {
      throw new IllegalArgumentException(
          String.format("Invalid value %s for %s", node.asText(), PROP_IP_PROTOCOLS));
    }
  }

  @Nullable
  @VisibleForTesting
  static Set<IpProtocol> expandProtocols(@Nullable String ipProtocols) {
    if (Strings.isNullOrEmpty(ipProtocols)) {
      return null;
    }
    String[] atoms = ipProtocols.trim().split(",");
    ImmutableSet.Builder<IpProtocol> including = ImmutableSet.builder();
    ImmutableSet.Builder<IpProtocol> excluding = ImmutableSet.builder();
    for (String atom : atoms) {
      String trimmed = atom.trim();
      if (trimmed.startsWith("!")) {
        excluding.add(IpProtocol.fromString(trimmed.replaceFirst("!", "")));
      } else {
        including.add(IpProtocol.fromString(trimmed));
      }
    }

    if (including.build().isEmpty()) {
      including.addAll(Arrays.asList(IpProtocol.values()));
    }

    return ImmutableSet.copyOf(Sets.difference(including.build(), excluding.build()));
  }

  private PacketHeaderConstraints(
      @Nullable IntegerSpace dscps,
      @Nullable IntegerSpace ecns,
      @Nullable IntegerSpace packetLengths,
      @Nullable Set<FlowState> flowStates,
      @Nullable IntegerSpace fragmentOffsets,
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable String srcIp,
      @Nullable String dstIp,
      @Nullable IntegerSpace icmpCode,
      @Nullable IntegerSpace icmpType,
      @Nullable IntegerSpace srcPorts,
      @Nullable IntegerSpace dstPorts,
      @Nullable Set<Protocol> applications,
      @Nullable Set<TcpFlagsMatchConditions> tcpFlags) {
    _dscps = dscps;
    _ecns = ecns;
    _packetLengths = packetLengths;
    _flowStates = flowStates;
    _fragmentOffsets = fragmentOffsets;
    _ipProtocols = ipProtocols;
    _srcIp = srcIp;
    _dstIp = dstIp;
    _icmpCode = icmpCode;
    _icmpType = icmpType;
    _srcPorts = srcPorts;
    _dstPorts = dstPorts;
    _applications = applications;
    _tcpFlags = tcpFlags;
    validate(this);
  }

  @Nullable
  private static IntegerSpace processBuilder(
      @Nullable IntegerSpace.Builder field, @Nonnull IntegerSpace validRange) {
    if (field == null) {
      return null;
    }
    if (field.hasExclusionsOnly()) {
      return field.including(validRange).build();
    }
    return field.build();
  }

  /** Create new object with all fields unconstrained */
  public static PacketHeaderConstraints unconstrained() {
    return new PacketHeaderConstraints(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  @Nullable
  @JsonProperty(PROP_DSCPS)
  public IntegerSpace getDscps() {
    return _dscps;
  }

  @Nullable
  @JsonProperty(PROP_ECNS)
  public IntegerSpace getEcns() {
    return _ecns;
  }

  @Nullable
  @JsonProperty(PROP_PACKET_LENGTHS)
  public IntegerSpace getPacketLengths() {
    return _packetLengths;
  }

  @Nullable
  @JsonProperty(PROP_FLOW_STATES)
  public Set<FlowState> getFlowStates() {
    return _flowStates;
  }

  @Nullable
  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public IntegerSpace getFragmentOffsets() {
    return _fragmentOffsets;
  }

  @Nullable
  public Set<IpProtocol> getIpProtocols() {
    return _ipProtocols;
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  @Nullable
  private String getIpProtocolsString() {
    if (_ipProtocols == null) {
      return null;
    }
    return String.join(
        ",",
        _ipProtocols.stream().map(IpProtocol::toString).collect(ImmutableSet.toImmutableSet()));
  }

  @Nullable
  @JsonProperty(PROP_SRC_IPS)
  public String getSrcIps() {
    return _srcIp;
  }

  @Nullable
  @JsonProperty(PROP_DST_IPS)
  public String getDstIps() {
    return _dstIp;
  }

  @Nullable
  @JsonProperty(PROP_ICMP_CODES)
  public IntegerSpace getIcmpCodes() {
    return _icmpCode;
  }

  @Nullable
  @JsonProperty(PROP_ICMP_TYPES)
  public IntegerSpace getIcmpTypes() {
    return _icmpType;
  }

  @Nullable
  @JsonProperty(PROP_SRC_PORTS)
  public IntegerSpace getSrcPorts() {
    return _srcPorts;
  }

  @Nullable
  @JsonProperty(PROP_DST_PORTS)
  public IntegerSpace getDstPorts() {
    return _dstPorts;
  }

  @Nullable
  @JsonProperty(PROP_APPLICATIONS)
  public Set<Protocol> getApplications() {
    return _applications;
  }

  @Nullable
  @JsonProperty(PROP_TCP_FLAGS)
  public Set<TcpFlagsMatchConditions> getTcpFlags() {
    return _tcpFlags;
  }

  /** Return the set of allowed IP protocols */
  @Nullable
  public Set<IpProtocol> resolveIpProtocols() {
    return resolveIpProtocols(
        getIpProtocols(), getSrcPorts(), getDstPorts(), getApplications(), getTcpFlags());
  }

  /** Return the set of allowed destination port values */
  @Nullable
  public IntegerSpace resolveDstPorts() {
    return resolvePorts(getDstPorts(), getApplications());
  }

  /** Check that constraints contain valid values and do not conflict with each other. */
  private static void validate(@Nonnull PacketHeaderConstraints headerConstraints)
      throws IllegalArgumentException {
    // Ensure IP protocols is not an empty set
    Set<IpProtocol> ipProtocols = headerConstraints.getIpProtocols();
    checkArgument(
        ipProtocols == null || !ipProtocols.isEmpty(), "Cannot have empty set of IpProtocols");
    validateIpFields(headerConstraints);
    validateIcmpFields(headerConstraints);
    validatePortValues(headerConstraints.getSrcPorts());
    validatePortValues(headerConstraints.getDstPorts());
    try {
      areProtocolsAndPortsCompatible(ipProtocols, headerConstraints.getSrcPorts(), null);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Source ports/protocols are incompatible: %s", e.getMessage()));
    }
    try {
      areProtocolsAndPortsCompatible(
          ipProtocols, headerConstraints.getDstPorts(), headerConstraints.getApplications());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Destination ports/protocols are incompatible: %s", e.getMessage()));
    }
    // TODO: validate other fields: fragment offsets, packet lengths, etc.
  }

  private static void validatePortValues(@Nullable IntegerSpace ports)
      throws IllegalArgumentException {
    // Unconstrained is OK
    if (ports == null) {
      return;
    }
    // Reject empty port ranges
    checkArgument(!ports.isEmpty(), "Empty port ranges are not allowed");
    checkArgument(
        ports.difference(IntegerSpace.PORTS).isEmpty(), "Invalid port range specified: %s", ports);
  }

  private static void validateIcmpFields(@Nonnull PacketHeaderConstraints headerConstraints)
      throws IllegalArgumentException {
    // TODO: more stringent validation, not all values 0-255 are valid ICMP codes/types
    Set<IpProtocol> ipProtocols = headerConstraints.getIpProtocols();
    IntegerSpace icmpTypes = headerConstraints.getIcmpTypes();
    if (icmpTypes != null) {
      checkArgument(!icmpTypes.isEmpty(), "Set of ICMP types cannot be empty");
      checkArgument(
          ipProtocols == null || ipProtocols.equals(ImmutableSet.of(IpProtocol.ICMP)),
          "ICMP types specified when ICMP protocol is forbidden in IpProtocols");
      checkArgument(isValidIcmpTypeOrCode(icmpTypes), "Invalid ICMP type range: %s", icmpTypes);
    }

    IntegerSpace icmpCodes = headerConstraints.getIcmpCodes();
    if (icmpCodes != null) {
      checkArgument(!icmpCodes.isEmpty(), "Set of ICMP codes cannot be empty");
      checkArgument(
          ipProtocols == null || ipProtocols.equals(ImmutableSet.of(IpProtocol.ICMP)),
          "ICMP codes specified when ICMP protocol is forbidden in IpProtocols");
      checkArgument(isValidIcmpTypeOrCode(icmpCodes), "Invalid ICMP code range: %s", icmpCodes);
    }
  }

  private static void validateIpFields(@Nonnull PacketHeaderConstraints headerConstraints)
      throws IllegalArgumentException {
    IntegerSpace dscps = headerConstraints.getDscps();
    if (dscps != null) {
      checkArgument(!dscps.isEmpty(), "Empty set of DSCP values is not allowed");
      checkArgument(isValidDscp(dscps), "Invalid DSCP values specified: %s", dscps);
    }

    IntegerSpace ecns = headerConstraints.getEcns();
    if (ecns != null) {
      checkArgument(!ecns.isEmpty(), "Empty set of ECN values is not allowed");
      checkArgument(isValidEcn(ecns), "Invalid value for ECN: %s", ecns);
    }
  }

  /** Check if the subrange represents valid IP DSCP values. */
  @VisibleForTesting
  static boolean isValidDscp(@Nonnull IntegerSpace dscp) {
    return !dscp.isEmpty() && VALID_DSCP.contains(dscp);
  }

  /** Check if the subrange represents valid IP ECN values. */
  @VisibleForTesting
  static boolean isValidEcn(@Nonnull IntegerSpace ecn) {
    return !ecn.isEmpty() && VALID_ECN.contains(ecn);
  }

  @VisibleForTesting
  static boolean isValidIcmpTypeOrCode(@Nonnull IntegerSpace icmpType) {
    return !icmpType.isEmpty() && VALID_ICMP_CODE_TYPE.contains(icmpType);
  }

  @VisibleForTesting
  static boolean areProtocolsAndPortsCompatible(
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable IntegerSpace ports,
      @Nullable Set<Protocol> protocols)
      throws IllegalArgumentException {

    // Ports are only applicable to TCP/UDP
    if (ports != null && ipProtocols != null) {
      checkArgument(
          Sets.difference(ipProtocols, IP_PROTOCOLS_WITH_PORTS).isEmpty(),
          "Cannot combine given ports (%s) and IP protocols (%s)",
          ports,
          ipProtocols);
    }

    // Intersection of IP protocols and higher level protocols should not be empty
    if (ipProtocols != null && protocols != null) {
      // Resolve Ip protocols from higher-level application protocols
      Set<IpProtocol> resolvedIpProtocols =
          protocols.stream().map(Protocol::getIpProtocol).collect(ImmutableSet.toImmutableSet());
      checkArgument(
          !Sets.intersection(ipProtocols, resolvedIpProtocols).isEmpty(),
          "Combination of given IP protocols (%s) and application protocols (%s) cannot be satisfied",
          ipProtocols,
          protocols);
    }

    // Intersection of ports given and ports resolved from higher-level protocols should
    // not be empty
    if (ports != null && protocols != null) {
      IntegerSpace resolvedPorts =
          protocols
              .stream()
              .map(Protocol::getPort)
              .map(IntegerSpace::of)
              .reduce(IntegerSpace::union)
              .orElse(IntegerSpace.EMPTY);

      // for each subrange, run all resolved ports through it, to see if a match occurs
      checkArgument(
          ports.contains(resolvedPorts),
          "Given ports (%s) and protocols (%s) do not overlap",
          ports,
          protocols);
    }
    return true;
  }

  /**
   * Resolve the set of allowed IP protocols given higher-level constraints (on ports and/or
   * protocols).
   *
   * @param ipProtocols specified IP protocols
   * @param srcPorts specified source ports
   * @param dstPorts specified destination ports
   * @param applications specified destination application protocols
   * @param tcpFlags set of TCP flags to match, if any
   * @return a set of {@link IpProtocol}s that are allowed. {@code null} means no constraints.
   * @throws IllegalArgumentException if the set of IP protocols resolves to an empty set.
   */
  @Nullable
  @VisibleForTesting
  static Set<IpProtocol> resolveIpProtocols(
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable IntegerSpace srcPorts,
      @Nullable IntegerSpace dstPorts,
      @Nullable Set<Protocol> applications,
      @Nullable Set<TcpFlagsMatchConditions> tcpFlags)
      throws IllegalArgumentException {
    @Nullable
    Set<IpProtocol> resolvedIpProtocols = ipProtocols; // either already defined or we don't care

    if (srcPorts != null || dstPorts != null) {
      if (ipProtocols != null) {
        resolvedIpProtocols = Sets.intersection(IP_PROTOCOLS_WITH_PORTS, resolvedIpProtocols);
      } else {
        resolvedIpProtocols = IP_PROTOCOLS_WITH_PORTS;
      }
    }

    if (applications != null) {
      Set<IpProtocol> collected =
          applications.stream().map(Protocol::getIpProtocol).collect(ImmutableSet.toImmutableSet());
      if (resolvedIpProtocols == null) {
        resolvedIpProtocols = collected;
      } else {
        resolvedIpProtocols = Sets.intersection(resolvedIpProtocols, collected);
      }
    }

    if (tcpFlags != null) {
      if (ipProtocols != null) {
        resolvedIpProtocols =
            Sets.intersection(resolvedIpProtocols, Collections.singleton(IpProtocol.TCP));
      } else {
        resolvedIpProtocols = Collections.singleton(IpProtocol.TCP);
      }
    }

    if (resolvedIpProtocols == null) {
      return null;
    }
    if (resolvedIpProtocols.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot find resolution between given IP protocols, ports, and application protocols");
    }
    return resolvedIpProtocols;
  }

  /**
   * Resolve set of allowed ports, given high-level constraints on application protocols.
   *
   * @param ports specified ports
   * @param protocols specified application protocols
   * @return a set of allowed port ranges that satisfy the constraints
   */
  @Nullable
  @VisibleForTesting
  static IntegerSpace resolvePorts(
      @Nullable IntegerSpace ports, @Nullable Set<Protocol> protocols) {
    // Don't care
    if (ports == null && protocols == null) {
      return null;
    }

    // Only ports are specified
    if (protocols == null) {
      return ports;
    }

    // Only protocols specified
    if (ports == null) {
      return protocols
          .stream()
          .map(Protocol::getPort)
          .map(IntegerSpace::of)
          .reduce(IntegerSpace::union)
          .orElse(IntegerSpace.EMPTY);
    }

    // Intersect. Protocols are the limiting factor, but they must belong to at least one space
    return protocols
        .stream()
        .map(Protocol::getPort)
        .map(IntegerSpace::of)
        .reduce(IntegerSpace::union)
        .orElse(IntegerSpace.EMPTY)
        .intersection(ports);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PacketHeaderConstraints)) {
      return false;
    }
    PacketHeaderConstraints that = (PacketHeaderConstraints) o;
    return Objects.equals(_dscps, that._dscps)
        && Objects.equals(_ecns, that._ecns)
        && Objects.equals(_packetLengths, that._packetLengths)
        && Objects.equals(_flowStates, that._flowStates)
        && Objects.equals(_fragmentOffsets, that._fragmentOffsets)
        && Objects.equals(_ipProtocols, that._ipProtocols)
        && Objects.equals(_srcIp, that._srcIp)
        && Objects.equals(_dstIp, that._dstIp)
        && Objects.equals(_icmpCode, that._icmpCode)
        && Objects.equals(_icmpType, that._icmpType)
        && Objects.equals(_srcPorts, that._srcPorts)
        && Objects.equals(_dstPorts, that._dstPorts)
        && Objects.equals(_applications, that._applications)
        && Objects.equals(_tcpFlags, that._tcpFlags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _dscps,
        _ecns,
        _packetLengths,
        _flowStates,
        _fragmentOffsets,
        _ipProtocols,
        _srcIp,
        _dstIp,
        _icmpCode,
        _icmpType,
        _srcPorts,
        _dstPorts,
        _applications,
        _tcpFlags);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    /*
     * All fields are nullable to allow being "not set", and therefore, unconstrained
     */
    // Ip fields, unlikely to be specified
    private @Nullable IntegerSpace _dscps;
    private @Nullable IntegerSpace _ecns;
    private @Nullable IntegerSpace _packetLengths;
    private @Nullable Set<FlowState> _flowStates;
    private @Nullable IntegerSpace _fragmentOffsets;
    // Ip fields, likely to be specified
    private @Nullable Set<IpProtocol> _ipProtocols;
    private @Nullable String _srcIps;
    private @Nullable String _dstIps;
    // ICMP fields
    private @Nullable IntegerSpace _icmpCodes;
    private @Nullable IntegerSpace _icmpTypes;
    // UDP/TCP fields
    private @Nullable IntegerSpace _srcPorts;
    private @Nullable IntegerSpace _dstPorts;
    // Shorthands for UDP/TCP fields
    private @Nullable Set<Protocol> _applications;
    private @Nullable Set<TcpFlagsMatchConditions> _tcpFlags;

    private Builder() {}

    public Builder setDscps(@Nullable IntegerSpace dscps) {
      this._dscps = dscps;
      return this;
    }

    public Builder setEcns(@Nullable IntegerSpace ecns) {
      this._ecns = ecns;
      return this;
    }

    public Builder setPacketLengths(@Nullable IntegerSpace packetLengths) {
      this._packetLengths = packetLengths;
      return this;
    }

    public Builder setFlowStates(@Nullable Set<FlowState> flowStates) {
      this._flowStates = flowStates;
      return this;
    }

    public Builder setFragmentOffsets(@Nullable IntegerSpace fragmentOffsets) {
      this._fragmentOffsets = fragmentOffsets;
      return this;
    }

    public Builder setIpProtocols(@Nullable Set<IpProtocol> ipProtocols) {
      this._ipProtocols = ipProtocols;
      return this;
    }

    public Builder setSrcIp(@Nullable String srcIps) {
      this._srcIps = srcIps;
      return this;
    }

    public Builder setDstIp(@Nullable String dstIps) {
      this._dstIps = dstIps;
      return this;
    }

    public Builder setIcmpCodes(@Nullable IntegerSpace icmpCodes) {
      this._icmpCodes = icmpCodes;
      return this;
    }

    public Builder setIcmpTypes(@Nullable IntegerSpace icmpTypes) {
      this._icmpTypes = icmpTypes;
      return this;
    }

    public Builder setSrcPorts(@Nullable IntegerSpace srcPorts) {
      this._srcPorts = srcPorts;
      return this;
    }

    public Builder setDstPorts(@Nullable IntegerSpace dstPorts) {
      this._dstPorts = dstPorts;
      return this;
    }

    public Builder setApplications(@Nullable Set<Protocol> applications) {
      this._applications = applications;
      return this;
    }

    public Builder setTcpFlags(@Nullable Set<TcpFlagsMatchConditions> tcpFlags) {
      this._tcpFlags = tcpFlags;
      return this;
    }

    public PacketHeaderConstraints build() {
      return new PacketHeaderConstraints(
          _dscps,
          _ecns,
          _packetLengths,
          _flowStates,
          _fragmentOffsets,
          _ipProtocols,
          _srcIps,
          _dstIps,
          _icmpCodes,
          _icmpTypes,
          _srcPorts,
          _dstPorts,
          _applications,
          _tcpFlags);
    }
  }
}
