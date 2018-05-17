package org.batfish.main;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
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
        .setHeaderSpace(headerSpace(settings))
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

  private static HeaderSpace headerSpace(ReachabilitySettings settings) {
    // remove constraints on src IP because those are handled elsewhere
    IpSpace nullIpSpace = null;
    return settings
        .getHeaderSpace()
        .toBuilder()
        .setNotSrcIps(nullIpSpace)
        .setSrcIps(nullIpSpace)
        .build();
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
