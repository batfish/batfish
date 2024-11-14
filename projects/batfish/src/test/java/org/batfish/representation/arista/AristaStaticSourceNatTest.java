package org.batfish.representation.arista;

import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocols;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.representation.arista.Conversions.nameOfSourceNatIpSpaceFromAcl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

public class AristaStaticSourceNatTest {
  private final AristaStaticSourceNat _nat =
      new AristaStaticSourceNat(
          Ip.parse("1.1.1.1"), 11, Ip.parse("2.2.2.2"), 22, "someAcl", NatProtocol.ANY);
  private final Transformation _orElse = when(TRUE).apply(assignDestinationPort(74, 1000)).build();

  /** All fields, in direction. */
  @Test
  public void testConversionIn() {
    Transformation trans = _nat.toIncomingTransformation(_orElse);
    // Test guard
    AclLineMatchExpr guard = trans.getGuard();
    assertThat(guard, instanceOf(AndMatchExpr.class));
    assertThat(
        ((AndMatchExpr) guard).getConjuncts(),
        containsInAnyOrder(
            matchDst(Ip.parse("2.2.2.2")),
            matchDstPort(22),
            matchSrc(new IpSpaceReference(nameOfSourceNatIpSpaceFromAcl("someAcl"))),
            matchIpProtocols(IpProtocol.TCP, IpProtocol.UDP)));
    // Test apply
    assertThat(
        trans.getTransformationSteps(),
        containsInAnyOrder(assignDestinationIp(Ip.parse("1.1.1.1")), assignDestinationPort(11)));
    // Test andThen
    assertThat(trans.getAndThen(), nullValue());
    // Test orElse
    assertThat(trans.getOrElse(), sameInstance(_orElse));
  }

  /** All fields, out direction. */
  @Test
  public void testConversionOut() {
    Transformation trans = _nat.toOutgoingTransformation(_orElse);
    // Test guard
    AclLineMatchExpr guard = trans.getGuard();
    assertThat(guard, instanceOf(AndMatchExpr.class));
    assertThat(
        ((AndMatchExpr) guard).getConjuncts(),
        containsInAnyOrder(
            matchSrc(Ip.parse("1.1.1.1")),
            matchSrcPort(11),
            matchDst(new IpSpaceReference(nameOfSourceNatIpSpaceFromAcl("someAcl"))),
            matchIpProtocols(IpProtocol.TCP, IpProtocol.UDP)));
    // Test apply
    assertThat(
        trans.getTransformationSteps(),
        containsInAnyOrder(assignSourceIp(Ip.parse("2.2.2.2")), assignSourcePort(22)));
    // Test andThen
    assertThat(trans.getAndThen(), nullValue());
    // Test orElse
    assertThat(trans.getOrElse(), sameInstance(_orElse));
  }

  /** Test presence and absence of protocol with and without ports. */
  @Test
  public void testProtocolGuard() {
    AristaStaticSourceNat anyNoPorts =
        new AristaStaticSourceNat(
            Ip.parse("1.1.1.1"), null, Ip.parse("2.2.2.2"), null, null, NatProtocol.ANY);
    assertThat(
        anyNoPorts.toOutgoingTransformation(_orElse).getGuard(),
        equalTo(matchSrc(Ip.parse("1.1.1.1"))));
    assertThat(
        anyNoPorts.toIncomingTransformation(_orElse).getGuard(),
        equalTo(matchDst(Ip.parse("2.2.2.2"))));

    AristaStaticSourceNat tcpNoPorts =
        new AristaStaticSourceNat(
            Ip.parse("1.1.1.1"), null, Ip.parse("2.2.2.2"), null, null, NatProtocol.TCP);
    assertThat(
        tcpNoPorts.toOutgoingTransformation(_orElse).getGuard(),
        equalTo(and(matchSrc(Ip.parse("1.1.1.1")), matchIpProtocol(IpProtocol.TCP))));

    AristaStaticSourceNat tcpPorts =
        new AristaStaticSourceNat(
            Ip.parse("1.1.1.1"), 11, Ip.parse("2.2.2.2"), 22, null, NatProtocol.TCP);
    assertThat(
        tcpPorts.toOutgoingTransformation(_orElse).getGuard(),
        equalTo(
            and(matchSrc(Ip.parse("1.1.1.1")), matchSrcPort(11), matchIpProtocol(IpProtocol.TCP))));
  }
}
