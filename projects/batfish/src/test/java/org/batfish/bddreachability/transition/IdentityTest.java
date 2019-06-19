package org.batfish.bddreachability.transition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.junit.Test;

/** Tests of {@link Identity}. */
public class IdentityTest {
  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD ONE = PKT.getFactory().one();
  private static final BDD ZERO = PKT.getFactory().zero();

  @Test
  public void testIdentity() {
    assertThat(Identity.INSTANCE.transitForward(ONE), equalTo(ONE));
    assertThat(Identity.INSTANCE.transitBackward(ONE), equalTo(ONE));

    assertThat(Identity.INSTANCE.transitForward(ZERO), equalTo(ZERO));
    assertThat(Identity.INSTANCE.transitBackward(ZERO), equalTo(ZERO));
  }
}
