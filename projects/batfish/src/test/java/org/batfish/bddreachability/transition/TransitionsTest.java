package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.branch;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.eraseAndSet;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;
import static org.batfish.bddreachability.transition.Transitions.or;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDFiniteDomain;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDPairingFactory;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Transitions}. */
public class TransitionsTest {
  private final BDDPacket _pkt = new BDDPacket();
  private final BDDFactory _factory = _pkt.getFactory();
  private final BDD _zero = _factory.zero();
  private final BDD _one = _factory.one();

  private BDD var(int i) {
    return _factory.ithVar(i);
  }

  private Transition eraseVar(int i) {
    return eraseAndSet(var(i), _one);
  }

  private Transition setSrcIp(Ip value) {
    return constraint(_pkt.getSrcIp().value(value.asLong()));
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
    Transition t1 = constraint(var(0));
    Transition t2 = constraint(var(1));
    Transition t3 = constraint(var(2));
    Transition eraseAndSet = eraseAndSet(var(0), var(0));
    Transition or = or(t2, eraseAndSet);
    assertThat(or, instanceOf(Or.class));
    Transition c1 = compose(t1, or);
    assertThat(c1, instanceOf(Composite.class));
    Transition c2 = compose(t3, or);
    assertThat(c2, instanceOf(Composite.class));
    Transition c1c2 = compose(c1, c2);
    assertThat(c1c2, instanceOf(Composite.class));
    assertThat(((Composite) c1c2).getTransitions(), contains(t1, or, t3, or));
  }

