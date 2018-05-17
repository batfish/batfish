package org.batfish.main;

import com.google.common.collect.ImmutableList;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AndHeaderSpaceConstraint;
import org.batfish.datamodel.DstIpHeaderSpaceConstraint;
import org.batfish.datamodel.DstPortHeaderSpaceConstraint;
import org.batfish.datamodel.DstProtocolHeaderSpaceConstraint;
import org.batfish.datamodel.HeaderFieldsHeaderSpaceConstraint;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.HeaderSpaceConstraint;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NotHeaderSpaceConstraint;
import org.batfish.datamodel.OrHeaderSpaceConstraint;
import org.batfish.datamodel.SrcIpHeaderSpaceConstraint;
import org.batfish.datamodel.SrcPortHeaderSpaceConstraint;
import org.batfish.datamodel.SrcProtocolHeaderSpaceConstraint;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.ReachabilitySettings;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.DescriptionRegexInterfaceLocationSpecifier;
import org.batfish.specifier.DifferenceNodeSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NameRegexInterfaceLocationSpecifier;
import org.batfish.specifier.NameRegexNodeSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLocationSpecifier;
import org.batfish.specifier.NodeRoleRegexInterfaceLocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoleRegexNodeSpecifier;
import org.batfish.specifier.UnionLocationSpecifier;
import org.batfish.specifier.VrfNameRegexInterfaceLocationSpecifier;

/**
 * Migration code to convert from the old ReachabilitySettings class to the new
 * ReachabilityParameters class.
 *
 * <p>This is intended to preserve the existing behavior of ReachabilityQuestion. Any changes will
 * be made in a new question.
 */
public class ReachabilitySettingsToReachabilityParameters {
  public static ReachabilityParameters convert(ReachabilitySettings settings) {
    return ReachabilityParameters.builder()
        .setActions(settings.getActions())
        .setDestinationSpecifier(destinationLocationSpecifier(settings))
        .setFinalNodesSpecifier(finalNodesSpecifier(settings))
        .setHeaderSpaceConstraint(headerSpaceConstraint(settings))
        .setMaxChunkSize(settings.getMaxChunkSize())
        .setSourceIpSpaceSpecifier(sourceIpSpaceSpecifier(settings))
        .setSourceSpecifier(sourceSpecifier(settings))
        .setSourceNatted(settings.getSrcNatted())
        .setSpecialize(settings.getSpecialize())
        .setTransitNodesSpecifier(transitNodesSpecifier(settings))
        .setUseCompression(settings.getUseCompression())
        .build();
  }

  private static LocationSpecifier destinationLocationSpecifier(ReachabilitySettings settings) {
    /*
     * If the only action is ACCEPT, then we can use the finalNodes as the destination.
     * Otherwise, destination can be anything.
     *
     * For now, just retain the old behavior, which does nothing with destinations.
     */
    return null;
  }

  private static NodeSpecifier differenceNodesSpecifier(
      NodeSpecifier nodeSpecifier, NodeSpecifier notNodeSpecifier) {
    if (nodeSpecifier != null && notNodeSpecifier != null) {
      return new DifferenceNodeSpecifier(nodeSpecifier, notNodeSpecifier);
    } else if (nodeSpecifier != null) {
      return nodeSpecifier;
    } else if (notNodeSpecifier != null) {
      return new DifferenceNodeSpecifier(AllNodesNodeSpecifier.INSTANCE, notNodeSpecifier);
    } else {
      return AllNodesNodeSpecifier.INSTANCE;
    }
  }

  private static NodeSpecifier finalNodesSpecifier(ReachabilitySettings settings) {
    return differenceNodesSpecifier(
        nodesSpecifier(settings.getFinalNodes()), nodesSpecifier(settings.getNotFinalNodes()));
  }

