package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.bddreachability.transition.Transitions.ZERO;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

public final class GuardEraseAndSet implements Transition {
  public Transition andThen(EraseAndSet eas) {
    BDD before = _forwardRelation.project(_vars);
    BDD after = _backwardRelation.project(_vars);
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
  private final BDD _backwardRelation;
  private final BDDPairings _pairings;

  private static final Map<BDD, BDDPairings> PAIRINGS_CACHE = new HashMap<>();

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
    _pairings = PAIRINGS_CACHE.computeIfAbsent(vars, GuardEraseAndSet::computePairings);
    _forwardRelation = valueBefore.and(valueAfter.replace(_pairings._toPrime));
    _backwardRelation = valueAfter.and(valueBefore.replace(_pairings._toPrime));
  }

  private static BDDPairings computePairings(BDD vars) {
    BDDPairing toPrime = vars.getFactory().makePair();
    BDDPairing fromPrime = vars.getFactory().makePair();
    BDD tmp = vars;
    while (!tmp.isZero() && !tmp.isOne()) {
      int var = tmp.var();
      toPrime.set(var, var + 1);
      fromPrime.set(var + 1, var);
      tmp = tmp.high();
    }
    return new BDDPairings(toPrime, fromPrime);
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

  private GuardEraseAndSet(
      BDD vars, BDD forwardRelation, BDD backwardRelation, BDDPairings pairings) {
    _vars = vars;
    _forwardRelation = forwardRelation;
    _backwardRelation = backwardRelation;
    _pairings = pairings;
  }

  private BDD getForwardRelation() {
    return _forwardRelation;
  }

  private BDD getBackwardRelation() {
    return _backwardRelation;
  }

  public GuardEraseAndSet or(GuardEraseAndSet other) {
    checkArgument(_vars.equals(other._vars));
    return new GuardEraseAndSet(
        _vars,
        _forwardRelation.or(other._forwardRelation),
        _backwardRelation.or(other._backwardRelation),
        _pairings);
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
    return new GuardEraseAndSet(
        vars,
        factory.orAll(
            gess.stream().map(GuardEraseAndSet::getForwardRelation).collect(Collectors.toList())),
        factory.orAll(
            gess.stream().map(GuardEraseAndSet::getBackwardRelation).collect(Collectors.toList())),
        ges._pairings);
  }

  public Transition constrainBefore(BDD before) {
    BDD forwardRelation = _forwardRelation.and(before);
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    BDD beforePrime = before.replace(_pairings._toPrime);
    BDD backwardRelation = _backwardRelation.and(beforePrime);
    beforePrime.free();
    return new GuardEraseAndSet(_vars, forwardRelation, backwardRelation, _pairings);
  }

  public Transition constrainAfter(BDD after) {
    BDD backwardRelation = _backwardRelation.and(after);
    if (backwardRelation.isZero()) {
      return ZERO;
    }
    BDD afterPrime = after.replace(_pairings._toPrime);
    BDD forwardRelation = _forwardRelation.and(afterPrime);
    afterPrime.free();
    return new GuardEraseAndSet(_vars, forwardRelation, backwardRelation, _pairings);
  }

  BDD getVars() {
    return _vars;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.applyEx(_forwardRelation, BDDFactory.and, _vars).replaceWith(_pairings._fromPrime);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.applyEx(_backwardRelation, BDDFactory.and, _vars).replaceWith(_pairings._fromPrime);
  }

  @Override
  public Transition andNotBefore(BDD before) {
    BDD forwardRelation = _forwardRelation.diff(before);
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    BDD bddPrime = before.replace(_pairings._toPrime);
    BDD backwardRelation = _backwardRelation.diff(bddPrime);
    bddPrime.free();
    return new GuardEraseAndSet(_vars, forwardRelation, backwardRelation, _pairings);
  }

  @Override
  public Transition andNotAfter(BDD after) {
    BDD backwardRelation = _backwardRelation.diff(after);
    if (backwardRelation.isZero()) {
      return ZERO;
    }
    BDD afterPrime = after.replace(_pairings._toPrime);
    BDD forwardRelation = _forwardRelation.diff(afterPrime);
    afterPrime.free();
    return new GuardEraseAndSet(_vars, forwardRelation, backwardRelation, _pairings);
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
