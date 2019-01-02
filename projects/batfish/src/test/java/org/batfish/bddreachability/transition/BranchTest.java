package org.batfish.bddreachability.transition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link Branch}. */
public class BranchTest {
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
  public void testBranch() {
    BDD guard = dstIp("1.2.3.4");
    BDD trueBranchBdd = srcIp("3.3.3.3");
    BDD falseBranchBdd = srcIp("4.4.4.4");
    Transition trueBranch = new Constraint(trueBranchBdd);
    Transition falseBranch = new Constraint(falseBranchBdd);
    Transition transition = new Branch(guard, trueBranch, falseBranch);

    assertThat(transition.transitForward(ONE), equalTo(guard.ite(trueBranchBdd, falseBranchBdd)));
    assertThat(transition.transitBackward(ONE), equalTo(guard.ite(trueBranchBdd, falseBranchBdd)));

    assertThat(transition.transitForward(ZERO), equalTo(ZERO));
    assertThat(transition.transitBackward(ZERO), equalTo(ZERO));

    assertThat(transition.transitForward(guard), equalTo(guard.and(trueBranchBdd)));
    assertThat(transition.transitBackward(guard), equalTo(guard.and(trueBranchBdd)));

    assertThat(transition.transitForward(guard.not()), equalTo(guard.not().and(falseBranchBdd)));
    assertThat(transition.transitBackward(guard.not()), equalTo(guard.not().and(falseBranchBdd)));
  }
}
