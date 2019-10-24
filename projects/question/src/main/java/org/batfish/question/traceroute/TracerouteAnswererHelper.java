package org.batfish.question.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.SetFlowStartLocation.setStartLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.question.PacketHeaderContraintToFlowHelper;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationVisitor;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/**
 * Helper for {@link TracerouteAnswerer} and {@link BidirectionalTracerouteAnswerer}. Processes
 * question parameters and constructs {@link Flow Flows} for the backend engine.
 */
public final class TracerouteAnswererHelper {
  private final PacketHeaderConstraints _packetHeaderConstraints;
  private final String _sourceLocationStr;
  private final SpecifierContext _specifierContext;
  private final PacketHeaderContraintToFlowHelper _packetHeaderConstraintToFlowHelper;
  private final LocationVisitor<Boolean> _isActiveLocation =
      new LocationVisitor<Boolean>() {
        private boolean isActiveInterface(String hostname, String ifaceName) {
          Configuration config = _specifierContext.getConfigs().get(hostname);
          if (config == null) {
            return false;
          }
          Interface iface = config.getAllInterfaces().get(ifaceName);
          return iface != null && iface.getActive();
        }

        @Override
        public Boolean visitInterfaceLinkLocation(InterfaceLinkLocation loc) {
          return isActiveInterface(loc.getNodeName(), loc.getInterfaceName());
        }

        @Override
        public Boolean visitInterfaceLocation(InterfaceLocation loc) {
          return isActiveInterface(loc.getNodeName(), loc.getInterfaceName());
        }
      };

  public TracerouteAnswererHelper(
      PacketHeaderConstraints packetHeaderConstraints,
      String sourceLocationStr,
      SpecifierContext specifierContext) {
    _packetHeaderConstraints = packetHeaderConstraints;
    _sourceLocationStr = sourceLocationStr;
    _specifierContext = specifierContext;

    _packetHeaderConstraintToFlowHelper =
        new PacketHeaderContraintToFlowHelper(
            initSourceIpAssignment(
                _sourceLocationStr, _packetHeaderConstraints.getSrcIps(), _specifierContext),
            _specifierContext);
  }

  private static final int TRACEROUTE_PORT = 33434;

  @VisibleForTesting
  static IpSpaceAssignment initSourceIpAssignment(
      String sourceLocation, String sourceIps, SpecifierContext specifierContext) {
    /* construct specifiers */
    LocationSpecifier sourceLocationSpecifier =
        SpecifierFactories.getLocationSpecifierOrDefault(
            sourceLocation, AllInterfacesLocationSpecifier.INSTANCE);

    IpSpaceSpecifier sourceIpSpaceSpecifier =
        SpecifierFactories.getIpSpaceSpecifierOrDefault(
            sourceIps, InferFromLocationIpSpaceSpecifier.INSTANCE);

    /* resolve specifiers */
    Set<Location> sourceLocations = sourceLocationSpecifier.resolve(specifierContext);
    return sourceIpSpaceSpecifier.resolve(sourceLocations, specifierContext);
  }

  private void setSrcIp(
      PacketHeaderConstraints constraints, Location srcLocation, Flow.Builder builder) {
    String headerSrcIp = constraints.getSrcIps();
    Ip srcIp =
        headerSrcIp != null
            ? _packetHeaderConstraintToFlowHelper.inferSrcIpFromHeaderSrcIp(headerSrcIp)
            : _packetHeaderConstraintToFlowHelper.inferSrcIpFromSourceLocation(srcLocation);
    builder.setSrcIp(srcIp);
  }

  private void setDstIp(PacketHeaderConstraints constraints, Flow.Builder builder) {
    String headerDstIp = constraints.getDstIps();
    checkArgument(headerDstIp != null, "Cannot perform traceroute without a destination");
    IpSpaceAssignment dstIps = _packetHeaderConstraintToFlowHelper.resolverHeaderIp(headerDstIp);
    checkArgument(
        dstIps.getEntries().size() == 1,
        "Specified destination '%s' resolves to more than one IP",
        headerDstIp);
    Optional<Ip> dstIp =
        _packetHeaderConstraintToFlowHelper.pickRepresentativeFromIpSpaceAssignment(
            dstIps.getEntries().iterator().next());
    checkArgument(dstIp.isPresent(), "Specified destination '%s' has no IPs.", headerDstIp);
    builder.setDstIp(dstIp.get());
  }

  /**
   * Generate a flow builder given some set of packet header constraints.
   *
   * @param constraints {@link PacketHeaderConstraints}
   * @throws IllegalArgumentException if the {@code constraints} cannot be resolved to a single
   *     value.
   */
  private Flow.Builder headerConstraintsToFlow(
      PacketHeaderConstraints constraints, Location srcLocation) throws IllegalArgumentException {
    Flow.Builder builder = PacketHeaderConstraintsUtil.toFlow(constraints);

    // Extract and source IP from header constraints,
    setSrcIp(constraints, srcLocation, builder);
    setDstIp(constraints, builder);

    // Set defaults for protocol, and ports and packet lengths:
    if (builder.getIpProtocol() == null) {
      builder.setIpProtocol(IpProtocol.UDP);
    }
    // set SYN if the user didn't specify any TCP flags
    if (builder.getIpProtocol() == IpProtocol.TCP && constraints.getTcpFlags() == null) {
      builder.setTcpFlagsSyn(1);
    }
    if (builder.getDstPort() == null) {
      builder.setDstPort(TRACEROUTE_PORT);
    }
    if (builder.getSrcPort() == null) {
      builder.setSrcPort(NamedPort.EPHEMERAL_LOWEST.number());
    }
    return builder;
  }

  /** Generate a set of flows to do traceroute */
  @VisibleForTesting
  Set<Flow> getFlows(String tag) {
    Set<Location> srcLocations =
        SpecifierFactories.getLocationSpecifierOrDefault(
                _sourceLocationStr, AllInterfacesLocationSpecifier.INSTANCE)
            .resolve(_specifierContext).stream()
            .filter(_isActiveLocation::visit)
            .collect(Collectors.toSet());

    checkArgument(
        !srcLocations.isEmpty(), "Found no active locations matching %s", _sourceLocationStr);

    ImmutableSet.Builder<Flow> setBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<String> allProblems = ImmutableSet.builder();

    // Perform cross-product of all locations to flows
    for (Location srcLocation : srcLocations) {
      try {
        Flow.Builder flowBuilder = headerConstraintsToFlow(_packetHeaderConstraints, srcLocation);
        setStartLocation(_specifierContext.getConfigs(), flowBuilder, srcLocation);
        flowBuilder.setTag(tag);
        setBuilder.add(flowBuilder.build());
      } catch (IllegalArgumentException e) {
        // Try to ignore silently if possible
        allProblems.add(e.getMessage());
      }
    }

    Set<Flow> flows = setBuilder.build();
    checkArgument(
        !flows.isEmpty(),
        "Could not construct a flow for traceroute. Found issues: %s",
        String.join(",", allProblems.build()));
    return flows;
  }
}
