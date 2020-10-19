package org.batfish.question.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.SetFlowStartLocation.setStartLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDFlowConstraintGenerator;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.phc_to_flow.IpFieldExtractorContext;
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
  private final IpFieldExtractorContext _packetHeaderConstraintToFlowHelper;
  private final IpSpaceAssignment _srcIpAssignment;
  private final Builder _flowBuilder;
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
    _srcIpAssignment =
        initSourceIpAssignment(
            _sourceLocationStr, _packetHeaderConstraints.getSrcIps(), _specifierContext);
    _packetHeaderConstraintToFlowHelper =
        new IpFieldExtractorContext(_srcIpAssignment, _specifierContext);

    _flowBuilder = initFlowBuilder(_packetHeaderConstraints);
    setDstIp(_packetHeaderConstraints, _flowBuilder);
  }

  static Flow.Builder initFlowBuilder(PacketHeaderConstraints phc) {
    BDDPacket pkt = new BDDPacket();
    return pkt.getFlow(
            PacketHeaderConstraintsUtil.toBDD(
                pkt, phc, UniverseIpSpace.INSTANCE, UniverseIpSpace.INSTANCE),
            BDDFlowConstraintGenerator.FlowPreference.TRACEROUTE)
        .orElseThrow(() -> new BatfishException("could not convert header constraints to flow"));
  }

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
    Ip dstIp = _packetHeaderConstraintToFlowHelper.inferDstIpFromHeaderDstIp(headerDstIp);
    builder.setDstIp(dstIp);
  }

  /** Generate a set of flows to do traceroute */
  @VisibleForTesting
  Set<Flow> getFlows() {
    Set<Location> srcLocations =
        SpecifierFactories.getLocationSpecifierOrDefault(
                _sourceLocationStr, AllInterfacesLocationSpecifier.INSTANCE)
            .resolve(_specifierContext)
            .stream()
            .filter(_isActiveLocation::visit)
            .collect(Collectors.toSet());

    checkArgument(
        !srcLocations.isEmpty(), "Found no active locations matching %s", _sourceLocationStr);

    ImmutableSet.Builder<Flow> setBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<String> allProblems = ImmutableSet.builder();

    // Perform cross-product of all locations to flows
    for (Location srcLocation : srcLocations) {
      try {
        Flow.Builder flowBuilder = _flowBuilder;
        // Extract and source IP from header constraints,
        setSrcIp(_packetHeaderConstraints, srcLocation, flowBuilder);
        setStartLocation(_specifierContext.getConfigs(), flowBuilder, srcLocation);
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
