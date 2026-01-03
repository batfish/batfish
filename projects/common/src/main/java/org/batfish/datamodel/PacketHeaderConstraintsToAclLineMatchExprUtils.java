package org.batfish.datamodel;

import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocols;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.applications.Application;

final class PacketHeaderConstraintsToAclLineMatchExprUtils {
  private PacketHeaderConstraintsToAclLineMatchExprUtils() {}

  static @Nullable AclLineMatchExpr dscpsToAclLineMatchExpr(@Nullable IntegerSpace dscps) {
    return Optional.ofNullable(dscps)
        .map(IntegerSpace::enumerate)
        .map(k -> match(HeaderSpace.builder().setDscps(k).build()))
        .orElse(null);
  }

  static @Nullable AclLineMatchExpr ecnsToAclLineMatchExpr(@Nullable IntegerSpace ecns) {
    return Optional.ofNullable(ecns)
        .map(IntegerSpace::enumerate)
        .map(k -> match(HeaderSpace.builder().setEcns(k).build()))
        .orElse(null);
  }

  static @Nullable AclLineMatchExpr packetLengthToAclLineMatchExpr(
      @Nullable IntegerSpace packetLengths) {
    return Optional.ofNullable(packetLengths)
        .map(IntegerSpace::getSubRanges)
        .map(k -> match(HeaderSpace.builder().setPacketLengths(k).build()))
        .orElse(null);
  }

  static @Nullable AclLineMatchExpr fragmentOffsetsToAclLineMatchExpr(
      @Nullable IntegerSpace fragmentOffsets) {
    return Optional.ofNullable(fragmentOffsets)
        .map(IntegerSpace::getSubRanges)
        .map(k -> match(HeaderSpace.builder().setFragmentOffsets(k).build()))
        .orElse(null);
  }

  static @Nullable AclLineMatchExpr ipProtocolsToAclLineMatchExpr(
      @Nullable Set<IpProtocol> ipProtocols) {
    return ipProtocols == null ? null : matchIpProtocols(ipProtocols, null);
  }

  static @Nullable AclLineMatchExpr icmpCodeToAclLineMatchExpr(@Nullable IntegerSpace icmpCode) {
    return Optional.ofNullable(icmpCode)
        .map(IntegerSpace::getSubRanges)
        .map(k -> match(HeaderSpace.builder().setIcmpCodes(k).build()))
        .orElse(null);
  }

  static @Nullable AclLineMatchExpr icmpTypeToAclLineMatchExpr(@Nullable IntegerSpace icmpType) {
    return Optional.ofNullable(icmpType)
        .map(IntegerSpace::getSubRanges)
        .map(k -> match(HeaderSpace.builder().setIcmpTypes(k).build()))
        .orElse(null);
  }

  static @Nullable AclLineMatchExpr srcPortsToAclLineMatchExpr(@Nullable IntegerSpace srcPorts) {
    return Optional.ofNullable(srcPorts).map(AclLineMatchExprs::matchSrcPort).orElse(null);
  }

  static @Nullable AclLineMatchExpr dstPortsToAclLineMatchExpr(@Nullable IntegerSpace dstPorts) {
    return Optional.ofNullable(dstPorts).map(AclLineMatchExprs::matchDstPort).orElse(null);
  }

  static @Nullable AclLineMatchExpr applicationsToAclLineMatchExpr(
      @Nullable Set<Application> applications) {
    return applications == null
        ? null
        : or(
            applications.stream()
                .map(Application::toAclLineMatchExpr)
                .collect(ImmutableList.toImmutableList()));
  }

  static @Nullable AclLineMatchExpr tcpFlagsToAclLineMatchExpr(
      @Nullable Set<TcpFlagsMatchConditions> tcpFlags) {
    return Optional.ofNullable(tcpFlags)
        .map(k -> match(HeaderSpace.builder().setTcpFlags(k).build()))
        .orElse(null);
  }
}