  /** Compose simplifies to zero. */
  @Test
  public void composeToZero() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    assertEquals(
        ZERO,
        compose(
            constraint(v0.imp(v1)),
            constraint(v0.not().imp(v1.not())),
            constraint(v0.imp(v1.not())),
            constraint(v0.not().imp(v1))));
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
    Transition t1 = eraseVar(0);
    Transition t2 = eraseVar(1);
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
    assertEquals(thenTrans, branch(_one, thenTrans, elseTrans));
  }

  @Test
  public void testBranchGuardIsZero() {
    Transition thenTrans = constraint(var(0));
    Transition elseTrans = constraint(var(1));
    assertEquals(elseTrans, branch(_zero, thenTrans, elseTrans));
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
  public void testMergeComposedZero() {
    Transition t = constraint(var(0));
    assertEquals(ZERO, mergeComposed(ZERO, t));
    assertEquals(ZERO, mergeComposed(t, ZERO));
  }

  @Test
  public void testMergeComposedIdentity() {
    Transition t = constraint(var(0));
    assertEquals(t, mergeComposed(IDENTITY, t));
    assertEquals(t, mergeComposed(t, IDENTITY));
  }

  @Test
  public void testMergeComposedConstraints() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    assertEquals(constraint(v0.and(v1)), mergeComposed(constraint(v0), constraint(v1)));
  }

  @Test
  public void testMergeComposed_Constraint_EraseAndSet() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    Constraint constraint = new Constraint(v0);
    EraseAndSet eas = new EraseAndSet(v1, v1);
    Transition actual = mergeComposed(constraint, eas);
    Transition expected = eraseAndSet(v1, v0.and(v1));
    assertEquals(expected, actual);
  }

  @Test
  public void testMergeComposed_Constraint_RemoveSourceConstraint() {
    BDDSourceManager mgr =
        BDDSourceManager.forSources(
            _pkt, ImmutableSet.of("a", "b", "c", "d"), ImmutableSet.of("a", "b"));
    BDDFiniteDomain<String> finiteDomain = mgr.getFiniteDomain();
    BDD bdd = var(0);
    Constraint constraint = new Constraint(bdd);
    RemoveSourceConstraint remove = new RemoveSourceConstraint(mgr);
    checkState(!mgr.hasSourceConstraint(bdd));
    Transition actual = mergeComposed(constraint, remove);
    Transition expected =
        compose(
            constraint(bdd.and(finiteDomain.getIsValidConstraint())),
            eraseAndSet(finiteDomain.getVar(), _factory.one()));
    assertEquals(expected, actual);
  }

  @Test
  public void testMergeComposedEraseAndSetSameVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    Transition t1 = eraseAndSet(v0.and(v1), v0);
    Transition t2 = eraseAndSet(v0.and(v1), v1);
    assertEquals(t2, mergeComposed(t1, t2));
  }

  @Test
  public void testMergeComposedEraseAndSetDiffVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    Transition t1 = eraseAndSet(v0, v0);
    Transition t2 = eraseAndSet(v1, v1);
    Transition merged = eraseAndSet(v0.and(v1), v0.and(v1));
    assertEquals(merged, mergeComposed(t1, t2));
  }

  @Test
  public void testMergeComposedEraseAndSetOverlappingVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD v2 = var(2);
    Transition t1 = eraseAndSet(v0.and(v1), v0.and(v1));
    Transition t2 = eraseAndSet(v1.and(v2), v1.not().and(v2));
    Transition merged = eraseAndSet(v0.and(v1).and(v2), v0.and(v1.not()).and(v2));
    assertEquals(merged, mergeComposed(t1, t2));
  }

  @Test
  public void testMergeComposed_RemoveSourceConstraint_AddSourceConstraint_merge() {
    BDDSourceManager mgr =
        BDDSourceManager.forSources(
            _pkt, ImmutableSet.of("a", "b", "c", "d"), ImmutableSet.of("a", "b", "c"));
    checkState(mgr.isValidValue().isOne(), "manager needs to have isValidValue = 1");
    RemoveSourceConstraint remove = new RemoveSourceConstraint(mgr);
    AddSourceConstraint add = new AddSourceConstraint(mgr, "a");

    Transition actual = mergeComposed(remove, add);
    Transition expected =
        eraseAndSet(mgr.getFiniteDomain().getVar(), mgr.getSourceInterfaceBDD("a"));
    assertEquals(expected, actual);
  }

  @Test
  public void testMergeComposed_RemoveSourceConstraint_AddSourceConstraint_no_merge() {
    BDDSourceManager mgr =
        BDDSourceManager.forSources(
            _pkt, ImmutableSet.of("a", "b", "c", "d"), ImmutableSet.of("a", "b"));
    checkState(
        !mgr.isValidValue().isOne(), "manager needs to have nontrivial isValidValue constraint");
    RemoveSourceConstraint remove = new RemoveSourceConstraint(mgr);
    AddSourceConstraint add = new AddSourceConstraint(mgr, "a");
    assertNull(mergeComposed(remove, add));
  }

  @Test
  public void testCompose1() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD v2 = var(2);
    Transition t0 = constraint(v0);
    Transition t1 = constraint(v1);
    Transition t2 = constraint(v2);
    Transition actual = compose(t0, t1, t2);
    assertThat(actual, equalTo(constraint(v0.and(v1).and(v2))));
  }

  @Test
  public void testCompose2() {
    BDD v1 = var(1);
    BDD v2 = var(2);
    BDD v3 = var(3);
    Transition t0 = constraint(v1);
    Transition t1 = eraseAndSet(v1, v1);
    Transition t2 = constraint(v2);
    Transition t3 = constraint(v3);
    Transition actual = compose(t0, t1, t2, t3);
    assertThat(actual, instanceOf(Composite.class));
    assertThat(
        ((Composite) actual).getTransitions(), contains(t0, eraseAndSet(v1, v1.and(v2).and(v3))));
  }

  @Test
  public void testCompose_Constraint_Transform() {
    BDD v0 = var(0);
    BDD v0Prime = var(1);
    BDD v1 = var(2);

    BDDPairingFactory pairFactory = new BDDPairingFactory(new BDD[] {v0}, new BDD[] {v0Prime});

    BDD xorRel = v0.xor(v0Prime); // flip the bit
    Transform xorTransform = new Transform(xorRel, pairFactory);

    BDD trueToFalseRel = v0.diff(v0Prime); // partial function mapping true to false
    Transform trueToFalseTransform = new Transform(trueToFalseRel, pairFactory);

    BDD falseToTrueRel = v0.less(v0Prime); // partial function mapping true to false
    Transform falseToTrueTransform = new Transform(falseToTrueRel, pairFactory);

    // constraint first
    assertEquals(new Transform(xorRel.and(v1), pairFactory), compose(constraint(v1), xorTransform));
    assertEquals(trueToFalseTransform, compose(constraint(v0), xorTransform));
    assertEquals(ZERO, compose(constraint(v0.not()), trueToFalseTransform));

    // constraint second
    assertEquals(new Transform(xorRel.and(v1), pairFactory), compose(xorTransform, constraint(v1)));
    assertEquals(falseToTrueTransform, compose(xorTransform, constraint(v0)));
    assertEquals(ZERO, compose(falseToTrueTransform, constraint(v0.not())));
  }

  @Test
  public void testCompose_Transform_Transform() {
    BDD v0 = var(0);
    BDD v0Prime = var(1);
    BDD v1 = var(2);
    BDD v1Prime = var(3);

    BDDPairingFactory pairingFactory0 = new BDDPairingFactory(new BDD[] {v0}, new BDD[] {v0Prime});
    BDDPairingFactory pairingFactory1 = new BDDPairingFactory(new BDD[] {v1}, new BDD[] {v1Prime});

    BDD rel0 = v0.and(v0Prime);
    Transform transformV0 = new Transform(rel0, pairingFactory0);
    BDD rel1 = v1.and(v1Prime);
    Transform transformV1 = new Transform(rel1, pairingFactory1);

    // Transform::tryCompose does the real work -- more interesting tests there
    assertEquals(transformV0.tryCompose(transformV1).get(), compose(transformV0, transformV1));
  }

  @Test
  public void testOr_Identity_Constraint() {
    assertEquals(IDENTITY, or(IDENTITY, constraint(var(0))));
  }

  @Test
  public void testOr_Constraint_Identity() {
    assertEquals(IDENTITY, or(constraint(var(0)), IDENTITY));
  }

  @Test
  public void testOr_Constraint_Constraint() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    assertEquals(constraint(v0.or(v1)), or(constraint(v0), constraint(v1)));
  }

  @Test
  public void testOr_EraseAndSet_EraseAndSet_SameVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD vars = v0.and(v1);
    EraseAndSet t1 = new EraseAndSet(vars, v0);
    EraseAndSet t2 = new EraseAndSet(vars, v1);
    assertEquals(new EraseAndSet(vars, v0.or(v1)), or(t1, t2));
  }

  @Test
  public void testOr_EraseAndSet_EraseAndSet_DiffVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    EraseAndSet t1 = new EraseAndSet(v0, v0);
    EraseAndSet t2 = new EraseAndSet(v1, v1);
    assertEquals(new Or(ImmutableList.of(t1, t2)), or(t1, t2));
  }

  @Test
  public void testOrConstraints() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD v2 = var(2);
    BDD v3 = var(3);
    Transition actual = or(constraint(v0), constraint(v1), constraint(v2), constraint(v3));
    assertThat(actual, equalTo(constraint(_factory.orAll(v0, v1, v2, v3))));
  }

  @Test
  public void testOr() {
    BDD v0 = var(0);
    BDD v1 = var(1);

    BDD v0Prime = var(2);
    BDD v1Prime = var(3);

    BDDPairingFactory pairingFactory0 = new BDDPairingFactory(new BDD[] {v0}, new BDD[] {v0Prime});
    BDDPairingFactory pairingFactory1 = new BDDPairingFactory(new BDD[] {v1}, new BDD[] {v1Prime});

    Transform transformV0_1 = new Transform(v0.and(v0Prime), pairingFactory0);
    Transform transformV0_2 = new Transform(v0.not().and(v0Prime.not()), pairingFactory0);
    Transform transformV0_1or2 = new Transform(v0.biimp(v0Prime), pairingFactory0);

    assertEquals(transformV0_1.tryOr(transformV0_2).get(), transformV0_1or2);

    Transform transformV1_1 = new Transform(v1.and(v1Prime), pairingFactory1);
    Transform transformV1_2 = new Transform(v1.not().and(v1Prime.not()), pairingFactory1);
    Transform transformV1_1or2 = new Transform(v1.biimp(v1Prime), pairingFactory1);

    assertEquals(transformV1_1.tryOr(transformV1_2).get(), transformV1_1or2);

    Transition actual =
        or(
            // constraints get merged
            constraint(v0),
            constraint(v1),

            // EraseAndSets with v0 get merged
            eraseAndSet(v0, v0),
            eraseAndSet(v0, v1),

            // EraseAndSets with v1 get merged
            eraseAndSet(v1, v0),
            eraseAndSet(v1, v1),

            // Transforms with v0 get merged
            transformV0_1,
            transformV0_2,

            // Transforms with v1 get merged
            transformV1_1,
            transformV1_2);
    assertThat(actual, instanceOf(Or.class));
    assertThat(
        ((Or) actual).getTransitions(),
        containsInAnyOrder(
            constraint(v0.or(v1)),
            eraseAndSet(v0, v0.or(v1)),
            eraseAndSet(v1, v0.or(v1)),
            transformV0_1or2,
            transformV1_1or2));
  }
}
