package org.batfish.bddreachability.transition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link Composite}. */
public class CompositeTest {
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
  public void testComposite() {
    BDD bdd1 = dstIp("1.2.3.4");
    BDD bdd2 = srcIp("5.6.7.8");

    Transition transition = new Composite(new Constraint(bdd1), new Constraint(bdd2));
    assertThat(transition.transitForward(_one), equalTo(bdd1.and(bdd2)));
    assertThat(transition.transitBackward(_one), equalTo(bdd1.and(bdd2)));

    // constraints are inconsistent
    bdd2 = dstIp("5.6.7.8");
    transition = new Composite(new Constraint(bdd1), new Constraint(bdd2));
    assertThat(transition.transitForward(_one), equalTo(_zero));
    assertThat(transition.transitBackward(_one), equalTo(_zero));

    // bdd1 implies bdd2
    bdd2 = dstIp("1.2.3.4").or(dstIp("1.2.3.5"));
    transition = new Composite(new Constraint(bdd1), new Constraint(bdd2));
    assertThat(transition.transitForward(_one), equalTo(bdd1));
    assertThat(transition.transitBackward(_one), equalTo(bdd1));
  }
}
