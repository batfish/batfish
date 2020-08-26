package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager;
import org.batfish.bddreachability.LastHopOutgoingInterfaceManager;
import org.batfish.common.bdd.BDDFiniteDomain;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDSourceManager;

/** Smart constructors of {@link Transition}. */
public final class Transitions {
  private Transitions() {}

  public static final Transition IDENTITY = Identity.INSTANCE;

  public static final Transition ZERO = Zero.INSTANCE;

  public static Transition branch(BDD guard, Transition thn, Transition els) {
    if (guard.isOne()) {
      return thn;
    }
    if (guard.isZero()) {
      return els;
    }
    if (els == ZERO) {
      return compose(constraint(guard), thn);
    }
    if (thn == ZERO) {
      return compose(constraint(guard.not()), els);
    }
    if (thn.equals(els)) {
      return thn;
    }
    if (thn instanceof Branch) {
      Branch thnBranch = (Branch) thn;
      if (els.equals(thnBranch.getFalseBranch())) {
        return branch(guard.and(thnBranch.getGuard()), thnBranch.getTrueBranch(), els);
      }
      if (els.equals(thnBranch.getTrueBranch())) {
        return branch(guard.imp(thnBranch.getGuard()), els, thnBranch.getFalseBranch());
      }
      // fall through
    }
    if (els instanceof Branch) {
      Branch elsBranch = (Branch) els;
      if (thn.equals(elsBranch.getTrueBranch())) {
        return branch(guard.or(elsBranch.getGuard()), thn, elsBranch.getFalseBranch());
      }
      if (thn.equals(elsBranch.getFalseBranch())) {
        return branch(elsBranch.getGuard().diff(guard), elsBranch.getTrueBranch(), thn);
      }
      // fall through
    }
    if (thn instanceof Constraint && els instanceof Constraint) {
      BDD trueBdd = ((Constraint) thn).getConstraint();
      BDD falseBdd = ((Constraint) els).getConstraint();
      return constraint(guard.ite(trueBdd, falseBdd));
    }
    return new Branch(guard, thn, els);
  }

  public static Transition compose(Transition... transitions) {
    if (Stream.of(transitions).anyMatch(t -> t == ZERO)) {
      return ZERO;
    }
    Stream<Transition> flatTransitions =
        Stream.of(transitions)
            .flatMap(
                t ->
                    t instanceof Composite
                        ? ((Composite) t).getTransitions().stream()
                        : Stream.of(t));
    List<Transition> nonIdentityTransitions =
        flatTransitions
            .filter(transition -> transition != IDENTITY)
            .collect(ImmutableList.toImmutableList());
    if (nonIdentityTransitions.isEmpty()) {
      return IDENTITY;
    }
    List<Transition> mergedTransitions = mergeCompositeTransitions(nonIdentityTransitions);
    if (mergedTransitions.size() == 1) {
      return mergedTransitions.get(0);
    }
    return new Composite(mergedTransitions);
  }

  static List<Transition> mergeCompositeTransitions(List<Transition> transitions) {
    if (transitions.size() < 2) {
      return transitions;
    }

    Stack<Transition> stack = new Stack<>();
    for (Transition transition : transitions) {
      stack.push(transition);

      while (stack.size() > 1) {
        Transition second = stack.pop();
        Transition first = stack.pop();
        @Nullable Transition merged = mergeComposed(first, second);
        if (merged == null) {
          stack.push(first);
          stack.push(second);
          break;
        } else {
          stack.push(merged);
        }
      }
    }
    return stack;
  }

