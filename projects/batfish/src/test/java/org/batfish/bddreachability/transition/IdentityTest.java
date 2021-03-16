package org.batfish.bddreachability.transition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.junit.Test;

/** Tests of {@link Identity}. */
public class IdentityTest {
  @Test
  public void testIdentity() {
    BDDPacket pkt = new BDDPacket();
    BDD one = pkt.getFactory().one();
    BDD zero = pkt.getFactory().zero();

    assertThat(Identity.INSTANCE.transitForward(one), equalTo(one));
    assertThat(Identity.INSTANCE.transitBackward(one), equalTo(one));

    assertThat(Identity.INSTANCE.transitForward(zero), equalTo(zero));
    assertThat(Identity.INSTANCE.transitBackward(zero), equalTo(zero));
  }
}
