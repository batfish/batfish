package org.batfish.bddreachability.transition;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.batfish.common.bdd.BDDMatchers.isZero;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.junit.Test;

public class RemoveSourceConstraintTest {
  @Test
  public void testTransitForward() {
    BDDPacket pkt = new BDDPacket();
    BDDSourceManager mgr = BDDSourceManager.forInterfaces(pkt, ImmutableSet.of("A", "B"));
    RemoveSourceConstraint rsc = new RemoveSourceConstraint(mgr);

    // one forward is one, backward is anything valid
    assertThat(rsc.transitForward(pkt.getFactory().one()), isOne());
    assertThat(rsc.transitBackward(pkt.getFactory().one()), equalTo(mgr.isValidValue()));

    // zero forward or backward stays zero
    assertThat(rsc.transitForward(pkt.getFactory().zero()), isZero());
    assertThat(rsc.transitBackward(pkt.getFactory().zero()), isZero());

    // any valid source forward is one
    assertThat(rsc.transitForward(mgr.getSourceInterfaceBDD("A")), isOne());
    assertThat(rsc.transitForward(mgr.getSourceInterfaceBDD("B")), isOne());
    assertThat(
        rsc.transitForward(mgr.getSourceInterfaceBDD(SOURCE_ORIGINATING_FROM_DEVICE)), isOne());

    // an invalid source forward is zero
    assertThat(
        "invalid test, there are no 'extra' sources", mgr.isValidValue().not(), not(isZero()));
    assertThat(rsc.transitForward(mgr.isValidValue().not()), isZero());
  }
}
