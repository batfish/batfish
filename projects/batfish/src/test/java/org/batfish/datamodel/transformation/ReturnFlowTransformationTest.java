package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.transformation.Noop.NOOP_DEST_NAT;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.ReturnFlowTransformation.GUARD_VISITOR;
import static org.batfish.datamodel.transformation.ReturnFlowTransformation.STEP_VISITOR;
import static org.batfish.datamodel.transformation.ReturnFlowTransformation.returnFlowTransformation;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ReturnFlowTransformation}. */
public final class ReturnFlowTransformationTest {
  @Rule public ExpectedException _expectedException = ExpectedException.none();

  @Test
  public void testStepVisitor() {
    // noop
    assertEquals(NOOP_SOURCE_NAT.accept(STEP_VISITOR), NOOP_SOURCE_NAT);
    assertEquals(NOOP_DEST_NAT.accept(STEP_VISITOR), NOOP_DEST_NAT);

    // assign IP address from pool
    {
      Ip poolStart = Ip.parse("1.1.1.1");
      Ip poolEnd = Ip.parse("1.1.2.2");
      AssignIpAddressFromPool assignSrc =
          new AssignIpAddressFromPool(
              TransformationType.SOURCE_NAT, IpField.SOURCE, poolStart, poolEnd);
      AssignIpAddressFromPool assignDst =
          new AssignIpAddressFromPool(
              TransformationType.SOURCE_NAT, IpField.DESTINATION, poolStart, poolEnd);
      assertEquals(assignSrc.accept(STEP_VISITOR), assignDst);
      assertEquals(assignDst.accept(STEP_VISITOR), assignSrc);
    }

    // shift
    {
      Prefix prefix = Prefix.parse("1.1.0.0/16");
      ShiftIpAddressIntoSubnet shiftSrc =
          new ShiftIpAddressIntoSubnet(TransformationType.DEST_NAT, IpField.SOURCE, prefix);
      ShiftIpAddressIntoSubnet shiftDst =
          new ShiftIpAddressIntoSubnet(TransformationType.DEST_NAT, IpField.DESTINATION, prefix);
      assertEquals(shiftSrc.accept(STEP_VISITOR), shiftDst);
      assertEquals(shiftDst.accept(STEP_VISITOR), shiftSrc);
    }

    // assign port from pool
    {
      int poolStart = 5;
      int poolEnd = 17;
      AssignPortFromPool assignSrc =
          new AssignPortFromPool(
              TransformationType.SOURCE_NAT, PortField.SOURCE, poolStart, poolEnd);
      AssignPortFromPool assignDst =
          new AssignPortFromPool(
              TransformationType.SOURCE_NAT, PortField.DESTINATION, poolStart, poolEnd);
      assertEquals(assignSrc.accept(STEP_VISITOR), assignDst);
      assertEquals(assignDst.accept(STEP_VISITOR), assignSrc);
    }

    // apply all
    {
      Ip ipPoolStart = Ip.parse("1.1.1.1");
      Ip ipPoolEnd = Ip.parse("1.1.2.2");
      int portPoolStart = 5;
      int portPoolEnd = 17;
      AssignIpAddressFromPool assignSrcIp =
          new AssignIpAddressFromPool(
              TransformationType.SOURCE_NAT, IpField.SOURCE, ipPoolStart, ipPoolEnd);
      AssignIpAddressFromPool assignDstIp =
          new AssignIpAddressFromPool(
              TransformationType.SOURCE_NAT, IpField.DESTINATION, ipPoolStart, ipPoolEnd);
      AssignPortFromPool assignSrcPort =
          new AssignPortFromPool(
              TransformationType.SOURCE_NAT, PortField.SOURCE, portPoolStart, portPoolEnd);
      AssignPortFromPool assignDstPort =
          new AssignPortFromPool(
              TransformationType.SOURCE_NAT, PortField.DESTINATION, portPoolStart, portPoolEnd);
      ApplyAll applyAllSrc = new ApplyAll(assignSrcIp, assignSrcPort);
      ApplyAll applyAllDst = new ApplyAll(assignDstIp, assignDstPort);
      assertEquals(applyAllSrc.accept(STEP_VISITOR), applyAllDst);
      assertEquals(applyAllDst.accept(STEP_VISITOR), applyAllSrc);
    }

    // apply any
    {
      Ip ipPoolStart = Ip.parse("1.1.1.1");
      Ip ipPoolEnd = Ip.parse("1.1.2.2");
      int portPoolStart = 5;
      int portPoolEnd = 17;
      AssignIpAddressFromPool assignSrcIp =
          new AssignIpAddressFromPool(
              TransformationType.SOURCE_NAT, IpField.SOURCE, ipPoolStart, ipPoolEnd);
      AssignIpAddressFromPool assignDstIp =
          new AssignIpAddressFromPool(
              TransformationType.SOURCE_NAT, IpField.DESTINATION, ipPoolStart, ipPoolEnd);
      AssignPortFromPool assignSrcPort =
          new AssignPortFromPool(
              TransformationType.SOURCE_NAT, PortField.SOURCE, portPoolStart, portPoolEnd);
      AssignPortFromPool assignDstPort =
          new AssignPortFromPool(
              TransformationType.SOURCE_NAT, PortField.DESTINATION, portPoolStart, portPoolEnd);
      ApplyAny applyAnySrc = new ApplyAny(assignSrcIp, assignSrcPort);
      ApplyAny applyAnyDst = new ApplyAny(assignDstIp, assignDstPort);
      assertEquals(applyAnySrc.accept(STEP_VISITOR), applyAnyDst);
      assertEquals(applyAnyDst.accept(STEP_VISITOR), applyAnySrc);
    }
  }