  /**
   * Try to compose two transitions by merging into a "simpler" transition.
   *
   * <p>The definition of a single transition being simpler than two transitions is open-ended, but
   * typically involves reducing the total number of operations involved (i.e. is not a simple
   * {@link Composite} of the two input transitions).
   *
   * <p>If no simplification can be found, return {@code null}.
   */
  public static @Nullable Transition mergeComposed(Transition t1, Transition t2) {
    if (t1 == ZERO || t2 == ZERO) {
      return ZERO;
    }
    if (t1 == IDENTITY) {
      return t2;
    }
    if (t2 == IDENTITY) {
      return t1;
    }
    if (t1 instanceof Constraint && t2 instanceof Constraint) {
      BDD bdd1 = ((Constraint) t1).getConstraint();
      BDD bdd2 = ((Constraint) t2).getConstraint();
      return constraint(bdd1.and(bdd2));
    }
    if (t1 instanceof Constraint && t2 instanceof Branch) {
      BDD constraintBdd = ((Constraint) t1).getConstraint();
      Branch branch = (Branch) t2;
      BDD guard = ((Branch) t2).getGuard();
      if (!constraintBdd.andSat(guard)) {
        // True branch can never be taken. Also, t1 subsumes guard.not(), so we can elide it.
        return compose(t1, branch.getFalseBranch());
      }
      if (!constraintBdd.diffSat(guard)) {
        // False branch can never be taken. Also, t1 subsumes guard, so we can elide it.
        return compose(t1, branch.getTrueBranch());
      }
      // fall through
    }
    if (t1 instanceof Constraint && t2 instanceof EraseAndSet) {
      BDD constraintBdd = ((Constraint) t1).getConstraint();
      EraseAndSet eas = (EraseAndSet) t2;
      BDD eraseVars = eas.getEraseVars();
      if (constraintBdd.exist(eraseVars).equals(constraintBdd)) {
        // constraint doesn't refer to eraseVars
        return eraseAndSet(eraseVars, constraintBdd.and(eas.getSetValue()));
      }
      // fall through
    }
    if (t1 instanceof Constraint && t2 instanceof RemoveSourceConstraint) {
      BDD constraintBdd = ((Constraint) t1).getConstraint();
      BDDSourceManager mgr = ((RemoveSourceConstraint) t2).getSourceManager();
      BDDFiniteDomain<String> finiteDomain = mgr.getFiniteDomain();
      return compose(
          constraint(constraintBdd.and(finiteDomain.getIsValidConstraint())),
          eraseAndSet(finiteDomain.getVar(), constraintBdd.getFactory().one()));
    }
    if (t1 instanceof EraseAndSet && t2 instanceof Constraint) {
      EraseAndSet eas = (EraseAndSet) t1;
      BDD vars = eas.getEraseVars();
      BDD value = eas.getSetValue();
      BDD constraint = ((Constraint) t2).getConstraint();
      return eraseAndSet(vars, value.and(constraint));
    }
    if (t1 instanceof EraseAndSet && t2 instanceof EraseAndSet) {
      EraseAndSet eas1 = (EraseAndSet) t1;
      EraseAndSet eas2 = (EraseAndSet) t2;
      BDD vars1 = eas1.getEraseVars();
      BDD vars2 = eas2.getEraseVars();
      BDD val1 = eas1.getSetValue();
      BDD val2 = eas2.getSetValue();
      if (vars1.equals(vars2)) {
        return eas2;
      } else if (vars1.exist(vars2).equals(vars1)) {
        return eraseAndSet(vars1.and(vars2), val1.and(val2));
      } else {
        // variables sets are different but overlap. probably would only happen as a result of
        // previous merges
        return eraseAndSet(vars1.and(vars2), val1.exist(vars2).and(val2));
      }
    }
    if (t1 instanceof RemoveSourceConstraint && t2 instanceof AddSourceConstraint) {
      RemoveSourceConstraint remove = (RemoveSourceConstraint) t1;
      AddSourceConstraint add = (AddSourceConstraint) t2;
      BDDSourceManager mgr = remove.getSourceManager();

      if (!mgr.isValidValue().isOne()) {
        // cannot merge, because we'd lose the isValidConstraint RemoveSourceConstraint imposes in
        // the backward direction.
        return null;
      }

      // each node has its own source mgr, but all mgrs are backed by a single BDDInteger
      checkState(
          mgr.getFiniteDomain().getVar() == add.getSourceManager().getFiniteDomain().getVar(),
          "all source managers should have the same BDDInteger");

      BDDInteger var = mgr.getFiniteDomain().getVar();
      return eraseAndSet(var, add.getSourceBdd());
    }
    // couldn't merge
    return null;
  }

  public static Transition constraint(BDD bdd) {
    return bdd.isOne() ? IDENTITY : bdd.isZero() ? ZERO : new Constraint(bdd);
  }

  public static Transition eraseAndSet(BDD var, BDD value) {
    if (var.isOne()) {
      return constraint(value);
    } else if (value.isZero()) {
      return ZERO;
    } else {
      return new EraseAndSet(var, value);
    }
  }

  public static Transition eraseAndSet(BDDInteger var, BDD value) {
    if (var.size() == 0) {
      return constraint(value);
    } else {
      return new EraseAndSet(var.getVars(), value);
    }
  }

  @Deprecated
  public static Transition or() {
    throw new IllegalArgumentException(
        "Don't call or() with no Transitions -- just use Zero instead.");
  }

  @VisibleForTesting
  static Collection<Transition> mergeDisjuncts(List<Transition> origDisjuncts) {
    if (origDisjuncts.size() < 2) {
      return origDisjuncts;
    }

    Set<Transition> disjuncts = new HashSet<>(origDisjuncts);

    // keep merge until we can't merge any more
    boolean merged = tryMergeDisjunctSet(disjuncts);
    while (merged) {
      merged = tryMergeDisjunctSet(disjuncts);
    }

    return disjuncts;
  }

