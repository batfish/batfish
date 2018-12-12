package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

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
      PacketHeaderConstraints phc, IpSpace srcIpSpace, IpSpace dstIpSpace) {
    return new MatchHeaderSpace(
        toHeaderSpaceBuilder(phc).setSrcIps(srcIpSpace).setDstIps(dstIpSpace).build());
  }

  /**
   * Convert given {@link PacketHeaderConstraints} to a BDD
   *
   * @param phc the packet header constraints
   * @param srcIpSpace Resolved source IP space
   * @param dstIpSpace Resolved destination IP space
   */
  public static BDD toBDD(PacketHeaderConstraints phc, IpSpace srcIpSpace, IpSpace dstIpSpace) {
    return toBDD(phc, ImmutableMap.of(), srcIpSpace, dstIpSpace);
  }

  /**
   * Convert given {@link PacketHeaderConstraints} to a BDD, also taking into account named IP
   * spaces
   *
   * @param phc the packet header constraints
   * @param namedIpSpaces map of named IP spaces
   * @param srcIpSpace Resolved source IP space
   * @param dstIpSpace Resolved destination IP space
   */
  public static BDD toBDD(
      PacketHeaderConstraints phc,
      Map<String, IpSpace> namedIpSpaces,
      IpSpace srcIpSpace,
      IpSpace dstIpSpace) {
    HeaderSpace.Builder b = toHeaderSpaceBuilder(phc).setSrcIps(srcIpSpace).setDstIps(dstIpSpace);
    return new HeaderSpaceToBDD(new BDDPacket(), namedIpSpaces).toBDD(b.build());
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
            .setDstProtocols(firstNonNull(phc.getApplications(), ImmutableSortedSet.of()))
            .setFragmentOffsets(extractSubranges(phc.getFragmentOffsets()))
            .setPacketLengths(extractSubranges(phc.getPacketLengths()))
            .setTcpFlags(firstNonNull(phc.getTcpFlags(), ImmutableSet.of()))
            .setStates(firstNonNull(phc.getFlowStates(), ImmutableSortedSet.of()));

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
    setFlowStates(phc, builder);
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
    }
    IntegerSpace icmpCodes = constraints.getIcmpCodes();
    if (icmpCodes != null) {
      checkArgument(icmpCodes.isSingleton(), "Cannot construct flow with multiple ICMP codes");
      builder.setIcmpType(icmpCodes.singletonValue());
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

  @VisibleForTesting
  static void setIpProtocol(PacketHeaderConstraints constraints, Flow.Builder builder) {
    // IP protocol (default to UDP)
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
  static void setFlowStates(PacketHeaderConstraints phc, Builder builder) {
    Set<FlowState> flowStates = phc.getFlowStates();
    checkArgument(
        flowStates == null || flowStates.size() == 1,
        "Cannot construct flow  with multiple packet lengths");
    if (flowStates != null) {
      builder.setState(flowStates.iterator().next());
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
