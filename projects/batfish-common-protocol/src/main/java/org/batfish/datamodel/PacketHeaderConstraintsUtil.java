package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Set;
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

  public static AclLineMatchExpr toAclLineMatchExpr(
      PacketHeaderConstraints phc, IpSpace srcIpSpace, IpSpace dstIpSpace) {
    return new MatchHeaderSpace(
        toHeaderSpaceBuilder(phc).setSrcIps(srcIpSpace).setDstIps(dstIpSpace).build());
  }

  public static BDD toBDD(PacketHeaderConstraints phc, IpSpace srcIpSpace, IpSpace dstIpSpace) {
    return toBDD(phc, ImmutableMap.of(), srcIpSpace, dstIpSpace);
  }

  public static BDD toBDD(
      PacketHeaderConstraints phc,
      Map<String, IpSpace> namedIpSpaces,
      IpSpace srcIpSpace,
      IpSpace dstIpSpace) {
    HeaderSpace.Builder b = toHeaderSpaceBuilder(phc).setSrcIps(srcIpSpace).setDstIps(dstIpSpace);
    return new HeaderSpaceToBDD(new BDDPacket(), namedIpSpaces).toBDD(b.build());
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
            .setSrcPorts(firstNonNull(phc.getSrcPorts(), ImmutableSortedSet.of()))
            .setDstPorts(firstNonNull(phc.resolveDstPorts(), ImmutableSortedSet.of()))
            .setIcmpCodes(firstNonNull(phc.getIcmpCodes(), ImmutableSortedSet.of()))
            .setIcmpTypes(firstNonNull(phc.getIcmpTypes(), ImmutableSortedSet.of()))
            .setDstProtocols(firstNonNull(phc.getApplications(), ImmutableSortedSet.of()));

    if (phc.getDscps() != null) {
      builder.setDscps(
          ImmutableSortedSet.copyOf(
              phc.getDscps().stream().flatMapToInt(SubRange::asStream).iterator()));
    }
    if (phc.getEcns() != null) {
      builder.setEcns(
          ImmutableSortedSet.copyOf(
              phc.getEcns().stream().flatMapToInt(SubRange::asStream).iterator()));
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
    return builder;
  }

  @VisibleForTesting
  static void setDscpValue(PacketHeaderConstraints constraints, Flow.Builder builder) {
    Set<SubRange> dscps = constraints.getDscps();
    if (dscps != null) {
      SubRange dscp = dscps.iterator().next();
      if (dscps.size() > 1 || !dscp.isSingleValue()) {
        throw new IllegalArgumentException("Cannot construct flow with multiple DSCP values");
      }
      builder.setDscp(dscp.getStart());
    } else {
      builder.setDscp(0);
    }
  }

  @VisibleForTesting
  static void setIcmpValues(PacketHeaderConstraints constraints, Flow.Builder builder) {
    Set<SubRange> icmpTypes = constraints.getIcmpTypes();
    if (icmpTypes != null) {
      SubRange icmpType = icmpTypes.iterator().next();
      if (icmpTypes.size() > 1 || !icmpType.isSingleValue()) {
        throw new IllegalArgumentException("Cannot construct flow with multiple ICMP types");
      }
      builder.setIcmpType(icmpType.getStart());
    }
    Set<SubRange> icmpCodes = constraints.getIcmpCodes();
    if (icmpCodes != null) {
      SubRange icmpCode = icmpCodes.iterator().next();
      if (icmpCodes.size() > 1 || !icmpCode.isSingleValue()) {
        throw new IllegalArgumentException("Cannot construct flow with multiple ICMP codes");
      }
      builder.setIcmpType(icmpCode.getStart());
    }
  }

  @VisibleForTesting
  static void setSrcPort(PacketHeaderConstraints constraints, Flow.Builder builder) {
    Set<SubRange> srcPorts = constraints.getSrcPorts();
    checkArgument(
        srcPorts == null || srcPorts.size() == 1,
        "Cannot construct flow with multiple source ports");
    if (srcPorts != null) {
      SubRange srcPort = srcPorts.iterator().next();
      checkArgument(srcPort.isSingleValue(), "Cannot construct flow with multiple source ports");
      builder.setSrcPort(srcPort.getStart());
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
    Set<SubRange> dstPorts = constraints.resolveDstPorts();
    final String errorMessage = "Cannot construct flow with multiple destination ports";
    checkArgument(dstPorts == null || dstPorts.size() == 1, errorMessage);
    if (dstPorts != null) {
      SubRange dstPort = dstPorts.iterator().next();
      checkArgument(dstPort.isSingleValue(), errorMessage);
      builder.setDstPort(dstPort.getStart());
    }
  }

  @VisibleForTesting
  static void setPacketLength(PacketHeaderConstraints constraints, Builder builder) {
    Set<SubRange> packetLengths = constraints.getPacketLengths();
    final String errorMessage = "Cannot construct flow with multiple packet lengths";
    checkArgument(packetLengths == null || packetLengths.size() == 1, errorMessage);
    if (packetLengths != null) {
      SubRange packetLength = packetLengths.iterator().next();
      checkArgument(packetLength.isSingleValue(), errorMessage);
      builder.setPacketLength(packetLength.getStart());
    } else {
      builder.setPacketLength(DEFAULT_PACKET_LENGTH);
    }
  }

  @VisibleForTesting
  static void setEcnValue(PacketHeaderConstraints phc, Builder builder) {
    Set<SubRange> ecns = phc.getEcns();
    final String errorMessage = "Cannot construct flow with multiple ECN values";
    checkArgument(ecns == null || ecns.size() == 1, errorMessage);
    if (ecns != null) {
      SubRange ecn = ecns.iterator().next();
      checkArgument(ecn.isSingleValue(), errorMessage);
      builder.setEcn(ecn.getStart());
    }
  }

  @VisibleForTesting
  static void setFragmentOffsets(PacketHeaderConstraints phc, Builder builder) {
    Set<SubRange> fragmentOffsets = phc.getFragmentOffsets();
    final String errorMessage = "Cannot construct flow with multiple packet lengths";
    checkArgument(fragmentOffsets == null || fragmentOffsets.size() == 1, errorMessage);
    if (fragmentOffsets != null) {
      SubRange fragmentOffset = fragmentOffsets.iterator().next();
      checkArgument(fragmentOffset.isSingleValue(), errorMessage);
      builder.setFragmentOffset(fragmentOffset.getStart());
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
}
