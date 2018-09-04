package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
 * resolved. For resolution, see {@link #resolveIpProtocols}, {@link #resolveSrcPorts}, and {@link
 * #resolveDstPorts}
 */
public class PacketHeaderConstraints {

  private static final String PROP_DSCPS = "dscps";

  private static final String PROP_DST_IPS = "dstIps";

  private static final String PROP_DST_PORTS = "dstPorts";

  private static final String PROP_DST_PROTOCOLS = "dstProtocols";

  private static final String PROP_ECNS = "ecns";

  private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";

  private static final String PROP_ICMP_CODES = "icmpCodes";

  private static final String PROP_ICMP_TYPES = "icmpTypes";

  private static final String PROP_IP_PROTOCOLS = "ipProtocols";

  private static final String PROP_PACKET_LENGTHS = "packetLengths";

  private static final String PROP_SRC_IPS = "srcIps";

  private static final String PROP_SRC_PORTS = "srcPorts";

  private static final String PROP_SRC_PROTOCOLS = "srcProtocols";

  private static final String PROP_FLOW_STATES = "flowStates";

  static final Set<IpProtocol> IP_PROTOCOLS_WITH_PORTS =
      ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.DCCP, IpProtocol.SCTP);

  /*
   * All fields are nullable to allow being "not set", and therefore, unconstrained
   */
  // Ip fields, unlikely to be specified
  @Nullable private final Set<SubRange> _dscps;

  @Nullable private final Set<SubRange> _ecns;

  @Nullable private final Set<SubRange> _packetLengths;

  @Nullable private final Set<FlowState> _flowStates;

  @Nullable private final Set<SubRange> _fragmentOffsets;

  // Ip fields, likely to be specified
  @Nullable private final Set<IpProtocol> _ipProtocols;

  @Nullable private final String _srcIp;

  @Nullable private final String _dstIp;

  // ICMP fields
  @Nullable private final Set<SubRange> _icmpCode;

  @Nullable private final Set<SubRange> _icmpType;

  // UDP/TCP fields
  @Nullable private final Set<SubRange> _srcPorts;

  @Nullable private final Set<SubRange> _dstPorts;

  // Shorthands for UDP/TCP fields
  @Nullable private final Set<Protocol> _dstProtocols;

  @Nullable private final Set<Protocol> _srcProtocols;
  private static final SubRange ALLOWED_PORTS = new SubRange(0, 65535);

  // TODO: add tcp flags

  @JsonCreator
  private PacketHeaderConstraints(
      @Nullable @JsonProperty(PROP_DSCPS) Set<SubRange> dscps,
      @Nullable @JsonProperty(PROP_ECNS) Set<SubRange> ecns,
      @Nullable @JsonProperty(PROP_PACKET_LENGTHS) Set<SubRange> packetLengths,
      @Nullable @JsonProperty(PROP_FLOW_STATES) Set<FlowState> flowStates,
      @Nullable @JsonProperty(PROP_FRAGMENT_OFFSETS) Set<SubRange> fragmentOffsets,
      @Nullable @JsonProperty(PROP_IP_PROTOCOLS) Set<IpProtocol> ipProtocols,
      @Nullable @JsonProperty(PROP_SRC_IPS) String srcIps,
      @Nullable @JsonProperty(PROP_DST_IPS) String dstIps,
      @Nullable @JsonProperty(PROP_ICMP_CODES) Set<SubRange> icmpCodes,
      @Nullable @JsonProperty(PROP_ICMP_TYPES) Set<SubRange> icmpTypes,
      @Nullable @JsonProperty(PROP_SRC_PORTS) Set<SubRange> srcPorts,
      @Nullable @JsonProperty(PROP_DST_PORTS) Set<SubRange> dstPorts,
      @Nullable @JsonProperty(PROP_SRC_PROTOCOLS) Set<Protocol> srcProtocols,
      @Nullable @JsonProperty(PROP_DST_PROTOCOLS) Set<Protocol> dstProtocols)
      throws IllegalArgumentException {
    _dscps = dscps;
    _ecns = ecns;
    _packetLengths = packetLengths;
    _flowStates = flowStates;
    _fragmentOffsets = fragmentOffsets;
    _ipProtocols = ipProtocols;
    _srcIp = srcIps;
    _dstIp = dstIps;
    _icmpCode = icmpCodes;
    _icmpType = icmpTypes;
    _srcPorts = srcPorts;
    _dstPorts = dstPorts;
    _dstProtocols = dstProtocols;
    _srcProtocols = srcProtocols;
    validate(this);
  }

  /** Create new object with all fields unconstrained */
  public static PacketHeaderConstraints unconstrained() {
    return new PacketHeaderConstraints(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  @Nullable
  @JsonProperty(PROP_DSCPS)
  public Set<SubRange> getDscps() {
    return _dscps;
  }

  @Nullable
  @JsonProperty(PROP_ECNS)
  public Set<SubRange> getEcns() {
    return _ecns;
  }

  @Nullable
  @JsonProperty(PROP_PACKET_LENGTHS)
  public Set<SubRange> getPacketLengths() {
    return _packetLengths;
  }

  @Nullable
  @JsonProperty(PROP_FLOW_STATES)
  public Set<FlowState> getFlowStates() {
    return _flowStates;
  }

  @Nullable
  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public Set<SubRange> getFragmentOffsets() {
    return _fragmentOffsets;
  }

  @Nullable
  @JsonProperty(PROP_IP_PROTOCOLS)
  public Set<IpProtocol> getIpProtocols() {
    return _ipProtocols;
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
  public Set<SubRange> getIcmpCodes() {
    return _icmpCode;
  }

  @Nullable
  @JsonProperty(PROP_ICMP_TYPES)
  public Set<SubRange> getIcmpTypes() {
    return _icmpType;
  }

  @Nullable
  @JsonProperty(PROP_SRC_PORTS)
  public Set<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  @Nullable
  @JsonProperty(PROP_DST_PORTS)
  public Set<SubRange> getDstPorts() {
    return _dstPorts;
  }

  @Nullable
  @JsonProperty(PROP_SRC_PROTOCOLS)
  public Set<Protocol> getSrcProtocols() {
    return _srcProtocols;
  }

  @Nullable
  @JsonProperty(PROP_DST_PROTOCOLS)
  public Set<Protocol> getDstProtocols() {
    return _dstProtocols;
  }

  /** Return the set of allowed IP protocols */
  @Nullable
  public Set<IpProtocol> resolveIpProtocols() {
    return resolveIpProtocols(
        getIpProtocols(), getSrcPorts(), getDstPorts(), getSrcProtocols(), getDstProtocols());
  }

  /** Return the set of allowed source port values */
  @Nullable
  public Set<SubRange> resolveSrcPorts() {
    return resolvePorts(getSrcPorts(), getSrcProtocols());
  }

  /** Return the set of allowed destination port values */
  @Nullable
  public Set<SubRange> resolveDstPorts() {
    return resolvePorts(getDstPorts(), getDstProtocols());
  }

  /** Check that constraints contain valid values and do not conflict with each other. */
  private static void validate(@Nonnull PacketHeaderConstraints headerConstraints)
      throws IllegalArgumentException {
    // Ensure IP protocols is not an empty set
    Set<IpProtocol> ipProtocols = headerConstraints.getIpProtocols();
    if (ipProtocols != null) {
      checkArgument(!ipProtocols.isEmpty(), "Cannot have empty set of IpProtocols");
    }
    validateIpFields(headerConstraints);
    validateIcmpFields(headerConstraints);
    validatePortValues(headerConstraints.getSrcPorts());
    validatePortValues(headerConstraints.getDstPorts());
    try {
      areProtocolsAndPortsCompatible(
          headerConstraints.getIpProtocols(),
          headerConstraints.getSrcPorts(),
          headerConstraints.getSrcProtocols());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Source ports/protocols are incompatible: %s", e.getMessage()));
    }
    try {
      areProtocolsAndPortsCompatible(
          headerConstraints.getIpProtocols(),
          headerConstraints.getDstPorts(),
          headerConstraints.getDstProtocols());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Destination ports/protocols are incompatible: %s", e.getMessage()));
    }
    // TODO: validate other fields: fragment offsets, packet lengths, etc.
  }

  private static void validatePortValues(@Nullable Set<SubRange> ports)
      throws IllegalArgumentException {
    // Reject empty lists and empty port ranges
    if (ports == null) {
      return;
    }
    if (ports.isEmpty()
        || ports.stream().map(SubRange::isEmpty).allMatch(Predicate.isEqual(true))) {
      throw new IllegalArgumentException("Empty port ranges are not allowed");
    }
    Optional<SubRange> invalidRange =
        ports.stream().filter(portRange -> !ALLOWED_PORTS.contains(portRange)).findFirst();
    checkArgument(
        !invalidRange.isPresent(),
        "Port range %s is outside of the allowed range %s",
        invalidRange,
        ALLOWED_PORTS);
  }

  private static void validateIcmpFields(@Nonnull PacketHeaderConstraints headerConstraints)
      throws IllegalArgumentException {
    Set<IpProtocol> ipProtocols = headerConstraints.getIpProtocols();
    Set<SubRange> icmpTypes = headerConstraints.getIcmpTypes();
    if (icmpTypes != null) {
      checkArgument(!icmpTypes.isEmpty(), "Set of ICMP types cannot be empty");
      checkArgument(
          ipProtocols == null || ipProtocols.equals(ImmutableSet.of(IpProtocol.ICMP)),
          "ICMP types specified when ICMP protocol is forbidden in IpProtocols");
      Optional<SubRange> invalidRange =
          icmpTypes.stream().filter(types -> !isValidIcmpTypeOrCode(types)).findFirst();
      checkArgument(!invalidRange.isPresent(), "Invalid ICMP type range: %s", invalidRange);
    }

    Set<SubRange> icmpCodes = headerConstraints.getIcmpCodes();
    if (icmpCodes != null) {
      checkArgument(!icmpCodes.isEmpty(), "Set of ICMP codes cannot be empty");
      checkArgument(
          ipProtocols == null || ipProtocols.equals(ImmutableSet.of(IpProtocol.ICMP)),
          "ICMP codes specified when ICMP protocol is forbidden in IpProtocols");
      Optional<SubRange> invalidRange =
          icmpCodes.stream().filter(codes -> !isValidIcmpTypeOrCode(codes)).findFirst();
      checkArgument(!invalidRange.isPresent(), "Invalid ICMP code range: %s", invalidRange);
    }
  }

  private static void validateIpFields(@Nonnull PacketHeaderConstraints headerConstraints)
      throws IllegalArgumentException {
    Set<SubRange> dscps = headerConstraints.getDscps();
    if (dscps != null) {
      checkArgument(!dscps.isEmpty(), "Empty set of DSCP values is not allowed");
      Optional<SubRange> invalidRange =
          dscps.stream().filter(dscp -> !isValidDscp(dscp)).findFirst();
      checkArgument(!invalidRange.isPresent(), "Invalid value for DSCP: %s", invalidRange);
    }

    Set<SubRange> ecns = headerConstraints.getEcns();
    if (ecns != null) {
      checkArgument(!ecns.isEmpty(), "Empty set of ECN values is not allowed");
      Optional<SubRange> invalidRange = ecns.stream().filter(ecn -> !isValidEcn(ecn)).findFirst();
      checkArgument(!invalidRange.isPresent(), "Invalid value for ECN: %s", invalidRange);
    }
  }

  /** Check if the subrange represents valid IP DSCP values. */
  @VisibleForTesting
  static boolean isValidDscp(@Nonnull SubRange dscp) {
    SubRange allowed = new SubRange(0, 63); // 6 bits in header
    return !dscp.isEmpty() && allowed.contains(dscp);
  }

  /** Check if the subrange represents valid IP ECN values. */
  @VisibleForTesting
  static boolean isValidEcn(@Nonnull SubRange ecn) {
    SubRange allowed = new SubRange(0, 3); // 2 bits in header
    return !ecn.isEmpty() && allowed.contains(ecn);
  }

  @VisibleForTesting
  static boolean isValidIcmpTypeOrCode(@Nonnull SubRange icmpType) {
    SubRange allowed = new SubRange(0, 255); // 8 bits in header
    return !icmpType.isEmpty() && allowed.contains(icmpType);
  }

  @VisibleForTesting
  static boolean areProtocolsAndPortsCompatible(
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable Set<SubRange> ports,
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
      Set<Integer> resolvedPorts =
          protocols.stream().map(Protocol::getPort).collect(ImmutableSet.toImmutableSet());

      // for each subrange, run all resolved ports through it, to see if a match occurs
      checkArgument(
          ports
              .stream()
              .flatMap(subrange -> resolvedPorts.stream().map(subrange::includes))
              .anyMatch(Predicate.isEqual(true)),
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
   * @param srcProtocols specified source application protocols
   * @param dstProtocols specified destination application protocols
   * @return a set of {@link IpProtocol}s that are allowed. {@code null} means no constraints.
   * @throws IllegalArgumentException if the set of IP protocols resolves to an empty set.
   */
  @Nullable
  @VisibleForTesting
  static Set<IpProtocol> resolveIpProtocols(
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable Set<SubRange> srcPorts,
      @Nullable Set<SubRange> dstPorts,
      @Nullable Set<Protocol> srcProtocols,
      @Nullable Set<Protocol> dstProtocols)
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

    if (srcProtocols != null) {
      Set<IpProtocol> collected =
          srcProtocols.stream().map(Protocol::getIpProtocol).collect(ImmutableSet.toImmutableSet());
      if (resolvedIpProtocols == null) {
        resolvedIpProtocols = collected;
      } else {
        resolvedIpProtocols = Sets.intersection(resolvedIpProtocols, collected);
      }
    }

    if (dstProtocols != null) {
      Set<IpProtocol> collected =
          dstProtocols.stream().map(Protocol::getIpProtocol).collect(ImmutableSet.toImmutableSet());
      if (resolvedIpProtocols == null) {
        resolvedIpProtocols = collected;
      } else {
        resolvedIpProtocols = Sets.intersection(resolvedIpProtocols, collected);
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
  static Set<SubRange> resolvePorts(
      @Nullable Set<SubRange> ports, @Nullable Set<Protocol> protocols) {
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
          .map(port -> new SubRange(port, port))
          .collect(ImmutableSet.toImmutableSet());
    }

    // Intersect. Protocols are the limiting factor, but they must belong to at least one subrange
    return protocols
        .stream()
        .map(Protocol::getPort)
        .filter(port -> ports.stream().map(r -> r.includes(port)).anyMatch(Predicate.isEqual(true)))
        .map(port -> new SubRange(port, port))
        .collect(ImmutableSet.toImmutableSet());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    /*
     * All fields are nullable to allow being "not set", and therefore, unconstrained
     */
    // Ip fields, unlikely to be specified
    private @Nullable Set<SubRange> _dscps;
    private @Nullable Set<SubRange> _ecns;
    private @Nullable Set<SubRange> _packetLengths;
    private @Nullable Set<FlowState> _flowStates;
    private @Nullable Set<SubRange> _fragmentOffsets;
    // Ip fields, likely to be specified
    private @Nullable Set<IpProtocol> _ipProtocols;
    private @Nullable String _srcIps;
    private @Nullable String _dstIps;
    // ICMP fields
    private @Nullable Set<SubRange> _icmpCodes;
    private @Nullable Set<SubRange> _icmpTypes;
    // UDP/TCP fields
    private @Nullable Set<SubRange> _srcPorts;
    private @Nullable Set<SubRange> _dstPorts;
    // Shorthands for UDP/TCP fields
    private @Nullable Set<Protocol> _dstProtocols;
    private @Nullable Set<Protocol> _srcProtocols;

    private Builder() {}

    public Builder setDscps(@Nullable Set<SubRange> dscps) {
      this._dscps = dscps;
      return this;
    }

    public Builder setEcns(@Nullable Set<SubRange> ecns) {
      this._ecns = ecns;
      return this;
    }

    public Builder setPacketLengths(@Nullable Set<SubRange> packetLengths) {
      this._packetLengths = packetLengths;
      return this;
    }

    public Builder setFlowStates(@Nullable Set<FlowState> flowStates) {
      this._flowStates = flowStates;
      return this;
    }

    public Builder setFragmentOffsets(@Nullable Set<SubRange> fragmentOffsets) {
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

    public Builder setIcmpCodes(@Nullable Set<SubRange> icmpCodes) {
      this._icmpCodes = icmpCodes;
      return this;
    }

    public Builder setIcmpTypes(@Nullable Set<SubRange> icmpTypes) {
      this._icmpTypes = icmpTypes;
      return this;
    }

    public Builder setSrcPorts(@Nullable Set<SubRange> srcPorts) {
      this._srcPorts = srcPorts;
      return this;
    }

    public Builder setDstPorts(@Nullable Set<SubRange> dstPorts) {
      this._dstPorts = dstPorts;
      return this;
    }

    public Builder setSrcProtocols(Set<Protocol> srcProtocols) {
      this._srcProtocols = srcProtocols;
      return this;
    }

    public Builder setDstProtocols(@Nullable Set<Protocol> dstProtocols) {
      this._dstProtocols = dstProtocols;
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
          _srcProtocols,
          _dstProtocols);
    }
  }
}
