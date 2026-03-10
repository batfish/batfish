package org.batfish.datamodel;

import static org.batfish.datamodel.PacketHeaderConstraintsUtil.toAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsUtil.toFlow;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link PacketHeaderConstraintsUtil} */
public class PacketHeaderConstraintsUtilTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testToAclLineMatchExprConversion() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setSrcPorts(
                IntegerSpace.builder()
                    .including(new SubRange(1, 3))
                    .including(new SubRange(5, 6))
                    .build())
            .setDstPorts(IntegerSpace.of(new SubRange(11, 12)))
            .setEcns(IntegerSpace.of(new SubRange(1, 3)))
            .setIpProtocols(Collections.singleton(IpProtocol.TCP))
            .build();

    AclLineMatchExpr hs = PacketHeaderConstraintsUtil.toAclLineMatchExpr(phc);
    assertEquals(
        and(
            matchSrc(UniverseIpSpace.INSTANCE),
            matchDst(UniverseIpSpace.INSTANCE),
            match(
                HeaderSpace.builder()
                    .setEcns(IntegerSpace.of(new SubRange(1, 3)).enumerate())
                    .build()),
            matchIpProtocol(IpProtocol.TCP),
            matchSrcPort(IntegerSpace.builder().including(1, 2, 3, 5, 6).build()),
            matchDstPort(IntegerSpace.of(new SubRange(11, 12)))),
        hs);
  }

  @Test
  public void testToBDDConversion() {
    BDDPacket packet = new BDDPacket();
    assertThat(
        PacketHeaderConstraintsUtil.toBDD(
            packet,
            PacketHeaderConstraints.unconstrained(),
            UniverseIpSpace.INSTANCE,
            UniverseIpSpace.INSTANCE),
        equalTo(packet.getFactory().one()));
  }

  /** Known bug with current conversion from PacketHeaderConstraints to HeaderSpace. */
  @Test
  public void testApplications() {
    BDDPacket packet = new BDDPacket();
    BDD ssh = packet.getIpProtocol().value(IpProtocol.TCP).and(packet.getDstPort().value(22));
    BDD dns = packet.getIpProtocol().value(IpProtocol.UDP).and(packet.getDstPort().value(53));
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setApplications("ssh, dns").build();
    assertThat(
        PacketHeaderConstraintsUtil.toBDD(
            packet, phc, UniverseIpSpace.INSTANCE, UniverseIpSpace.INSTANCE),
        equalTo(ssh.or(dns)));
  }

  /**
   * Test that unconstrained ICMP type/code defaults to ICMP ping (echo request) if the ip protocol
   * is ICMP.
   */
  @Test
  public void testDefaultToIcmpPing() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setIpProtocols(ImmutableSet.of(IpProtocol.ICMP)).build();
    Builder builder =
        toFlow(new BDDPacket(), phc, UniverseIpSpace.INSTANCE, UniverseIpSpace.INSTANCE);
    assertThat(builder.getIcmpType(), equalTo(8));
    assertThat(builder.getIcmpCode(), equalTo(0));
  }

  @Test
  public void testToAclLineMatchExpr() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
            .setApplications("ssh, dns")
            .build();
    IpSpace srcIp = Prefix.parse("1.0.0.0/8").toIpSpace();
    IpSpace dstIp = Prefix.parse("2.0.0.0/8").toIpSpace();
    assertEquals(
        toAclLineMatchExpr(phc, srcIp, dstIp),
        and(
            // src ip
            matchSrc(srcIp),
            // dst ip
            matchDst(dstIp),
            // ip protocols
            matchIpProtocol(IpProtocol.TCP),
            // application
            or(
                // ssh
                and(matchIpProtocol(IpProtocol.TCP), matchDstPort(22)),
                // dns
                and(matchIpProtocol(IpProtocol.UDP), matchDstPort(53)))));
  }
}
