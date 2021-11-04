package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager;
import org.batfish.bddreachability.LastHopOutgoingInterfaceManager;
import org.batfish.bddreachability.transition.GuardEraseAndSet.ValueBeforeAndAfter;
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
    if (thn instanceof Constraint && els instanceof Constraint) {
      BDD trueBdd = ((Constraint) thn).getConstraint();
      BDD falseBdd = ((Constraint) els).getConstraint();
      return constraint(guard.ite(trueBdd, falseBdd));
    }
    return or(compose(constraint(guard), thn), compose(constraint(guard.not()), els));
  }

  public static Transition compose(Transition... transitions) {
    Stack<Transition> mergedTransitions = new Stack<>();
    for (Transition transition : transitions) {
      if (transition == ZERO) {
        return ZERO;
      } else if (transition == IDENTITY) {
        continue;
      } else if (transition instanceof Composite) {
        List<Transition> composedTransitions = ((Composite) transition).getTransitions();
        // invariant: composedTransitions can't be merged any further, don't contain ZERO or
        // IDENTITY
        if (mergedTransitions.empty()) {
          mergedTransitions.addAll(composedTransitions);
        } else {
          // left-to-right, try to merge into the top of the stack
          int i = 0;
          while (i < composedTransitions.size()) {
            Transition left = mergedTransitions.pop();
            Transition right = composedTransitions.get(i++);
            @Nullable Transition merged = mergeComposed(left, right);
            if (merged == null) {
              // failed to merge. stop trying to merge and push the rest
              mergedTransitions.push(left);
              mergedTransitions.push(right);
              break;
            } else if (merged == ZERO) {
              return ZERO;
            } else if (merged != IDENTITY) {
              mergedTransitions.push(merged);
            }
          }
          // push the rest
          while (i < composedTransitions.size()) {
            mergedTransitions.push(composedTransitions.get(i++));
          }
        }
      } else {
        if (mergedTransitions.empty()) {
          mergedTransitions.push(transition);
        } else {
          Transition left = mergedTransitions.pop();
          @Nullable Transition merged = mergeComposed(left, transition);
          if (merged == null) {
            // failed to merge.
            mergedTransitions.push(left);
            mergedTransitions.push(transition);
          } else if (merged == ZERO) {
            return ZERO;
          } else if (merged != IDENTITY) {
            mergedTransitions.push(merged);
          }
        }
      }
    }
    if (mergedTransitions.isEmpty()) {
      return IDENTITY;
    }
    if (mergedTransitions.size() == 1) {
      return mergedTransitions.get(0);
    }
    return new Composite(mergedTransitions);
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
    if (t1 instanceof Constraint && t2 instanceof EraseAndSet) {
      BDD constraintBdd = ((Constraint) t1).getConstraint();
      EraseAndSet eas = (EraseAndSet) t2;
      BDD vars = eas.getEraseVars();
      BDD value = eas.getSetValue();
      return new GuardEraseAndSet(
          vars, ImmutableList.of(new ValueBeforeAndAfter(constraintBdd, value)));
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

  public static Transition or(Transition... transitions) {
    if (transitions.length == 1) {
      return transitions[0];
    }
    return or(Arrays.stream(transitions));
  }

  public static Transition or(Stream<Transition> transitions) {
    Iterator<Transition> flatTransitions =
        transitions
            .flatMap(t -> t instanceof Or ? ((Or) t).getTransitions().stream() : Stream.of(t))
            .iterator();
    boolean foundIdentity = false;
    List<Transition> disjuncts = new ArrayList<>();
    List<Constraint> constraints = null;
    List<EraseAndSet> eraseAndSets = null;
    List<GuardEraseAndSet> guardEraseAndSets = null;

    while (flatTransitions.hasNext()) {
      Transition t = flatTransitions.next();

      if (t == IDENTITY) {
        foundIdentity = true;
      } else if (t instanceof Constraint) {
        // constraints are redundant if identity is found, so ignore
        if (!foundIdentity) {
          if (constraints == null) {
            constraints = new ArrayList<>();
          }
          constraints.add((Constraint) t);
        }
      } else if (t instanceof EraseAndSet) {
        if (eraseAndSets == null) {
          eraseAndSets = new ArrayList<>();
        }
        eraseAndSets.add((EraseAndSet) t);
      } else if (t instanceof GuardEraseAndSet) {
        if (guardEraseAndSets == null) {
          guardEraseAndSets = new ArrayList<>();
        }
        guardEraseAndSets.add((GuardEraseAndSet) t);
      } else if (t != ZERO) { // ignore ZERO
        // unmergable
        disjuncts.add(t);
      }
    }

    // only add a constraint if we didn't find identity
    if (foundIdentity) {
      disjuncts.add(IDENTITY);
    } else if (constraints != null) {
      disjuncts.add(orConstraints(constraints));
    }
    if (eraseAndSets != null) {
      disjuncts.addAll(orEraseAndSets(eraseAndSets));
    }
    if (guardEraseAndSets != null) {
      disjuncts.addAll(orGuardEraseAndSets(guardEraseAndSets));
    }
    if (disjuncts.isEmpty()) {
      return ZERO;
    }
    if (disjuncts.size() == 1) {
      return Iterables.getOnlyElement(disjuncts);
    }
    return new Or(disjuncts);
  }

  private static Transition orConstraints(List<Constraint> constraints) {
    checkArgument(!constraints.isEmpty(), "orConstraints: constraints must be non-empty");
    if (constraints.size() == 1) {
      return constraints.get(0);
    } else {
      BDDFactory bddFactory = constraints.get(0).getConstraint().getFactory();
      return constraint(
          bddFactory.orAll(
              constraints.stream().map(Constraint::getConstraint).collect(Collectors.toList())));
    }
  }

  private static Collection<GuardEraseAndSet> orGuardEraseAndSets(
      List<GuardEraseAndSet> guardEraseAndSets) {
    checkArgument(
        !guardEraseAndSets.isEmpty(), "orGuardEraseAndSets: guardEraseAndSets must be non-empty");
    if (guardEraseAndSets.size() == 1) {
      return guardEraseAndSets;
    }
    Map<BDD, List<GuardEraseAndSet>> groupedByVars =
        guardEraseAndSets.stream()
            .collect(Collectors.groupingBy(GuardEraseAndSet::getVars, Collectors.toList()));
    if (groupedByVars.size() == guardEraseAndSets.size()) {
      // no two EraseAndSets had the same eraseVars
      return guardEraseAndSets;
    }
    return groupedByVars.entrySet().stream()
        .map(
            entry -> {
              BDD vars = entry.getKey();
              List<ValueBeforeAndAfter> valuesBeforeAndAfter =
                  entry.getValue().stream()
                      .map(GuardEraseAndSet::getValuesBeforeAndAfter)
                      .flatMap(List::stream)
                      .collect(ImmutableList.toImmutableList());
              return new GuardEraseAndSet(vars, valuesBeforeAndAfter);
            })
        .collect(Collectors.toList());
  }

  private static Collection<EraseAndSet> orEraseAndSets(List<EraseAndSet> eraseAndSets) {
    checkArgument(!eraseAndSets.isEmpty(), "orEraseAndSets: eraseAndSets must be non-empty");
    if (eraseAndSets.size() == 1) {
      return eraseAndSets;
    }
    Map<BDD, List<BDD>> eraseVarsToSetValue =
        eraseAndSets.stream()
            .collect(
                Collectors.groupingBy(
                    EraseAndSet::getEraseVars,
                    Collectors.mapping(EraseAndSet::getSetValue, Collectors.toList())));
    if (eraseVarsToSetValue.size() == eraseAndSets.size()) {
      // no two EraseAndSets had the same eraseVars
      return eraseAndSets;
    }
    return eraseVarsToSetValue.entrySet().stream()
        .map(
            entry -> {
              BDD eraseVars = entry.getKey();
              List<BDD> setValues = entry.getValue();
              BDDFactory factory = eraseVars.getFactory();
              // Skipping the eraseAndSet factory method: if the input EraseAndSets are well-formed
              // (i.e. created by eraseAndSet, then the output will be too. So this is equivalent
              // and has the type we need here.
              return new EraseAndSet(eraseVars, factory.orAll(setValues));
            })
        .collect(Collectors.toList());
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
