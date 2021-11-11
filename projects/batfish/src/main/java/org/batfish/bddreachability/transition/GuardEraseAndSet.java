package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.bddreachability.transition.Transitions.ZERO;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

public final class GuardEraseAndSet implements Transition {
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
  private final BDDPairing _toPrime;
  private final BDDPairing _fromPrime;

  public GuardEraseAndSet(BDD vars, List<ValueBeforeAndAfter> valuesBeforeAndAfter) {
    checkArgument(!vars.isOne() && !vars.isZero(), "No variables to erase. Use Constraint instead");
    checkArgument(
        !valuesBeforeAndAfter.isEmpty(), "GuardEraseAndSet: valuesBeforeAndAfter cannot be empty");
    _vars = vars;
    _toPrime = vars.getFactory().makePair();
    _fromPrime = vars.getFactory().makePair();

    BDD tmp = vars;
    while (!tmp.isZero() && !tmp.isOne()) {
      int var = tmp.var();
      _toPrime.set(var, var + 1);
      _fromPrime.set(var + 1, var);
      tmp = tmp.high();
    }

    _forwardRelation =
        _vars
            .getFactory()
            .orAll(
                valuesBeforeAndAfter.stream()
                    .map(vba -> vba._before.and(vba._after.replace(_toPrime)))
                    .collect(Collectors.toList()));
    _backwardRelation =
        _vars
            .getFactory()
            .orAll(
                valuesBeforeAndAfter.stream()
                    .map(vba -> vba._before.replace(_toPrime).and(vba._after))
                    .collect(Collectors.toList()));
  }

  private GuardEraseAndSet(
      BDD vars,
      BDD forwardRelation,
      BDD backwardRelation,
      BDDPairing toPrime,
      BDDPairing fromPrime) {
    _vars = vars;
    _forwardRelation = forwardRelation;
    _backwardRelation = backwardRelation;
    _toPrime = toPrime;
    _fromPrime = fromPrime;
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
        _toPrime,
        _fromPrime);
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
        ges._toPrime,
        ges._fromPrime);
  }

  public Transition constrainBefore(BDD before) {
    BDD forwardRelation = _forwardRelation.and(before);
    if (forwardRelation.isZero()) {
      return ZERO;
    }
    return new GuardEraseAndSet(
        _vars,
        forwardRelation,
        _backwardRelation.and(before.replace(_toPrime)),
        _toPrime,
        _fromPrime);
  }

  public GuardEraseAndSet constrainAfter(BDD after) {
    return new GuardEraseAndSet(
        _vars,
        _forwardRelation.and(after.replace(_toPrime)),
        _backwardRelation.and(after),
        _toPrime,
        _fromPrime);
  }

  BDD getVars() {
    return _vars;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.applyEx(_forwardRelation, BDDFactory.and, _vars).replace(_fromPrime);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.applyEx(_backwardRelation, BDDFactory.and, _vars).replace(_fromPrime);
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
