package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.applications.Application;
import org.batfish.datamodel.applications.PortsApplication;
import org.batfish.specifier.NoIpProtocolsIpProtocolSpecifier;
import org.batfish.specifier.SpecifierFactories;

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
  private static final String PROP_DEPRECATED_FLOW_STATES = "flowStates";
  private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";
  private static final String PROP_ICMP_CODES = "icmpCodes";
  private static final String PROP_ICMP_TYPES = "icmpTypes";
  private static final String PROP_IP_PROTOCOLS = "ipProtocols";
  private static final String PROP_PACKET_LENGTHS = "packetLengths";
  private static final String PROP_SRC_IPS = "srcIps";
  private static final String PROP_SRC_PORTS = "srcPorts";
  private static final String PROP_TCP_FLAGS = "tcpFlags";

  /*
   * All fields are nullable to allow being "not set", and therefore, unconstrained
   */
  // Ip fields, unlikely to be specified
  private final @Nullable IntegerSpace _dscps;
  private final @Nullable IntegerSpace _ecns;
  private final @Nullable IntegerSpace _packetLengths;
  private final @Nullable IntegerSpace _fragmentOffsets;

  // Ip fields, likely to be specified
  private final @Nullable Set<IpProtocol> _ipProtocols;
  private final @Nullable String _srcIp;
  private final @Nullable String _dstIp;

  // ICMP fields
  private final @Nullable IntegerSpace _icmpCode;
  private final @Nullable IntegerSpace _icmpType;

  // UDP/TCP fields
  private final @Nullable IntegerSpace _srcPorts;
  private final @Nullable IntegerSpace _dstPorts;

  // Shorthands for UDP/TCP fields
  private final @Nullable String _applications;
  private final @Nullable Set<TcpFlagsMatchConditions> _tcpFlags;

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

  @Deprecated
  @JsonCreator
  private static PacketHeaderConstraints create(
      @JsonProperty(PROP_DSCPS) @Nullable IntegerSpace.Builder dscps,
      @JsonProperty(PROP_ECNS) @Nullable IntegerSpace.Builder ecns,
      @JsonProperty(PROP_PACKET_LENGTHS) @Nullable IntegerSpace.Builder packetLengths,
      @JsonProperty(PROP_FRAGMENT_OFFSETS) @Nullable IntegerSpace.Builder fragmentOffsets,
      @JsonProperty(PROP_IP_PROTOCOLS) @Nullable JsonNode ipProtocols,
      @JsonProperty(PROP_SRC_IPS) @Nullable String srcIps,
      @JsonProperty(PROP_DST_IPS) @Nullable String dstIps,
      @JsonProperty(PROP_ICMP_CODES) @Nullable IntegerSpace.Builder icmpCodes,
      @JsonProperty(PROP_ICMP_TYPES) @Nullable IntegerSpace.Builder icmpTypes,
      @JsonProperty(PROP_SRC_PORTS) @Nullable IntegerSpace.Builder srcPorts,
      @JsonProperty(PROP_DST_PORTS) @Nullable IntegerSpace.Builder dstPorts,
      @JsonProperty(PROP_APPLICATIONS) @Nullable JsonNode applications,
      @JsonProperty(PROP_TCP_FLAGS) @Nullable Set<TcpFlagsMatchConditions> tcpFlags,
      @JsonProperty(PROP_DEPRECATED_FLOW_STATES) @Nullable Object ignored)
      throws IllegalArgumentException {
    return new PacketHeaderConstraints(
        processBuilder(dscps, VALID_DSCP),
        processBuilder(ecns, VALID_ECN),
        processBuilder(packetLengths, VALID_PACKET_LENGTH),
        processBuilder(fragmentOffsets, VALID_FRAGMENT_OFFSET),
        parseIpProtocols(ipProtocols),
        srcIps,
        dstIps,
        processBuilder(icmpCodes, VALID_ICMP_CODE_TYPE),
        processBuilder(icmpTypes, VALID_ICMP_CODE_TYPE),
        processBuilder(srcPorts, IntegerSpace.PORTS),
        processBuilder(dstPorts, IntegerSpace.PORTS),
        parseApplicationJsonToString(applications),
        tcpFlags);
  }

  private PacketHeaderConstraints(
      @Nullable IntegerSpace dscps,
      @Nullable IntegerSpace ecns,
      @Nullable IntegerSpace packetLengths,
      @Nullable IntegerSpace fragmentOffsets,
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable String srcIp,
      @Nullable String dstIp,
      @Nullable IntegerSpace icmpCode,
      @Nullable IntegerSpace icmpType,
      @Nullable IntegerSpace srcPorts,
      @Nullable IntegerSpace dstPorts,
      @Nullable String applications,
      @Nullable Set<TcpFlagsMatchConditions> tcpFlags) {
    _dscps = dscps;
    _ecns = ecns;
    _packetLengths = packetLengths;
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

  /**
   * Applications can be specified as one or a list of terms. Each term can be either a named
   * application such as "ssh", or a protocol name followed optionally by a list of
   * protocol-specific parameters. For TCP/UDP, those parameters are a list of ports or port ranges,
   * so one can specify "ssh" as "tcp/22". For ICMP, those parameters are ICMP types and codes,
   * e.g., both "icmp/echo-request" specifies ICMP echo request, "icmp/1" defines ICMP wity type 1,
   * and "icmp/1/1" defines ICMP type 1 and code 1.
   */
  @VisibleForTesting
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  static Set<Application> parseApplications(@Nullable String input) {
    if (input == null) {
      return null;
    }

    return SpecifierFactories.getApplicationSpecifier(input, SpecifierFactories.ACTIVE_VERSION)
        .resolve();
  }

  static @Nullable String parseApplicationJsonToString(JsonNode applications) {
    if (applications == null || applications.isNull()) {
      return null;
    }

    if (applications.isTextual()) {
      return applications.asText();
    }
    if (applications.isArray()) {
      return Streams.stream(applications.elements())
          .map(JsonNode::textValue)
          .collect(Collectors.joining(","));
    }

    throw new IllegalArgumentException(
        String.format(
            "Application specifier should be a string or a list of strings. Got: %s",
            applications));
  }

  /**
   * IpProtocols can be specified either as 1) a string like "tcp, udp"; or 2) a (Json) list of
   * strings like ["tcp", "udp"]. Negation (e.g., "!tcp" is allowed too.
   */
  @VisibleForTesting
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  static Set<IpProtocol> parseIpProtocols(JsonNode ipProtocols) {
    String input = "";
    if (ipProtocols == null || ipProtocols.isNull()) {
      return null;
    } else if (ipProtocols.isTextual()) {
      input = ipProtocols.asText();
    } else if (ipProtocols.isArray()) {
      input =
          Streams.stream(ipProtocols.elements())
              .map(JsonNode::textValue)
              .collect(Collectors.joining(","));
    } else {
      throw new IllegalArgumentException(
          String.format(
              "IP protocol specifier should be a string or a list of strings. Got: %s",
              ipProtocols));
    }

    return SpecifierFactories.getIpProtocolSpecifierOrDefault(
            input, NoIpProtocolsIpProtocolSpecifier.INSTANCE)
        .resolve();
  }

  private static @Nullable IntegerSpace processBuilder(
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
        null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  @JsonProperty(PROP_DSCPS)
  public @Nullable IntegerSpace getDscps() {
    return _dscps;
  }

  @JsonProperty(PROP_ECNS)
  public @Nullable IntegerSpace getEcns() {
    return _ecns;
  }

  @JsonProperty(PROP_PACKET_LENGTHS)
  public @Nullable IntegerSpace getPacketLengths() {
    return _packetLengths;
  }

  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public @Nullable IntegerSpace getFragmentOffsets() {
    return _fragmentOffsets;
  }

  public @Nullable Set<IpProtocol> getIpProtocols() {
    return _ipProtocols;
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  private @Nullable String getIpProtocolsString() {
    if (_ipProtocols == null) {
      return null;
    }
    return String.join(
        ",",
        _ipProtocols.stream().map(IpProtocol::toString).collect(ImmutableSet.toImmutableSet()));
  }

  @JsonProperty(PROP_SRC_IPS)
  public @Nullable String getSrcIps() {
    return _srcIp;
  }

  @JsonProperty(PROP_DST_IPS)
  public @Nullable String getDstIps() {
    return _dstIp;
  }

  @JsonProperty(PROP_ICMP_CODES)
  public @Nullable IntegerSpace getIcmpCodes() {
    return _icmpCode;
  }

  @JsonProperty(PROP_ICMP_TYPES)
  public @Nullable IntegerSpace getIcmpTypes() {
    return _icmpType;
  }

  @JsonProperty(PROP_SRC_PORTS)
  public @Nullable IntegerSpace getSrcPorts() {
    return _srcPorts;
  }

  @JsonProperty(PROP_DST_PORTS)
  public @Nullable IntegerSpace getDstPorts() {
    return _dstPorts;
  }

  @JsonProperty(PROP_APPLICATIONS)
  public @Nullable String getApplicationsString() {
    return _applications;
  }

  public Set<Application> getApplications() {
    return parseApplications(_applications);
  }

  @JsonProperty(PROP_TCP_FLAGS)
  public @Nullable Set<TcpFlagsMatchConditions> getTcpFlags() {
    return _tcpFlags;
  }

  /** Return the set of allowed IP protocols */
  public @Nullable Set<IpProtocol> resolveIpProtocols() {
    return resolveIpProtocols(
        getIpProtocols(),
        getSrcPorts(),
        getDstPorts(),
        getApplications(),
        getTcpFlags(),
        getIcmpTypes(),
        getIcmpCodes());
  }

  /** Return the set of allowed destination port values */
  public @Nullable IntegerSpace resolveDstPorts() {
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
      @Nullable Set<Application> applications)
      throws IllegalArgumentException {

    // Ports are only applicable to TCP/UDP
    if (ports != null && ipProtocols != null) {
      checkArgument(
          Sets.difference(ipProtocols, IpProtocol.IP_PROTOCOLS_WITH_PORTS).isEmpty(),
          "Cannot combine given ports (%s) and IP protocols (%s)",
          ports,
          ipProtocols);
    }

    // Intersection of IP protocols and higher level applications should not be empty
    if (ipProtocols != null && applications != null) {
      // Resolve Ip protocols from higher-level application protocols
      Set<IpProtocol> resolvedIpProtocols =
          applications.stream()
              .map(Application::getIpProtocol)
              .collect(ImmutableSet.toImmutableSet());
      checkArgument(
          !Sets.intersection(ipProtocols, resolvedIpProtocols).isEmpty(),
          "Combination of given IP protocols (%s) and application protocols (%s) cannot be"
              + " satisfied",
          ipProtocols,
          applications);
    }

    // Intersection of ports given and ports resolved from higher-level protocols should
    // not be empty
    if (ports != null && applications != null) {
      IntegerSpace resolvedPorts =
          applications.stream()
              .filter(application -> application instanceof PortsApplication)
              .flatMap(application -> ((PortsApplication) application).getPorts().stream())
              .map(IntegerSpace::of)
              .reduce(IntegerSpace::union)
              .orElse(IntegerSpace.EMPTY);

      checkArgument(
          !ports.intersection(resolvedPorts).isEmpty(),
          "Given ports (%s) and applications (%s) do not overlap",
          ports,
          applications);
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
  @VisibleForTesting
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  static @Nullable Set<IpProtocol> resolveIpProtocols(
      @Nullable Set<IpProtocol> ipProtocols,
      @Nullable IntegerSpace srcPorts,
      @Nullable IntegerSpace dstPorts,
      @Nullable Set<Application> applications,
      @Nullable Set<TcpFlagsMatchConditions> tcpFlags,
      @Nullable IntegerSpace icmpTypes,
      @Nullable IntegerSpace icmpCodes)
      throws IllegalArgumentException {
    @Nullable

    /* The PHC imposes constraints on the IpProtocol in different ways. collect these constraints
     * and intersect them at the end.
     */
    List<Set<IpProtocol>> constraints = new ArrayList<>();

    if (ipProtocols != null) {
      constraints.add(ipProtocols);
    }

    if (srcPorts != null || dstPorts != null) {
      constraints.add(IpProtocol.IP_PROTOCOLS_WITH_PORTS);
    }

    if (applications != null) {
      constraints.add(
          applications.stream()
              .map(Application::getIpProtocol)
              .collect(ImmutableSet.toImmutableSet()));
    }

    if (tcpFlags != null) {
      constraints.add(ImmutableSet.of(IpProtocol.TCP));
    }

    if (icmpTypes != null || icmpCodes != null) {
      constraints.add(ImmutableSet.of(IpProtocol.ICMP));
    }

    Set<IpProtocol> resolvedIpProtocols =
        constraints.stream().reduce(Sets::intersection).orElse(null);
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
   * Resolve set of allowed ports, given high-level constraints on applications.
   *
   * @param ports specified ports
   * @param applications specified applications
   * @return a set of allowed port ranges that satisfy the constraints
   */
  @VisibleForTesting
  static @Nullable IntegerSpace resolvePorts(
      @Nullable IntegerSpace ports, @Nullable Set<Application> applications) {
    @Nullable
    IntegerSpace portsFromApplications =
        firstNonNull(applications, ImmutableSet.of()).stream()
            .filter(application -> application instanceof PortsApplication)
            .flatMap(application -> ((PortsApplication) application).getPorts().stream())
            .map(IntegerSpace::of)
            .reduce(IntegerSpace::union)
            .orElse(null);
    return intersectNullable(ports, portsFromApplications);
  }

  /** Intersect two {@link Nullable} {@link IntegerSpace IntegerSpaces}. */
  private static @Nullable IntegerSpace intersectNullable(
      @Nullable IntegerSpace is1, @Nullable IntegerSpace is2) {
    if (is1 == null && is2 == null) {
      return null;
    }
    if (is1 == null) {
      return is2;
    }
    if (is2 == null) {
      return is1;
    }
    return is1.intersection(is2);
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
    private @Nullable String _applications;
    private @Nullable Set<TcpFlagsMatchConditions> _tcpFlags;

    private Builder() {}

    public Builder setDscps(@Nullable IntegerSpace dscps) {
      _dscps = dscps;
      return this;
    }

    public Builder setEcns(@Nullable IntegerSpace ecns) {
      _ecns = ecns;
      return this;
    }

    public Builder setPacketLengths(@Nullable IntegerSpace packetLengths) {
      _packetLengths = packetLengths;
      return this;
    }

    public Builder setFragmentOffsets(@Nullable IntegerSpace fragmentOffsets) {
      _fragmentOffsets = fragmentOffsets;
      return this;
    }

    public Builder setIpProtocols(@Nullable Set<IpProtocol> ipProtocols) {
      _ipProtocols = ipProtocols;
      return this;
    }

    public Builder setSrcIp(@Nullable String srcIps) {
      _srcIps = srcIps;
      return this;
    }

    public Builder setDstIp(@Nullable String dstIps) {
      _dstIps = dstIps;
      return this;
    }

    public Builder setIcmpCodes(@Nullable IntegerSpace icmpCodes) {
      _icmpCodes = icmpCodes;
      return this;
    }

    public Builder setIcmpTypes(@Nullable IntegerSpace icmpTypes) {
      _icmpTypes = icmpTypes;
      return this;
    }

    public Builder setSrcPorts(@Nullable IntegerSpace srcPorts) {
      _srcPorts = srcPorts;
      return this;
    }

    public Builder setDstPorts(@Nullable IntegerSpace dstPorts) {
      _dstPorts = dstPorts;
      return this;
    }

    public Builder setApplications(@Nullable String applications) {
      _applications = applications;
      return this;
    }

    public Builder setTcpFlags(@Nullable Set<TcpFlagsMatchConditions> tcpFlags) {
      _tcpFlags = tcpFlags;
      return this;
    }

    public PacketHeaderConstraints build() {
      return new PacketHeaderConstraints(
          _dscps,
          _ecns,
          _packetLengths,
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
