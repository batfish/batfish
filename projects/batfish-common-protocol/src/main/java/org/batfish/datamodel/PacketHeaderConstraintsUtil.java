package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.applicationsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.dscpsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.dstPortsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.ecnsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.fragmentOffsetsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.icmpCodeToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.icmpTypeToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.ipProtocolsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.packetLengthToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.srcPortsToAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsToAclLineMatchExprUtils.tcpFlagsToAclLineMatchExpr;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/** Utility class to convert {@link PacketHeaderConstraints} to other internal representations. */
@ParametersAreNonnullByDefault
public class PacketHeaderConstraintsUtil {

  public static final int DEFAULT_PACKET_LENGTH = 512;

  /**
   * Convert {@link PacketHeaderConstraints} to an {@link AclLineMatchExpr}.
   *
   * @param phc the packet header constraints
   * @param srcIpSpace Resolved source IP space
   * @param dstIpSpace Resolved destination IP space
   */
  public static AclLineMatchExpr toAclLineMatchExpr(
      PacketHeaderConstraints phc, @Nullable IpSpace srcIpSpace, @Nullable IpSpace dstIpSpace) {
    List<AclLineMatchExpr> conjuncts =
        Stream.of(
                srcIpSpace == null
                    ? null
                    : match(HeaderSpace.builder().setSrcIps(srcIpSpace).build()),
                dstIpSpace == null
                    ? null
                    : match(HeaderSpace.builder().setDstIps(dstIpSpace).build()),
                dscpsToAclLineMatchExpr(phc.getDscps()),
                ecnsToAclLineMatchExpr(phc.getEcns()),
                packetLengthToAclLineMatchExpr(phc.getPacketLengths()),
                fragmentOffsetsToAclLineMatchExpr(phc.getFragmentOffsets()),
                ipProtocolsToAclLineMatchExpr(phc.getIpProtocols()),
                icmpCodeToAclLineMatchExpr(phc.getIcmpCodes()),
                icmpTypeToAclLineMatchExpr(phc.getIcmpTypes()),
                srcPortsToAclLineMatchExpr(phc.getSrcPorts()),
                dstPortsToAclLineMatchExpr(phc.getDstPorts()),
                applicationsToAclLineMatchExpr(phc.getApplications()),
                tcpFlagsToAclLineMatchExpr(phc.getTcpFlags()))
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());