  private static boolean tryMergeDisjunctSet(Set<Transition> disjuncts) {
    if (disjuncts.size() < 2) {
      return false;
    }

    for (Transition t1 : disjuncts) {
      for (Transition t2 : disjuncts) {
        if (t1 == t2) {
          continue;
        }
        @Nullable Transition merged = tryMergeDisjuncts(t1, t2);
        if (merged != null) {
          disjuncts.remove(t1);
          disjuncts.remove(t2);
          disjuncts.add(merged);
          return true;
        }
      }
    }
    return false;
  }

  @VisibleForTesting
  static Transition tryMergeDisjuncts(Transition t1, Transition t2) {
    if (t1 == IDENTITY && t2 instanceof Constraint) {
      return t1;
    }
    if (t1 instanceof Constraint && t2 == IDENTITY) {
      return t2;
    }
    if (t1 instanceof Constraint && t2 instanceof Constraint) {
      Constraint c1 = (Constraint) t1;
      Constraint c2 = (Constraint) t2;
      return constraint(c1.getConstraint().or(c2.getConstraint()));
    }
    if (t1 instanceof EraseAndSet && t2 instanceof EraseAndSet) {
      EraseAndSet eas1 = (EraseAndSet) t1;
      EraseAndSet eas2 = (EraseAndSet) t2;

      BDD vars1 = eas1.getEraseVars();
      BDD vars2 = eas2.getEraseVars();

      BDD val1 = eas1.getSetValue();
      BDD val2 = eas2.getSetValue();

      if (vars1.equals(vars2)) {
        return eraseAndSet(vars1, val1.or(val2));
      }

      // fall through
    }
    // couldn't merge
    return null;
  }

  public static Transition or(Transition... transitions) {
    Stream<Transition> flatUniqueTransitions =
        Stream.of(transitions)
            .flatMap(t -> t instanceof Or ? ((Or) t).getTransitions().stream() : Stream.of(t))
            .distinct();
    List<Transition> nonZeroTransitions =
        flatUniqueTransitions
            .filter(transition -> transition != ZERO)
            .collect(ImmutableList.toImmutableList());
    if (nonZeroTransitions.isEmpty()) {
      return ZERO;
    }
    Collection<Transition> mergedTransitions = mergeDisjuncts(nonZeroTransitions);
    if (mergedTransitions.size() == 1) {
      return Iterables.getOnlyElement(mergedTransitions);
    }
    return new Or(mergedTransitions);
  }

  public static Transition addLastHopConstraint(
      @Nullable LastHopOutgoingInterfaceManager mgr,
      String sendingNode,
      String sendingIface,
      String recvNode,
      String recvIface) {
    return mgr == null || !mgr.isTrackedReceivingNode(recvNode)
        ? IDENTITY
        : new AddLastHopConstraint(mgr, sendingNode, sendingIface, recvNode, recvIface);
  }

  public static Transition addNoLastHopConstraint(
      @Nullable LastHopOutgoingInterfaceManager mgr, String recvNode, String recvIface) {
    return mgr == null || !mgr.isTrackedReceivingNode(recvNode)
        ? IDENTITY
        : new AddNoLastHopConstraint(mgr, recvNode, recvIface);
  }

  public static Transition addOutgoingOriginalFlowFiltersConstraint(
      BDDOutgoingOriginalFlowFilterManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new AddOutgoingOriginalFlowFiltersConstraint(mgr);
  }

  public static Transition addOriginatingFromDeviceConstraint(BDDSourceManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new AddSourceConstraint(mgr);
  }

  public static Transition addSourceInterfaceConstraint(BDDSourceManager mgr, String iface) {
    return mgr.isTrivial() ? IDENTITY : new AddSourceConstraint(mgr, iface);
  }

  public static Transition removeNodeSpecificConstraints(
      String node,
      @Nullable LastHopOutgoingInterfaceManager lastHopMgr,
      BDDOutgoingOriginalFlowFilterManager originalFlowFilterMgr,
      BDDSourceManager sourceMgr) {
    return compose(
        removeLastHopConstraint(lastHopMgr, node),
        removeSourceConstraint(sourceMgr),
        removeOutgoingInterfaceConstraints(originalFlowFilterMgr));
  }

  private static Transition removeLastHopConstraint(
      @Nullable LastHopOutgoingInterfaceManager mgr, String node) {
    return mgr == null || !mgr.isTrackedReceivingNode(node)
        ? IDENTITY
        : new RemoveLastHopConstraint(mgr, node);
  }

  @VisibleForTesting
  public static Transition removeOutgoingInterfaceConstraints(
      BDDOutgoingOriginalFlowFilterManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new RemoveOutgoingInterfaceConstraints(mgr);
  }

  @VisibleForTesting
  public static Transition removeSourceConstraint(BDDSourceManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new RemoveSourceConstraint(mgr);
  }

  public static Transition reverse(Transition transition) {
    if (transition == IDENTITY || transition == ZERO || transition instanceof Constraint) {
      // transition is bijective, so is its own reverse
      return transition;
    }
    return new Reverse(transition);
  }
}
