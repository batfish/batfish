package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.branch;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.or;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Transitions}. */
public class TransitionsTest {
  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD BDD_ZERO = PKT.getFactory().zero();
  private static final BDD BDD_ONE = PKT.getFactory().one();

  private BDD var(int i) {
    return PKT.getFactory().ithVar(i);
  }

  Transition setSrcIp(Ip value) {
    return constraint(PKT.getSrcIp().value(value.asLong()));
  }

  // Any composition containing ZERO is zero
  @Test
  public void composeWithZero() {
    assertThat(compose(ZERO), sameInstance(ZERO));
    assertThat(compose(IDENTITY, ZERO, IDENTITY), sameInstance(ZERO));
    assertThat(compose(ZERO, ZERO), sameInstance(ZERO));
    assertThat(compose(ZERO, setSrcIp(Ip.parse("1.2.3.4"))), sameInstance(ZERO));
    assertThat(compose(setSrcIp(Ip.parse("1.2.3.4")), ZERO), sameInstance(ZERO));
  }

  // Composing with IDENTITY is a no-op.
  @Test
  public void composeWithIdentity() {
    assertThat(compose(IDENTITY), sameInstance(IDENTITY));
    assertThat(compose(IDENTITY, IDENTITY), sameInstance(IDENTITY));
    assertThat(
        compose(IDENTITY, setSrcIp(Ip.parse("1.2.3.4"))), equalTo(setSrcIp(Ip.parse("1.2.3.4"))));
    assertThat(
        compose(setSrcIp(Ip.parse("1.2.3.4")), IDENTITY), equalTo(setSrcIp(Ip.parse("1.2.3.4"))));
  }

  // Composing with Compose flattens
  @Test
  public void composeWithCompose() {
    Transition t1 = setSrcIp(Ip.parse("1.2.3.4"));
    Transition t2 = setSrcIp(Ip.parse("1.2.3.5"));
    Transition t3 = setSrcIp(Ip.parse("1.2.3.6"));
    Transition t4 = setSrcIp(Ip.parse("1.2.3.7"));
    Transition c1 = compose(t1, t2);
    Transition c2 = compose(t3, t4);
    // Sanity check data prep
    assertThat(c1, instanceOf(Composite.class));
    assertThat(c2, instanceOf(Composite.class));

    assertThat(compose(c1, c2), equalTo(compose(t1, t2, t3, t4)));
  }

  // Or with ZERO is a no-op
  @Test
  public void orWithZero() {
    assertThat(or(ZERO), sameInstance(ZERO));
    assertThat(or(IDENTITY, ZERO), sameInstance(IDENTITY));
    assertThat(or(ZERO, ZERO), sameInstance(ZERO));
    assertThat(or(ZERO, setSrcIp(Ip.parse("1.2.3.4"))), equalTo(setSrcIp(Ip.parse("1.2.3.4"))));
    assertThat(or(setSrcIp(Ip.parse("1.2.3.4")), ZERO), equalTo(setSrcIp(Ip.parse("1.2.3.4"))));
  }

  // Or with Or flattens and uniquifies
  @Test
  public void orWithOr() {
    Transition t1 = setSrcIp(Ip.parse("1.2.3.4"));
    Transition t2 = setSrcIp(Ip.parse("1.2.3.5"));
    Transition o1 = or(t1, IDENTITY);
    Transition o2 = or(IDENTITY, t2);
    // Sanity check data prep
    assertThat(o1, instanceOf(Or.class));
    assertThat(o2, instanceOf(Or.class));

    assertThat(or(o1, o2), equalTo(or(t1, IDENTITY, t2)));
  }

  @Test
  public void testBranchGuardIsOne() {
    Transition thenTrans = constraint(var(0));
    Transition elseTrans = constraint(var(1));
    assertEquals(thenTrans, branch(BDD_ONE, thenTrans, elseTrans));
  }

  @Test
  public void testBranchGuardIsZero() {
    Transition thenTrans = constraint(var(0));
    Transition elseTrans = constraint(var(1));
    assertEquals(elseTrans, branch(BDD_ZERO, thenTrans, elseTrans));
  }

  @Test
  public void testBranchThenIsZero() {
    BDD guard = var(0);
    Transition elseTrans = constraint(var(1));
    assertEquals(compose(constraint(guard.not()), elseTrans), branch(guard, ZERO, elseTrans));
  }

  @Test
  public void testBranchElseIsZero() {
    BDD guard = var(0);
    Transition thenTrans = constraint(var(1));
    assertEquals(compose(constraint(guard), thenTrans), branch(guard, thenTrans, ZERO));
  }

  @Test
  public void testBranchConstraints() {
    BDD guard = var(0);
    BDD thn = var(1);
    BDD els = var(2);
    assertEquals(constraint(guard.ite(thn, els)), branch(guard, constraint(thn), constraint(els)));
  }

  @Test
  public void testBranchThenEqualsElse() {
    BDD guard = var(0);
    Transition trans = constraint(var(1));
    assertEquals(trans, branch(guard, trans, trans));
  }

  @Test
  public void testBranchNestedThen() {
    BDD guard1 = var(0);
    BDD guard2 = var(1);
    Transition thn = constraint(var(2));
    Transition els = constraint(var(3));
    Transition expected = new Branch(guard1.and(guard2), thn, els);
    Transition actual = branch(guard1, new Branch(guard2, thn, els), els);
    assertEquals(expected, actual);
  }

  @Test
  public void testBranchNestedElse() {
    BDD guard1 = var(0);
    BDD guard2 = var(1);
    Transition thn = constraint(var(2));
    Transition els = constraint(var(3));
    Transition expected = new Branch(guard1.or(guard2), thn, els);
    Transition actual = branch(guard1, thn, new Branch(guard2, thn, els));
    assertEquals(expected, actual);
  }
}
