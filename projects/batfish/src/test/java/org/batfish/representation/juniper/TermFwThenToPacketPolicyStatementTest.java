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
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link TermFwThenToPacketPolicyStatement} */
public class TermFwThenToPacketPolicyStatementTest {
  private FwTerm _fwTerm;

  @Before
  public void setUp() {
    _fwTerm = new FwTerm("termName");
  }

  @Test
  public void testVisitAccept() {
    _fwTerm.getThens().add(FwThenAccept.INSTANCE);
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(IngressInterfaceVrf.instance())))));
  }

  @Test
  public void testVisitAcceptAfterNextTerm() {
    _fwTerm.getThens().addAll(ImmutableList.of(FwThenNextTerm.INSTANCE, FwThenAccept.INSTANCE));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), equalTo(ImmutableList.of()));
  }

  @Test
  public void testVisitNextTermAfterAccept() {
    _fwTerm.getThens().addAll(ImmutableList.of(FwThenAccept.INSTANCE, FwThenNextTerm.INSTANCE));
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(IngressInterfaceVrf.instance())))));
  }

  @Test
  public void testVisitNop() {
    _fwTerm.getThens().add(FwThenNop.INSTANCE);
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), equalTo(ImmutableList.of()));
  }

  @Test
  public void testVisitDiscard() {
    _fwTerm.getThens().add(FwThenDiscard.INSTANCE);
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(Drop.instance()))));
  }

  @Test
  public void testVisitDiscardAfterSkip() {
    _fwTerm.getThens().addAll(ImmutableList.of(FwThenNextTerm.INSTANCE, FwThenDiscard.INSTANCE));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }

  @Test
  public void testVisitNextIp() {
    _fwTerm.getThens().add(new FwThenNextIp(Prefix.parse("1.1.1.0/24")));
    // Not implemented yet:
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }

  @Test
  public void testVisitRoutingInstance() {
    _fwTerm.getThens().add(new FwThenRoutingInstance("otherVRF"));
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("otherVRF"))))));
  }

  /** Tests that Junos master RI is correctly mapped to Batfish default VRF. */
  @Test
  public void testVisitMasterRoutingInstance() {
    _fwTerm.getThens().add(new FwThenRoutingInstance("master"));
    assertThat(
        TermFwThenToPacketPolicyStatement.convert(_fwTerm),
        equalTo(ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("default"))))));
  }

  @Test
  public void testVisitRoutingInstanceAfterSkip() {
    _fwTerm
        .getThens()
        .addAll(ImmutableList.of(FwThenNextTerm.INSTANCE, new FwThenRoutingInstance("otherVRF")));
    assertThat(TermFwThenToPacketPolicyStatement.convert(_fwTerm), empty());
  }
}