  private static HeaderSpaceConstraint headerSpaceConstraint(ReachabilitySettings settings) {
    HeaderSpace headerSpace = settings.getHeaderSpace();

    ImmutableList.Builder<HeaderSpaceConstraint> conjuncts = ImmutableList.builder();

    // add positive header fields constraint
    conjuncts.add(
        HeaderFieldsHeaderSpaceConstraint.builder()
            .setDscps(headerSpace.getDscps())
            .setDstProtocols(headerSpace.getDstProtocols())
            .setEcns(headerSpace.getEcns())
            .setFragmentOffsets(headerSpace.getFragmentOffsets())
            .setIcmpCodes(headerSpace.getIcmpCodes())
            .setIcmpTypes(headerSpace.getIcmpTypes())
            .setIpProtocols(headerSpace.getIpProtocols())
            .setPacketLengths(headerSpace.getPacketLengths())
            .setSrcProtocols(headerSpace.getSrcProtocols())
            .setStates(headerSpace.getStates())
            .setTcpFlags(headerSpace.getTcpFlags())
            .build());

    // add negative header fields constraint
    conjuncts.add(
        new NotHeaderSpaceConstraint(
            HeaderFieldsHeaderSpaceConstraint.builder()
                .setDscps(headerSpace.getNotDscps())
                .setDstProtocols(headerSpace.getNotDstProtocols())
                .setEcns(headerSpace.getNotEcns())
                .setFragmentOffsets(headerSpace.getNotFragmentOffsets())
                .setIcmpCodes(headerSpace.getNotIcmpCodes())
                .setIcmpTypes(headerSpace.getNotIcmpTypes())
                .setIpProtocols(headerSpace.getNotIpProtocols())
                .setPacketLengths(headerSpace.getNotPacketLengths())
                .setSrcProtocols(headerSpace.getNotSrcProtocols())
                .build()));

    // add srcOrDst constraints
    if (headerSpace.getSrcOrDstIps() != null) {
      conjuncts.add(
          new OrHeaderSpaceConstraint(
              new SrcIpHeaderSpaceConstraint(headerSpace.getSrcOrDstIps()),
              new DstIpHeaderSpaceConstraint(headerSpace.getSrcOrDstIps())));
    }
    if (headerSpace.getSrcOrDstPorts() != null) {
      conjuncts.add(
          new OrHeaderSpaceConstraint(
              new SrcPortHeaderSpaceConstraint(headerSpace.getSrcOrDstPorts()),
              new DstPortHeaderSpaceConstraint(headerSpace.getSrcOrDstPorts())));
    }
    if (headerSpace.getSrcOrDstProtocols() != null) {
      conjuncts.add(
          new OrHeaderSpaceConstraint(
              new SrcProtocolHeaderSpaceConstraint(headerSpace.getSrcOrDstProtocols()),
              new DstProtocolHeaderSpaceConstraint(headerSpace.getSrcOrDstProtocols())));
    }

    HeaderSpaceConstraint and = new AndHeaderSpaceConstraint(conjuncts.build());
    return headerSpace.getNegate() ? new NotHeaderSpaceConstraint(and) : and;
  }

  private static LocationSpecifier locationSpecifier(InterfacesSpecifier ingressInterfaces) {
    if (ingressInterfaces == null) {
      return null;
    }
    switch (ingressInterfaces.getType()) {
      case DESC:
        return new DescriptionRegexInterfaceLocationSpecifier(ingressInterfaces.getRegex());
      case NAME:
        return new NameRegexInterfaceLocationSpecifier(ingressInterfaces.getRegex());
      case VRF:
        return new VrfNameRegexInterfaceLocationSpecifier(ingressInterfaces.getRegex());
      default:
        throw new BatfishException(
            "Unexcepted InterfacesSpecifier type: " + ingressInterfaces.getType());
    }
  }

  private static LocationSpecifier locationSpecifier(
      org.batfish.datamodel.questions.NodesSpecifier ingressNodes) {
    if (ingressNodes == null) {
      return null;
    }
    switch (ingressNodes.getType()) {
      case NAME:
        return new NodeNameRegexInterfaceLocationSpecifier(ingressNodes.getRegex());
      case ROLE:
        return new NodeRoleRegexInterfaceLocationSpecifier(
            ingressNodes.getRoleDimension(), ingressNodes.getRegex());
      default:
        throw new BatfishException("Unexpected NodesSpecifier type: " + ingressNodes.getType());
    }
  }

  private static NodeSpecifier nodesSpecifier(
      org.batfish.datamodel.questions.NodesSpecifier nodesSpecifier) {
    if (nodesSpecifier == null) {
      return null;
    }

    switch (nodesSpecifier.getType()) {
      case NAME:
        return new NameRegexNodeSpecifier(nodesSpecifier.getRegex());
      case ROLE:
        return new RoleRegexNodeSpecifier(
            nodesSpecifier.getRegex(), nodesSpecifier.getRoleDimension());
      default:
        throw new BatfishException("Unexpected NodesSpecifier type: " + nodesSpecifier.getType());
    }
  }

  private static NodeSpecifier transitNodesSpecifier(ReachabilitySettings settings) {
    return differenceNodesSpecifier(
        nodesSpecifier(settings.getTransitNodes()), nodesSpecifier(settings.getNonTransitNodes()));
  }

  private static IpSpaceSpecifier sourceIpSpaceSpecifier(ReachabilitySettings settings) {
    HeaderSpace headerSpace = settings.getHeaderSpace();

    IpSpace srcIps = headerSpace.getSrcIps();
    IpSpace notSrcIps = headerSpace.getNotSrcIps();
    if (srcIps != null && notSrcIps != null) {
      return new ConstantIpSpaceSpecifier(AclIpSpace.difference(srcIps, notSrcIps));
    } else if (srcIps != null) {
      return new ConstantIpSpaceSpecifier(srcIps);
    } else if (notSrcIps != null) {
      return new ConstantIpSpaceSpecifier(
          AclIpSpace.difference(UniverseIpSpace.INSTANCE, notSrcIps));
    }

    // If no explicit IP space constraint, infer a sane source IP space for each location.
    return InferFromLocationIpSpaceSpecifier.INSTANCE;
  }

  private static LocationSpecifier sourceSpecifier(ReachabilitySettings settings) {
    LocationSpecifier ingressInterfaces = locationSpecifier(settings.getIngressInterfaces());
    LocationSpecifier ingressNodes = locationSpecifier(settings.getIngressNodes());

    if (ingressInterfaces != null && ingressNodes != null) {
      return new UnionLocationSpecifier(ingressInterfaces, ingressNodes);
    } else if (ingressInterfaces != null) {
      return ingressInterfaces;
    } else if (ingressNodes != null) {
      return ingressNodes;
    }

    return AllInterfacesLocationSpecifier.INSTANCE;
  }
}
