package org.batfish.bddreachability.transition;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.LastHopOutgoingInterfaceManager;
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
      if (els.equals(((Branch) thn).getFalseBranch())) {
        return branch(guard.and(thnBranch.getGuard()), thnBranch.getTrueBranch(), els);
      }
      if (els.equals(((Branch) thn).getTrueBranch())) {
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
    if (nonIdentityTransitions.size() == 1) {
      return nonIdentityTransitions.get(0);
    }
    return new Composite(nonIdentityTransitions);
  }

  public static Transition constraint(BDD bdd) {
    return bdd.isOne() ? IDENTITY : bdd.isZero() ? ZERO : new Constraint(bdd);
  }

  public static Transition eraseAndSet(BDD var, BDD value) {
    return value.isOne() ? IDENTITY : new EraseAndSet(var, value);
  }

  public static Transition eraseAndSet(BDDInteger var, BDD value) {
    return value.isOne()
        ? IDENTITY
        : new EraseAndSet(
            Arrays.stream(var.getBitvec()).reduce(var.getFactory().one(), BDD::and), value);
  }

  @Deprecated
  public static Transition or() {
    throw new IllegalArgumentException(
        "Don't call or() with no Transitions -- just use Zero instead.");
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
    if (nonZeroTransitions.size() == 1) {
      return nonZeroTransitions.get(0);
    }
    return new Or(nonZeroTransitions);
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

  public static Transition addOriginatingFromDeviceConstraint(BDDSourceManager mgr) {
    return mgr.isTrivial() ? IDENTITY : new AddSourceConstraint(mgr);
  }

  public static Transition addSourceInterfaceConstraint(BDDSourceManager mgr, String iface) {
    return mgr.isTrivial() ? IDENTITY : new AddSourceConstraint(mgr, iface);
  }

  public static Transition removeLastHopConstraint(
      @Nullable LastHopOutgoingInterfaceManager mgr, String node) {
    return mgr == null || !mgr.isTrackedReceivingNode(node)
        ? IDENTITY
        : new RemoveLastHopConstraint(mgr, node);
  }

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
