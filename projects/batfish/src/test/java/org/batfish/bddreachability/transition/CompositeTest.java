package org.batfish.bddreachability.transition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link Composite}. */
public class CompositeTest {
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
  public void testComposite() {
    BDD bdd1 = dstIp("1.2.3.4");
    BDD bdd2 = srcIp("5.6.7.8");

    Transition transition = new Composite(new Constraint(bdd1), new Constraint(bdd2));
    assertThat(transition.transitForward(ONE), equalTo(bdd1.and(bdd2)));
    assertThat(transition.transitBackward(ONE), equalTo(bdd1.and(bdd2)));

    // constraints are inconsistent
    bdd2 = dstIp("5.6.7.8");
    transition = new Composite(new Constraint(bdd1), new Constraint(bdd2));
    assertThat(transition.transitForward(ONE), equalTo(ZERO));
    assertThat(transition.transitBackward(ONE), equalTo(ZERO));

    // bdd1 implies bdd2
    bdd2 = dstIp("1.2.3.4").or(dstIp("1.2.3.5"));
    transition = new Composite(new Constraint(bdd1), new Constraint(bdd2));
    assertThat(transition.transitForward(ONE), equalTo(bdd1));
    assertThat(transition.transitBackward(ONE), equalTo(bdd1));
  }
}
