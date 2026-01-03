package org.batfish.datamodel;

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
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/** Utility class to convert {@link PacketHeaderConstraints} to other internal representations. */
@ParametersAreNonnullByDefault
public class PacketHeaderConstraintsUtil {

  /**
   * Convert {@link PacketHeaderConstraints} to an {@link AclLineMatchExpr}.
   *
   * @param phc the packet header constraints
   * @param srcIpSpace Resolved source IP space
   * @param dstIpSpace Resolved destination IP space
   */
  public static AclLineMatchExpr toAclLineMatchExpr(
      PacketHeaderConstraints phc, IpSpace srcIpSpace, IpSpace dstIpSpace) {
    List<AclLineMatchExpr> conjuncts =
        Stream.of(
                matchSrc(srcIpSpace),
                matchDst(dstIpSpace),
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
    return toAclLineMatchExpr(phc, UniverseIpSpace.INSTANCE, UniverseIpSpace.INSTANCE);
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

  /**
   * Convert packet header constraints to a {@link Flow.Builder}
   *
   * <p><b>Does not resolve/set source and destination IPs or start location</b>. That is left up to
   * the caller.
   */
  @VisibleForTesting
  public static Flow.Builder toFlow(
      BDDPacket pkt, PacketHeaderConstraints phc, IpSpace srcIps, IpSpace dstIps)
      throws IllegalArgumentException {
    return pkt.getFlow(toBDD(pkt, phc, srcIps, dstIps))
        .orElseThrow(() -> new BatfishException("could not convert header constraints to flow"));
  }
}
