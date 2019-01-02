package org.batfish.bddreachability.transition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Constraint}. */
public class ConstraintTest {
  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD ONE = PKT.getFactory().one();
  private static final BDD ZERO = PKT.getFactory().zero();

  private static BDD dstIp(String ip) {
    return PKT.getDstIp().value(new Ip(ip).asLong());
  }

  private static BDD srcIp(String ip) {
    return PKT.getSrcIp().value(new Ip(ip).asLong());
  }

  @Test
  public void testConstraint() {
    BDD constraint = dstIp("1.2.3.4");
    Transition transition = new Constraint(constraint);

    assertThat(transition.transitForward(ONE), equalTo(constraint));
    assertThat(transition.transitBackward(ONE), equalTo(constraint));

    assertThat(transition.transitForward(ZERO), equalTo(ZERO));
    assertThat(transition.transitBackward(ZERO), equalTo(ZERO));

    // bdd is consistent with the constraint
    BDD bdd = srcIp("6.7.8.9");
    assertThat(transition.transitForward(bdd), equalTo(constraint.and(bdd)));
    assertThat(transition.transitBackward(bdd), equalTo(constraint.and(bdd)));

    // bdd is inconsistent with the constraint
    bdd = dstIp("2.3.4.5");
    assertThat(transition.transitForward(bdd), equalTo(ZERO));
    assertThat(transition.transitBackward(bdd), equalTo(ZERO));
  }
}
