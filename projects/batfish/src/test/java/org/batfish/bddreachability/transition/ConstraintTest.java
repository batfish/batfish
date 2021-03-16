package org.batfish.bddreachability.transition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Constraint}. */
public class ConstraintTest {
  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _one = _pkt.getFactory().one();
  private final BDD _zero = _pkt.getFactory().zero();

  private BDD dstIp(String ip) {
    return _pkt.getDstIp().value(Ip.parse(ip).asLong());
  }

  private BDD srcIp(String ip) {
    return _pkt.getSrcIp().value(Ip.parse(ip).asLong());
  }

  @Test
  public void testConstraint() {
    BDD constraint = dstIp("1.2.3.4");
    Transition transition = new Constraint(constraint);

    assertThat(transition.transitForward(_one), equalTo(constraint));
    assertThat(transition.transitBackward(_one), equalTo(constraint));

    assertThat(transition.transitForward(_zero), equalTo(_zero));
    assertThat(transition.transitBackward(_zero), equalTo(_zero));

    // bdd is consistent with the constraint
    BDD bdd = srcIp("6.7.8.9");
    assertThat(transition.transitForward(bdd), equalTo(constraint.and(bdd)));
    assertThat(transition.transitBackward(bdd), equalTo(constraint.and(bdd)));

    // bdd is inconsistent with the constraint
    bdd = dstIp("2.3.4.5");
    assertThat(transition.transitForward(bdd), equalTo(_zero));
    assertThat(transition.transitBackward(bdd), equalTo(_zero));
  }
}
