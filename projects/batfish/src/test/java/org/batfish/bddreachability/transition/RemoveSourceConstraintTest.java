package org.batfish.bddreachability.transition;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.junit.jupiter.api.Test;

class RemoveSourceConstraintTest {

  @Test
  void testTransit() {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(pkt, ImmutableSet.of("A", "B"));
    RemoveSourceConstraint rsc = new RemoveSourceConstraint(mgr);

    // any valid source forward is one
    assertThat(rsc.transitForward(mgr.getSourceInterfaceBDD("A")), isOne());
    assertThat(rsc.transitForward(mgr.getSourceInterfaceBDD("B")), isOne());
    assertThat(
        rsc.transitForward(mgr.getSourceInterfaceBDD(SOURCE_ORIGINATING_FROM_DEVICE)), isOne());

    // zero forward or backward stays zero
    assertThat(rsc.transitForward(pkt.getFactory().zero()), isZero());
    assertThat(rsc.transitBackward(pkt.getFactory().zero()), isZero());
  }

  @Test
  void testTransitMissingInvariants() {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(pkt, ImmutableSet.of("A", "B"));
    RemoveSourceConstraint rsc = new RemoveSourceConstraint(mgr);

    // an invalid source forward is zero
    assertThat(
        "invalid test, there are no 'extra' sources", mgr.isValidValue().not(), not(isZero()));
    Throwable exception =
        assertThrows(AssertionError.class, () -> rsc.transitForward(mgr.isValidValue().not()));
    assertThat(
        exception.getMessage(), containsString("input BDD must have a valid source constraint"));
  }
}
