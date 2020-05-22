package org.batfish.datamodel;

import static org.batfish.datamodel.PacketHeaderConstraintsUtil.toAclLineMatchExpr;
import static org.batfish.datamodel.PacketHeaderConstraintsUtil.toFlow;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
            match(HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build()),
            match(
                HeaderSpace.builder().setSrcPorts(new SubRange(1, 3), new SubRange(5, 6)).build()),
            match(HeaderSpace.builder().setDstPorts(new SubRange(11, 12)).build())),
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
    assertEquals(
        toAclLineMatchExpr(phc, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE),
        and(
            // src ip
            match(HeaderSpace.builder().setSrcIps(EmptyIpSpace.INSTANCE).build()),
            // dst ip
            match(HeaderSpace.builder().setDstIps(EmptyIpSpace.INSTANCE).build()),
            // ip protocols
            match(HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build()),
            // application
            or(
                // ssh
                match(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP)
                        .setDstPorts(SubRange.singleton(22))
                        .build()),
                // dns
                match(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.UDP)
                        .setDstPorts(SubRange.singleton(53))
                        .build()))));
  }
}
