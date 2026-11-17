package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.IngressInterfaceVrf;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.batfish.datamodel.packet_policy.Return;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests of {@link TermFwThenToPacketPolicyStatement} */
class TermFwThenToPacketPolicyStatementTest {
  private FwTerm _fwTerm;

  @BeforeEach
  void setUp() {
    _fwTerm = new FwTerm("termName");
  }

  @Test
  void testVisitAccept() {
    _fwTerm.getThens().add(FwThenAccept.INSTANCE);
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(IngressInterfaceVrf.instance())))));
  }

  @Test
  void testVisitAcceptAfterNextTerm() {
    _fwTerm.getThens().addAll(ImmutableList.of(FwThenNextTerm.INSTANCE, FwThenAccept.INSTANCE));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), equalTo(ImmutableList.of()));
  }

  @Test
  void testVisitNextTermAfterAccept() {
    _fwTerm.getThens().addAll(ImmutableList.of(FwThenAccept.INSTANCE, FwThenNextTerm.INSTANCE));
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(IngressInterfaceVrf.instance())))));
  }

  @Test
  void testVisitNop() {
    _fwTerm.getThens().add(FwThenNop.INSTANCE);
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), equalTo(ImmutableList.of()));
  }

  @Test
  void testVisitDiscard() {
    _fwTerm.getThens().add(FwThenDiscard.INSTANCE);
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(Drop.instance()))));
  }

  @Test
  void testVisitDiscardAfterSkip() {
    _fwTerm.getThens().addAll(ImmutableList.of(FwThenNextTerm.INSTANCE, FwThenDiscard.INSTANCE));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }

  @Test
  void testVisitDecapsulate() {
    _fwTerm.getThens().add(new FwThenDecapsulate(FwThenDecapsulate.Type.GRE, null));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }

  @Test
  void testVisitNextIp() {
    _fwTerm.getThens().add(new FwThenNextIp(Prefix.parse("1.1.1.0/24")));
    // Not implemented yet:
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }

  @Test
  void testVisitRoutingInstance() {
    _fwTerm.getThens().add(new FwThenRoutingInstance("otherVRF"));
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("otherVRF"))))));
  }

  /** Tests that Junos master RI is correctly mapped to Batfish default VRF. */
  @Test
  void testVisitMasterRoutingInstance() {
    _fwTerm.getThens().add(new FwThenRoutingInstance("master"));
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("default"))))));
  }

  @Test
  void testVisitRoutingInstanceAfterSkip() {
    _fwTerm
        .getThens()
        .addAll(ImmutableList.of(FwThenNextTerm.INSTANCE, new FwThenRoutingInstance("otherVRF")));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }
}
