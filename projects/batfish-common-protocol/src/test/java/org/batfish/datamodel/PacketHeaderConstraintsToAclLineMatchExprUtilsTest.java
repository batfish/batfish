package org.batfish.datamodel;

import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class PacketHeaderConstraintsToAclLineMatchExprUtilsTest {

  @Test
  public void testDscpsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.dscpsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setDscps(ImmutableList.of(10, 11)).build()));
  }

  @Test
  public void testEcnsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.ecnsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setEcns(ImmutableList.of(10, 11)).build()));
  }

  @Test
  public void testPacketLengthToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.packetLengthToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(
            HeaderSpace.builder()
                .setPacketLengths(ImmutableList.of(new SubRange(10, 11)))
                .build()));
  }

  @Test
  public void testFragmentOffsetsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.fragmentOffsetsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(
            HeaderSpace.builder()
                .setFragmentOffsets(ImmutableList.of(new SubRange(10, 11)))
                .build()));
  }

  @Test
  public void testIpProtocolsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.ipProtocolsToAclLineMatchExpr(
            ImmutableSet.of(IpProtocol.TCP)),
        match(HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.TCP)).build()));
  }

  @Test
  public void testIcmpCodeToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.icmpCodeToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setIcmpCodes(new SubRange(10, 11)).build()));
  }

  @Test
  public void testIcmpTypeToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.icmpTypeToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        match(HeaderSpace.builder().setIcmpTypes(new SubRange(10, 11)).build()));
  }

  @Test
  public void testSrcPortsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.srcPortsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        matchSrcPort(IntegerSpace.of(new SubRange(10, 11))));
  }

  @Test
  public void testDstPortsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.dstPortsToAclLineMatchExpr(
            IntegerSpace.of(new SubRange(10, 11))),
        matchDstPort(IntegerSpace.of(new SubRange(10, 11))));
  }

  @Test
  public void testApplicationsToAclLineMatchExpr() {
    assertEquals(
        PacketHeaderConstraintsToAclLineMatchExprUtils.applicationsToAclLineMatchExpr(
            ImmutableSet.of(Protocol.SSH.toApplication(), Protocol.DNS.toApplication())),
        or(
            match(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.TCP)
                    .setDstPorts(SubRange.singleton(22))
                    .build()),
            match(
                HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.UDP)
                    .setDstPorts(SubRange.singleton(53))
                    .build())));
  }

  @Test
  public void testTcpFlagsToAclLineMatchExpr() {
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
