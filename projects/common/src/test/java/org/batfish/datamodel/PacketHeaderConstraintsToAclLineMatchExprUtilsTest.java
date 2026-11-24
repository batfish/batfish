package org.batfish.datamodel;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

class PacketHeaderConstraintsToAclLineMatchExprUtilsTest {

  @Test
  void testDscpsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.dscpsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setDscps(ImmutableList.of(10, 11)).build()));
  }

  @Test
  void testEcnsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.ecnsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setEcns(ImmutableList.of(10, 11)).build()));
  }

  @Test
  void testPacketLengthToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.packetLengthToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(
            HeaderSpace.builder()
                .setPacketLengths(ImmutableList.of(new SubRange(10, 11)))
                .build()));
  }

  @Test
  void testFragmentOffsetsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.fragmentOffsetsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(
            HeaderSpace.builder()
                .setFragmentOffsets(ImmutableList.of(new SubRange(10, 11)))
                .build()));
  }

  @Test
  void testIpProtocolsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.ipProtocolsToAclLineMatchExpr(
            ImmutableSet.of(IpProtocol.TCP)),
        matchIpProtocol(IpProtocol.TCP));
  }

  @Test
  void testIcmpCodeToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.icmpCodeToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setIcmpCodes(new SubRange(10, 11)).build()));
  }

  @Test
  void testIcmpTypeToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.icmpTypeToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setIcmpTypes(new SubRange(10, 11)).build()));
  }

  @Test
  void testSrcPortsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.srcPortsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        matchSrcPort(IntegerSpace.of(new SubRange(10, 11))));
  }

  @Test
  void testDstPortsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.dstPortsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        matchDstPort(IntegerSpace.of(new SubRange(10, 11))));
  }

  @Test
  void testApplicationsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.applicationsToAclLineMatchExpr(
            ImmutableSet.of(Protocol.SSH.toApplication(), Protocol.DNS.toApplication())),
        or(
            and(matchIpProtocol(IpProtocol.TCP), matchDstPort(22)),
            and(matchIpProtocol(IpProtocol.UDP), matchDstPort(53))));
  }

  @Test
  void testTcpFlagsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.tcpFlagsToAclLineMatchExpr(
            ImmutableSet.of(
                TcpFlagsMatchConditions.builder()
                    .setTcpFlags(TcpFlags.builder().setSyn(true).build())
                    .build())),
        match(
            HeaderSpace.builder()
                .setTcpFlags(
                    ImmutableSet.of(
                        TcpFlagsMatchConditions.builder()
                            .setTcpFlags(TcpFlags.builder().setSyn(true).build())
                            .build()))
                .build()));
  }
}
