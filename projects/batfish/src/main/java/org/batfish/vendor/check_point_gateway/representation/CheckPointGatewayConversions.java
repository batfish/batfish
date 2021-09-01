package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.applyOriginalDestinationConstraint;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.applyOriginalServiceConstraint;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.applyOriginalSourceConstraint;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getDestinationTransformationSteps;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getServiceTransformationSteps;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getSourceTransformationSteps;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.NatTranslatedService;
import org.batfish.vendor.check_point_management.NatTranslatedSrcOrDst;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.Service;
import org.batfish.vendor.check_point_management.SrcOrDst;
import org.batfish.vendor.check_point_management.TypedManagementObject;

/** Utility class for Checkpoint conversion methods */
public final class CheckPointGatewayConversions {
  /**
   * Converts the given {@link AddressRange} into an {@link IpSpace}, or returns {@code null} if the
   * address range does not represent an IPv4 space.
   */
  static @Nullable IpSpace toIpSpace(AddressRange addressRange) {
    // TODO Convert IPv6 address ranges
    if (addressRange.getIpv4AddressFirst() == null || addressRange.getIpv4AddressLast() == null) {
      return null;
    }
    return IpRange.range(addressRange.getIpv4AddressFirst(), addressRange.getIpv4AddressLast());
  }

  /** Converts the given {@link Network} into an {@link IpSpace}. */
  static @Nonnull IpSpace toIpSpace(Network network) {
    // TODO Network objects also have a "mask-length4" that we don't currently extract.
    //  If network objects always represent valid Prefixes, it may be simpler to extract
    //  that instead of subnet-mask and convert the network to a PrefixIpSpace.
    // In Network, the mask has bits that matter set, but IpWildcard interprets set mask bits as
    // "don't care". Flip mask to convert to IpWildcard.
    long flippedMask = network.getSubnetMask().asLong() ^ Ip.MAX.asLong();
    return IpWildcard.ipWithWildcardMask(network.getSubnet4(), flippedMask).toIpSpace();
  }

  static @Nonnull HeaderSpace constructHeaderSpace(
      TypedManagementObject src,
      TypedManagementObject dst,
      TypedManagementObject service,
      Warnings warnings) {
    HeaderSpace.Builder hsb = HeaderSpace.builder();
    if (src instanceof SrcOrDst) {
      applyOriginalSourceConstraint((SrcOrDst) src, hsb);
    } else {
      warnings.redFlag(
          String.format(
              "NAT rule original-source %s has unsupported type %s and will be ignored",
              src.getName(), src.getClass()));
    }
    if (dst instanceof SrcOrDst) {
      applyOriginalDestinationConstraint((SrcOrDst) src, hsb);
    } else {
      warnings.redFlag(
          String.format(
              "NAT rule original-destination %s has unsupported type %s and will be ignored",
              dst.getName(), dst.getClass()));
    }
    if (service instanceof Service) {
      applyOriginalServiceConstraint((Service) service, hsb);
    } else {
      warnings.redFlag(
          String.format(
              "NAT rule original-service %s has unsupported type %s and will be ignored",
              service.getName(), service.getClass()));
    }
    return hsb.build();
  }

  static @Nonnull List<TransformationStep> getTransformationSteps(
      TypedManagementObject src,
      TypedManagementObject dst,
      TypedManagementObject service,
      Warnings warnings) {
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    if (src instanceof NatTranslatedSrcOrDst) {
      steps.addAll(getSourceTransformationSteps((NatTranslatedSrcOrDst) src));
    } else {
      warnings.redFlag(
          String.format(
              "NAT rule translated-source %s has unsupported type %s and will be ignored",
              src.getName(), src.getClass()));
    }
    if (dst instanceof NatTranslatedSrcOrDst) {
      steps.addAll(getDestinationTransformationSteps((NatTranslatedSrcOrDst) dst));
    } else {
      warnings.redFlag(
          String.format(
              "NAT rule translated-destination %s has unsupported type %s and will be ignored",
              dst.getName(), dst.getClass()));
    }
    if (service instanceof NatTranslatedService) {
      steps.addAll(getServiceTransformationSteps((NatTranslatedService) service));
    } else {
      warnings.redFlag(
          String.format(
              "NAT rule translated-service %s has unsupported type %s and will be ignored",
              service.getName(), service.getClass()));
    }
    return steps.build();
  }

  private CheckPointGatewayConversions() {}
}
