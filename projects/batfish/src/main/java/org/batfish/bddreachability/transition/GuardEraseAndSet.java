package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.bddreachability.transition.Transitions.ZERO;

import com.google.common.base.Objects;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

public final class GuardEraseAndSet implements Transition {
  public Transition andThen(EraseAndSet eas) {
    // TODO better way?
    // eas.transitForward(_forwardRelation.replace(_swap)).replace(_swap)
    // relies on the invariant that eas is a noop on prime vars
    BDD before = _forwardRelation.project(_vars);
    BDD after = _forwardRelation.project(_vars.replace(_swap));
    BDD newAfter = eas.transitForward(after);
    BDD vars = _vars.and(eas.getEraseVars());
    return new GuardEraseAndSet(vars, before, newAfter);
  }

  public static final class ValueBeforeAndAfter {
    private final BDD _before;
    private final BDD _after;

    ValueBeforeAndAfter(BDD before, BDD after) {
      checkArgument(
          !before.isZero() && !after.isZero(),
          "Value constraints before/after must be satisfiable");
      _before = before;
      _after = after;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ValueBeforeAndAfter)) {
        return false;
      }
      ValueBeforeAndAfter that = (ValueBeforeAndAfter) o;
      return _before.equals(that._before) && _after.equals(that._after);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_before, _after);
    }
  }

  private final BDD _vars;
  private final BDD _forwardRelation;
  private final BDDPairing _swap;

  // computed lazily
  private final Supplier<BDD> _backwardRelation = Suppliers.memoize(this::computeBackwardRelation);

  private static final Map<BDD, BDDPairing> PAIRING_CACHE = new HashMap<>();

  private static final class BDDPairings {
    private final BDDPairing _toPrime;
    private final BDDPairing _fromPrime;

    private BDDPairings(BDDPairing toPrime, BDDPairing fromPrime) {
      _toPrime = toPrime;
      _fromPrime = fromPrime;
    }
  }

  public GuardEraseAndSet(BDD vars, BDD valueBefore, BDD valueAfter) {
    _vars = vars;
    _swap = PAIRING_CACHE.computeIfAbsent(vars, GuardEraseAndSet::computeSwapPairing);
    _forwardRelation = valueBefore.and(valueAfter.replace(_swap));
  }

  private BDD computeBackwardRelation() {
    return _forwardRelation.replace(_swap);
  }

  private static BDDPairing computeSwapPairing(BDD vars) {
    BDDPairing swap = vars.getFactory().makePair();
    BDD tmp = vars;
    while (!tmp.isZero() && !tmp.isOne()) {
      int var = tmp.var();
      swap.set(var, var + 1);
      swap.set(var + 1, var);
      tmp = tmp.high();
    }
    return swap;
  }

  public GuardEraseAndSet(BDD vars, List<ValueBeforeAndAfter> valuesBeforeAndAfter) {
    checkArgument(!vars.isOne() && !vars.isZero(), "No variables to erase. Use Constraint instead");
    checkArgument(
        !valuesBeforeAndAfter.isEmpty(), "GuardEraseAndSet: valuesBeforeAndAfter cannot be empty");
    throw new UnsupportedOperationException();
    //    _vars = vars;
    //    _pairings = PAIRINGS_CACHE.computeIfAbsent(vars, GuardEraseAndSet::computePairings);
    //
    //    _forwardRelation =
    //        _vars
    //            .getFactory()
    //            .orAll(
    //                valuesBeforeAndAfter.stream()
    //                    .map(vba -> vba._before.and(vba._after.replace(_pairings._toPrime)))
    //                    .collect(Collectors.toList()));
    //    _backwardRelation =
    //        _vars
    //            .getFactory()
    //            .orAll(
    //                valuesBeforeAndAfter.stream()
    //                    .map(vba -> vba._before.replace(_pairings._toPrime).and(vba._after))
    //                    .collect(Collectors.toList()));
  }

  private GuardEraseAndSet(BDD vars, BDD forwardRelation, BDDPairing swap) {
    _vars = vars;
    _forwardRelation = forwardRelation;
    _swap = swap;
  }

  private BDD getForwardRelation() {
    return _forwardRelation;
  }

  public GuardEraseAndSet or(GuardEraseAndSet other) {
    checkArgument(_vars.equals(other._vars));
    return new GuardEraseAndSet(_vars, _forwardRelation.or(other._forwardRelation), _swap);
  }

  public static GuardEraseAndSet orAll(List<GuardEraseAndSet> gess) {
    checkArgument(!gess.isEmpty(), "GuardEraseAndSet.orAll requires at least one object");
    if (gess.size() == 1) {
      return Iterables.getOnlyElement(gess);
    }

    GuardEraseAndSet ges = gess.get(0);
    BDD vars = ges.getVars();
    checkArgument(
        gess.stream().map(GuardEraseAndSet::getVars).allMatch(vars::equals),
        "GuardEraseAndSet.orAll: all instances must have the same variables");

    BDDFactory factory = vars.getFactory();
    GuardEraseAndSet result =
        new GuardEraseAndSet(
            vars,
            factory.orAll(
                gess.stream()
                    .map(GuardEraseAndSet::getForwardRelation)
                    .collect(Collectors.toList())),
            ges._swap);
    for (GuardEraseAndSet guardEraseAndSet : gess) {
      guardEraseAndSet._forwardRelation.free();
    }
    return result;
  }

  public Transition constrainBefore(BDD before) {
    BDD forwardRelation = _forwardRelation.and(before);
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    return new GuardEraseAndSet(_vars, forwardRelation, _swap);
  }

  public Transition constrainAfter(BDD after) {
    BDD afterPrime = after.replace(_swap);
    BDD forwardRelation = _forwardRelation.and(afterPrime);
    afterPrime.free();
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    return new GuardEraseAndSet(_vars, forwardRelation, _swap);
  }

  BDD getVars() {
    return _vars;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.applyEx(_forwardRelation, BDDFactory.and, _vars).replaceWith(_swap);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.applyEx(_backwardRelation.get(), BDDFactory.and, _vars).replaceWith(_swap);
  }

  @Override
  public Transition andNotBefore(BDD before) {
    BDD forwardRelation = _forwardRelation.diff(before);
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    _forwardRelation.free();
    return new GuardEraseAndSet(_vars, forwardRelation, _swap);
  }

  @Override
  public Transition andNotAfter(BDD after) {
    BDD afterPrime = after.replace(_swap);
    BDD forwardRelation = _forwardRelation.diff(afterPrime);
    afterPrime.free();
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    return new GuardEraseAndSet(_vars, forwardRelation, _swap);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GuardEraseAndSet)) {
      return false;
    }
    GuardEraseAndSet that = (GuardEraseAndSet) o;
    // backwardRelation, toPrime and fromPrime are all uniquely determined by vars and
    // forwardRelation
    return _vars.equals(that._vars) && _forwardRelation.equals(that._forwardRelation);
  }

  @Override
  public int hashCode() {
    // backwardRelation, toPrime and fromPrime are all uniquely determined by vars and
    // forwardRelation
    return Objects.hashCode(_vars, _forwardRelation);
  }
}