  @Test
  public void testGuardVisitor() {
    AclLineMatchExpr dstIp = matchDstIp("1.1.1.1");
    AclLineMatchExpr srcIp = matchSrcIp("1.1.1.1");

    ImmutableList<SubRange> ports = ImmutableList.of(new SubRange(10, 20));
    MatchHeaderSpace dstPort = match(HeaderSpace.builder().setDstPorts(ports).build());
    MatchHeaderSpace srcPort = match(HeaderSpace.builder().setSrcPorts(ports).build());

    MatchSrcInterface srcIface = new MatchSrcInterface(ImmutableSet.of("a", "b"));

    // identity on constants, sources
    assertEquals(TRUE.accept(GUARD_VISITOR), TRUE);
    assertEquals(FALSE.accept(GUARD_VISITOR), FALSE);

    // source constraints are removed
    {
      assertNull(ORIGINATING_FROM_DEVICE.accept(GUARD_VISITOR));
      assertNull(srcIface.accept(GUARD_VISITOR));
    }

    // headerspace fields are flipped
    {
      IpIpSpace ipSpace = Ip.parse("1.1.1.1").toIpSpace();
      MatchHeaderSpace notDstIp = match(HeaderSpace.builder().setNotDstIps(ipSpace).build());
      MatchHeaderSpace notSrcIp = match(HeaderSpace.builder().setNotSrcIps(ipSpace).build());
      MatchHeaderSpace notDstPort = match(HeaderSpace.builder().setNotDstPorts(ports).build());
      MatchHeaderSpace notSrcPort = match(HeaderSpace.builder().setNotSrcPorts(ports).build());
      assertEquals(dstIp.accept(GUARD_VISITOR), srcIp);
      assertEquals(srcIp.accept(GUARD_VISITOR), dstIp);
      assertEquals(notDstIp.accept(GUARD_VISITOR), notSrcIp);
      assertEquals(notSrcIp.accept(GUARD_VISITOR), notDstIp);
      assertEquals(dstPort.accept(GUARD_VISITOR), srcPort);
      assertEquals(srcPort.accept(GUARD_VISITOR), dstPort);
      assertEquals(notDstPort.accept(GUARD_VISITOR), notSrcPort);
      assertEquals(notSrcPort.accept(GUARD_VISITOR), notDstPort);

      // non src/dst fields are unchanged
      MatchHeaderSpace tcp =
          match(HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.TCP)).build());
      assertEquals(tcp.accept(GUARD_VISITOR), tcp);
    }

    // and
    {
      assertEquals(and(dstIp, dstPort).accept(GUARD_VISITOR), and(srcIp, srcPort));
      assertEquals(and(dstIp, ORIGINATING_FROM_DEVICE).accept(GUARD_VISITOR), srcIp);
      assertNull(and(ORIGINATING_FROM_DEVICE, srcIface).accept(GUARD_VISITOR));
    }

    // or
    {
      assertEquals(or(dstIp, dstPort).accept(GUARD_VISITOR), or(srcIp, srcPort));
      assertEquals(or(dstIp, ORIGINATING_FROM_DEVICE).accept(GUARD_VISITOR), srcIp);
      assertNull(or(ORIGINATING_FROM_DEVICE, srcIface).accept(GUARD_VISITOR));
    }

    // not
    {
      assertEquals(not(dstIp).accept(GUARD_VISITOR), not(srcIp));
      assertNull(not(ORIGINATING_FROM_DEVICE).accept(GUARD_VISITOR));
      assertNull(not(srcIface).accept(GUARD_VISITOR));
    }

    // permitted by acl is not allowed
    _expectedException.expect(IllegalArgumentException.class);
    new PermittedByAcl("foo").accept(GUARD_VISITOR);
  }

  @Test
  public void testReturnFlowTransformation() {
    AclLineMatchExpr matchDst = matchDstIp("1.1.1.1");
    AclLineMatchExpr matchSrc = matchSrcIp("1.1.1.1");
    ShiftIpAddressIntoSubnet shiftDst =
        new ShiftIpAddressIntoSubnet(
            TransformationType.DEST_NAT, IpField.DESTINATION, Prefix.parse("2.2.0.0/16"));
    ShiftIpAddressIntoSubnet shiftSrc =
        new ShiftIpAddressIntoSubnet(
            TransformationType.DEST_NAT, IpField.SOURCE, Prefix.parse("2.2.0.0/16"));
    AssignIpAddressFromPool poolSrc =
        new AssignIpAddressFromPool(
            TransformationType.SOURCE_NAT,
            IpField.SOURCE,
            Ip.parse("10.0.0.0"),
            Ip.parse("10.0.0.10"));
    AssignIpAddressFromPool poolDst =
        new AssignIpAddressFromPool(
            TransformationType.SOURCE_NAT,
            IpField.DESTINATION,
            Ip.parse("10.0.0.0"),
            Ip.parse("10.0.0.10"));

    // base case: transform the guard and all steps
    Transformation forwardTransformation = when(matchDst).apply(shiftDst, poolSrc).build();
    Transformation reverseTransformation = when(matchSrc).apply(shiftSrc, poolDst).build();
    assertEquals(returnFlowTransformation(forwardTransformation), reverseTransformation);
    // recursive case: andThen
    assertEquals(
        returnFlowTransformation(always().setAndThen(forwardTransformation).build()),
        always().setAndThen(reverseTransformation).build());
    // recursive case: orElse
    assertEquals(
        returnFlowTransformation(always().setOrElse(forwardTransformation).build()),
        always().setOrElse(reverseTransformation).build());

    // handle null in the transformed guard
    assertEquals(
        returnFlowTransformation(when(ORIGINATING_FROM_DEVICE).apply(shiftDst).build()),
        always().apply(shiftSrc).build());
  }
}
