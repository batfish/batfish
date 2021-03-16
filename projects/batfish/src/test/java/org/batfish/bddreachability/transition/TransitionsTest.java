package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.branch;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.eraseAndSet;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;
import static org.batfish.bddreachability.transition.Transitions.mergeDisjuncts;
import static org.batfish.bddreachability.transition.Transitions.or;
import static org.batfish.bddreachability.transition.Transitions.tryMergeDisjuncts;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDFiniteDomain;
import org.batfish.common.bdd.BDDOps;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Transitions}. */
public class TransitionsTest {
  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _zero = _pkt.getFactory().zero();
  private final BDD _one = _pkt.getFactory().one();

  private BDD var(int i) {
    return _pkt.getFactory().ithVar(i);
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
    Transition t4 = constraint(var(3));
    Transition c1 = new Composite(t1, t2);
    Transition c2 = new Composite(t3, t4);
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
  public void testBranchNestedThen1() {
    // else branch == nested branch's else branch
    BDD guard1 = var(0);
    BDD guard2 = var(1);
    Transition thn = constraint(var(2));
    Transition els = constraint(var(3));
    Transition actual = branch(guard1, new Branch(guard2, thn, els), els);
    Transition expected = branch(guard1.and(guard2), thn, els);
    assertEquals(expected, actual);
  }

  @Test
  public void testBranchNestedThen2() {
    // else branch == nested branch's then branch
    BDD guard1 = var(0);
    BDD guard2 = var(1);
    Transition t1 = constraint(var(2));
    Transition t2 = constraint(var(3));
    Transition actual = branch(guard1, new Branch(guard2, t1, t2), t1);
    Transition expected = branch(guard1.not().or(guard2), t1, t2);
    assertEquals(expected, actual);
  }

  @Test
  public void testBranchNestedElse1() {
    // then branch == nest branch's then branch
    BDD guard1 = var(0);
    BDD guard2 = var(1);
    Transition thn = constraint(var(2));
    Transition els = constraint(var(3));
    Transition actual = branch(guard1, thn, new Branch(guard2, thn, els));
    Transition expected = branch(guard1.or(guard2), thn, els);
    assertEquals(expected, actual);
  }

  @Test
  public void testBranchNestedElse2() {
    // then branch == nest branch's else branch
    BDD guard1 = var(0);
    BDD guard2 = var(1);
    Transition t1 = constraint(var(2));
    Transition t2 = constraint(var(3));
    Transition actual = branch(guard1, t1, new Branch(guard2, t2, t1));
    Transition expected = branch(guard1.not().and(guard2), t2, t1);
    assertEquals(expected, actual);
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
  public void testMergeComposed_Constraint_Branch() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD v2 = var(2);
    BDD v3 = var(3);
    Branch branch = new Branch(v1, constraint(v2), constraint(v3));

    // constraint implies guard => guard and true branch
    assertThat(mergeComposed(constraint(v1), branch), equalTo(constraint(v1.and(v2))));
    assertThat(
        mergeComposed(constraint(v1.and(v0)), branch), equalTo(constraint(v0.and(v1).and(v2))));

    // constraint implies !guard => !guard and false branch
    assertThat(mergeComposed(constraint(v1.not()), branch), equalTo(constraint(v1.not().and(v3))));
    assertThat(
        mergeComposed(constraint(v1.not().and(v0)), branch),
        equalTo(constraint(v1.not().and(v0).and(v3))));

    // constraint does not imply either guard or !guard => do not merge
    assertThat(mergeComposed(constraint(v0), branch), nullValue());
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
            eraseAndSet(finiteDomain.getVar(), _pkt.getFactory().one()));
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

  private static List<Transition> mergeCompositeTransitions(Transition... transitions) {
    return Transitions.mergeCompositeTransitions(Lists.newArrayList(transitions));
  }

  @Test
  public void testMergeCompositeTransitions1() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD v2 = var(2);
    Transition t0 = constraint(v0);
    Transition t1 = constraint(v1);
    Transition t2 = constraint(v2);
    List<Transition> actual = mergeCompositeTransitions(t0, t1, t2);
    assertThat(actual, contains(constraint(v0.and(v1).and(v2))));
  }

  @Test
  public void testMergeCompositeTransitions2() {
    BDD v1 = var(1);
    BDD v2 = var(2);
    BDD v3 = var(3);
    Transition t0 = constraint(v1);
    Transition t1 = eraseAndSet(v1, v1);
    Transition t2 = constraint(v2);
    Transition t3 = constraint(v3);
    List<Transition> actual = mergeCompositeTransitions(t0, t1, t2, t3);
    assertThat(actual, contains(t0, eraseAndSet(v1, v1.and(v2).and(v3))));
  }

  @Test
  public void testTryMergeDisjuncts_Identity_Constraint() {
    assertEquals(IDENTITY, tryMergeDisjuncts(IDENTITY, constraint(var(0))));
  }

  @Test
  public void testTryMergeDisjuncts_Constraint_Identity() {
    assertEquals(IDENTITY, tryMergeDisjuncts(constraint(var(0)), IDENTITY));
  }

  @Test
  public void testTryMergeDisjuncts_Constraint_Constraint() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    assertEquals(constraint(v0.or(v1)), tryMergeDisjuncts(constraint(v0), constraint(v1)));
  }

  @Test
  public void testTryMergeDisjuncts_EraseAndSet_EraseAndSet_SameVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD vars = v0.and(v1);
    EraseAndSet t1 = new EraseAndSet(vars, v0);
    EraseAndSet t2 = new EraseAndSet(vars, v1);
    assertEquals(new EraseAndSet(vars, v0.or(v1)), tryMergeDisjuncts(t1, t2));
  }

  @Test
  public void testTryMergeDisjuncts_EraseAndSet_EraseAndSet_DiffVars() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    EraseAndSet t1 = new EraseAndSet(v0, v0);
    EraseAndSet t2 = new EraseAndSet(v1, v1);
    assertNull(tryMergeDisjuncts(t1, t2));
  }

  @Test
  public void testMergeDisjunctsTo1() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    BDD v2 = var(2);
    BDD v3 = var(3);
    Collection<Transition> actual =
        mergeDisjuncts(
            ImmutableList.of(
                constraint(v0), constraint(v1),
                constraint(v2), constraint(v3)));
    Collection<Transition> expected = ImmutableSet.of(constraint(BDDOps.orNull(v0, v1, v2, v3)));
    assertEquals(expected, actual);
  }

  @Test
  public void testMergeDisjuncts() {
    BDD v0 = var(0);
    BDD v1 = var(1);
    Collection<Transition> actual =
        mergeDisjuncts(
            ImmutableList.of(
                // constraints get merged
                constraint(v0), constraint(v1),

                // EraseAndSets with v0 get merged
                eraseAndSet(v0, v0), eraseAndSet(v0, v1),

                // EraseAndSets with v1 get merged
                eraseAndSet(v1, v0), eraseAndSet(v1, v1)));
    Collection<Transition> expected =
        ImmutableSet.of(
            constraint(v0.or(v1)), eraseAndSet(v0, v0.or(v1)), eraseAndSet(v1, v0.or(v1)));
    assertEquals(expected, actual);
  }
}