    return and(conjuncts);
  }

  /**
   * Convert {@link PacketHeaderConstraints} to an {@link AclLineMatchExpr}.
   *
   * @param phc the packet header constraints
   */
  public static AclLineMatchExpr toAclLineMatchExpr(PacketHeaderConstraints phc) {
    return toAclLineMatchExpr(phc, null, null);
  }

  /**
   * Convert given {@link PacketHeaderConstraints} to a BDD
   *
   * @param pkt the {@link BDDPacket} to use
   * @param phc the packet header constraints
   * @param srcIpSpace Resolved source IP space
   * @param dstIpSpace Resolved destination IP space
   */
  public static BDD toBDD(
      BDDPacket pkt, PacketHeaderConstraints phc, IpSpace srcIpSpace, IpSpace dstIpSpace) {
    return toBDD(pkt, phc, ImmutableMap.of(), srcIpSpace, dstIpSpace);
  }

  /**
   * Convert given {@link PacketHeaderConstraints} to a BDD, also taking into account named IP
   * spaces
   *
   * @param pkt the {@link BDDPacket} to use
   * @param phc the packet header constraints
   * @param namedIpSpaces map of named IP spaces
   * @param srcIpSpace Resolved source IP space
   * @param dstIpSpace Resolved destination IP space
   */
  public static BDD toBDD(
      BDDPacket pkt,
      PacketHeaderConstraints phc,
      Map<String, IpSpace> namedIpSpaces,
      IpSpace srcIpSpace,
      IpSpace dstIpSpace) {

    IpAccessListToBddImpl converter =
        new IpAccessListToBddImpl(
            pkt, BDDSourceManager.empty(pkt), ImmutableMap.of(), namedIpSpaces);

    return converter.toBdd(toAclLineMatchExpr(phc, srcIpSpace, dstIpSpace));
  }

  private static SortedSet<SubRange> extractSubranges(@Nullable IntegerSpace space) {
    if (space == null || space.isEmpty()) {
      return ImmutableSortedSet.of();
    }
    return ImmutableSortedSet.copyOf(space.getSubRanges());
  }

  /**
   * Convert packet header constraints to a {@link HeaderSpace.Builder}
   *
   * <p><b>Does not resolve/set source and destination IPs</b>
   */
  public static HeaderSpace.Builder toHeaderSpaceBuilder(PacketHeaderConstraints phc) {
    // Note: headerspace builder does not accept nulls, so we have to convert nulls to empty sets
    HeaderSpace.Builder builder =
        HeaderSpace.builder()
            .setIpProtocols(firstNonNull(phc.resolveIpProtocols(), ImmutableSortedSet.of()))
            .setSrcPorts(extractSubranges(phc.getSrcPorts()))
            .setDstPorts(extractSubranges(phc.resolveDstPorts()))
            .setIcmpCodes(extractSubranges(phc.getIcmpCodes()))
            .setIcmpTypes(extractSubranges(phc.getIcmpTypes()))
            .setFragmentOffsets(extractSubranges(phc.getFragmentOffsets()))
            .setPacketLengths(extractSubranges(phc.getPacketLengths()))
            .setTcpFlags(firstNonNull(phc.getTcpFlags(), ImmutableSet.of()));

    if (phc.getDscps() != null) {
      builder.setDscps(phc.getDscps().enumerate());
    }
    if (phc.getEcns() != null) {
      builder.setEcns(ImmutableSortedSet.copyOf(phc.getEcns().enumerate()));
    }
    return builder;
  }

  /**
   * Convert packet header constraints to a {@link Flow.Builder}
   *
   * <p><b>Does not resolve/set source and destination IPs or start location</b>. That is left up to
   * the caller.
   */
  @VisibleForTesting
  public static Flow.Builder toFlow(PacketHeaderConstraints phc) throws IllegalArgumentException {
    Flow.Builder builder = Flow.builder();

    setIpProtocol(phc, builder);
    setSrcPort(phc, builder);
    setDstPort(phc, builder);
    setIcmpValues(phc, builder);
    setDscpValue(phc, builder);
    setPacketLength(phc, builder);
    setEcnValue(phc, builder);
    setFragmentOffsets(phc, builder);
    setTcpFlags(phc, builder);
    return builder;
  }

  @VisibleForTesting
  static void setDscpValue(PacketHeaderConstraints constraints, Flow.Builder builder) {
    IntegerSpace dscps = constraints.getDscps();
    if (dscps != null) {
      checkArgument(dscps.isSingleton(), "Cannot construct flow with multiple DSCP values");
      builder.setDscp(dscps.singletonValue());
    } else {
      builder.setDscp(0);
    }
  }

  @VisibleForTesting
  static void setIcmpValues(PacketHeaderConstraints constraints, Flow.Builder builder) {
    IntegerSpace icmpTypes = constraints.getIcmpTypes();
    if (icmpTypes != null) {
      checkArgument(icmpTypes.isSingleton(), "Cannot construct flow with multiple ICMP types");
      builder.setIcmpType(icmpTypes.singletonValue());
    } else if (builder.getIpProtocol() == IpProtocol.ICMP) {
      builder.setIcmpType(8); // Default to Echo request for unconstrained, used for ICMP ping
    }
    IntegerSpace icmpCodes = constraints.getIcmpCodes();
    if (icmpCodes != null) {
      checkArgument(icmpCodes.isSingleton(), "Cannot construct flow with multiple ICMP codes");
      builder.setIcmpCode(icmpCodes.singletonValue());
    } else if (builder.getIpProtocol() == IpProtocol.ICMP) {
      builder.setIcmpCode(0); // Default to Echo request for unconstrained, used for ICMP ping
    }
  }

  @VisibleForTesting
  static void setSrcPort(PacketHeaderConstraints constraints, Flow.Builder builder) {
    IntegerSpace srcPorts = constraints.getSrcPorts();
    checkArgument(
        srcPorts == null || srcPorts.isSingleton(),
        "Cannot construct flow with multiple source ports");
    if (srcPorts != null) {
      builder.setSrcPort(srcPorts.singletonValue());
    }
  }

  private static void setIpProtocol(PacketHeaderConstraints constraints, Flow.Builder builder) {
    // IP protocol if constrained, else unset and constrained later.
    Set<IpProtocol> ipProtocols = constraints.resolveIpProtocols();
    checkArgument(
        ipProtocols == null || ipProtocols.size() == 1,
        "Cannot construct flow with multiple IP protocols");
    if (ipProtocols != null) {
      builder.setIpProtocol(ipProtocols.iterator().next());
    }
  }

  @VisibleForTesting
  static void setDstPort(PacketHeaderConstraints constraints, Builder builder) {
    IntegerSpace dstPorts = constraints.resolveDstPorts();
    final String errorMessage = "Cannot construct flow with multiple destination ports";
    checkArgument(dstPorts == null || dstPorts.isSingleton(), errorMessage);
    if (dstPorts != null) {
      builder.setDstPort(dstPorts.singletonValue());
    }
  }

  @VisibleForTesting
  static void setPacketLength(PacketHeaderConstraints constraints, Builder builder) {
    IntegerSpace packetLengths = constraints.getPacketLengths();
    final String errorMessage = "Cannot construct flow with multiple packet lengths";
    checkArgument(packetLengths == null || packetLengths.isSingleton(), errorMessage);
    if (packetLengths != null) {
      builder.setPacketLength(packetLengths.singletonValue());
    } else {
      builder.setPacketLength(DEFAULT_PACKET_LENGTH);
    }
  }

  @VisibleForTesting
  static void setEcnValue(PacketHeaderConstraints phc, Builder builder) {
    IntegerSpace ecns = phc.getEcns();
    final String errorMessage = "Cannot construct flow with multiple ECN values";
    checkArgument(ecns == null || ecns.isSingleton(), errorMessage);
    if (ecns != null) {
      builder.setEcn(ecns.singletonValue());
    }
  }

  @VisibleForTesting
  static void setFragmentOffsets(PacketHeaderConstraints phc, Builder builder) {
    IntegerSpace fragmentOffsets = phc.getFragmentOffsets();
    final String errorMessage = "Cannot construct flow with multiple packet lengths";
    checkArgument(fragmentOffsets == null || fragmentOffsets.isSingleton(), errorMessage);
    if (fragmentOffsets != null) {
      builder.setFragmentOffset(fragmentOffsets.singletonValue());
    }
  }

  @VisibleForTesting
  static void setTcpFlags(PacketHeaderConstraints phc, Builder builder) {
    Set<TcpFlagsMatchConditions> tcpFlags = phc.getTcpFlags();
    checkArgument(
        tcpFlags == null || tcpFlags.size() == 1,
        "Cannot construct flow with multiple versions of TCP flags specified");
    if (tcpFlags != null) {
      builder.setTcpFlags(tcpFlags.iterator().next().getTcpFlags());
    }
  }
}
