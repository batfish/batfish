package org.batfish.bddreachability.transition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Transition} classes. */
public class TransitionTest {
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
  public void testIdentity() {
    assertThat(Identity.INSTANCE.transitForward(ONE), equalTo(ONE));
    assertThat(Identity.INSTANCE.transitBackward(ONE), equalTo(ONE));

    assertThat(Identity.INSTANCE.transitForward(ZERO), equalTo(ZERO));
    assertThat(Identity.INSTANCE.transitBackward(ZERO), equalTo(ZERO));
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

  @Test
  public void testEraseAndSet() {
    BDD setBdd = dstIp("1.1.1.1");
    BDD vars = Arrays.stream(PKT.getDstIp().getBitvec()).reduce(PKT.getFactory().one(), BDD::and);
    Transition transition = new EraseAndSet(vars, setBdd);

    assertThat(transition.transitForward(ONE), equalTo(setBdd));
    assertThat(transition.transitBackward(ONE), equalTo(ONE));

    assertThat(transition.transitForward(setBdd), equalTo(setBdd));
    assertThat(transition.transitBackward(setBdd), equalTo(ONE));

    assertThat(transition.transitForward(setBdd.not()), equalTo(setBdd));
    assertThat(transition.transitBackward(setBdd.not()), equalTo(ZERO));

    // more interesting example
    BDD bdd = setBdd.ite(srcIp("5.5.5.5"), srcIp("6.6.6.6"));
    /* transitForward: After erasing setBdd, we end up with srcIp("5.5.5.5").or(srcIp("6.6.6.6")).
     * Then we and the setBdd constraint back in.
     */
    assertThat(
        transition.transitForward(bdd), equalTo(setBdd.and(srcIp("5.5.5.5").or(srcIp("6.6.6.6")))));

    /* transitBackward: After applying the setBdd constraint, we have setBdd.and(srcIp("5.5.5.5")).
     * The false branch of the ite is inconsistent with the outputs of the transition. Then we erase
     * the dstIp vars and are left with just srcIp("5.5.5.5").
     */
    assertThat(transition.transitBackward(bdd), equalTo(srcIp("5.5.5.5")));
  }
}
