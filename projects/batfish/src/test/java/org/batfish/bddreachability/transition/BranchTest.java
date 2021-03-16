package org.batfish.bddreachability.transition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link Branch}. */
public class BranchTest {
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
  public void testBranch() {
    BDD guard = dstIp("1.2.3.4");
    BDD trueBranchBdd = srcIp("3.3.3.3");
    BDD falseBranchBdd = srcIp("4.4.4.4");
    Transition trueBranch = new Constraint(trueBranchBdd);
    Transition falseBranch = new Constraint(falseBranchBdd);
    Transition transition = new Branch(guard, trueBranch, falseBranch);

    assertThat(transition.transitForward(_one), equalTo(guard.ite(trueBranchBdd, falseBranchBdd)));
    assertThat(transition.transitBackward(_one), equalTo(guard.ite(trueBranchBdd, falseBranchBdd)));

    assertThat(transition.transitForward(_zero), equalTo(_zero));
    assertThat(transition.transitBackward(_zero), equalTo(_zero));

    assertThat(transition.transitForward(guard), equalTo(guard.and(trueBranchBdd)));
    assertThat(transition.transitBackward(guard), equalTo(guard.and(trueBranchBdd)));

    assertThat(transition.transitForward(guard.not()), equalTo(guard.not().and(falseBranchBdd)));
    assertThat(transition.transitBackward(guard.not()), equalTo(guard.not().and(falseBranchBdd)));
  }
}
