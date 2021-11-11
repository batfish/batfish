package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

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

    public @Nullable ValueBeforeAndAfter addConstraints(BDD addBefore, BDD addAfter) {
      BDD before = _before.and(addBefore);
      if (before.isZero()) {
        return null;
      }
      BDD after = _after.and(addAfter);
      if (after.isZero()) {
        return null;
      }
      return new ValueBeforeAndAfter(before, after);
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
  private final List<ValueBeforeAndAfter> _valuesBeforeAndAfter;

  public GuardEraseAndSet(BDD vars, List<ValueBeforeAndAfter> valuesBeforeAndAfter) {
    checkArgument(!vars.isOne() && !vars.isZero(), "No variables to erase. Use Constraint instead");
    checkArgument(
        !valuesBeforeAndAfter.isEmpty(), "GuardEraseAndSet: valuesBeforeAndAfter cannot be empty");
    assert valuesBeforeAndAfter.stream()
            .allMatch(vba -> vba._before.exist(vars).equals(vba._after.exist(vars)))
        : "GuardEraseAndSet: Constraints on non-erased variables must be preserved before and"
            + " after";
    _vars = vars;
    _valuesBeforeAndAfter = ImmutableList.copyOf(valuesBeforeAndAfter);
  }

  BDD getVars() {
    return _vars;
  }

  List<ValueBeforeAndAfter> getValuesBeforeAndAfter() {
    return _valuesBeforeAndAfter;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    if (_valuesBeforeAndAfter.size() == 1) {
      ValueBeforeAndAfter beforeAndAfter = _valuesBeforeAndAfter.get(0);
      BDD valueBefore = beforeAndAfter._before;
      BDD valueAfter = beforeAndAfter._after;
      return bdd.applyEx(valueBefore, BDDFactory.and, _vars).and(valueAfter);
    }

    List<BDD> valuesBefore = new ArrayList<>(_valuesBeforeAndAfter.size());
    List<BDD> valuesAfter = new ArrayList<>(_valuesBeforeAndAfter.size());
    for (ValueBeforeAndAfter beforeAndAfter : _valuesBeforeAndAfter) {
      if (bdd.andSat(beforeAndAfter._before)) {
        valuesBefore.add(beforeAndAfter._before);
        valuesAfter.add(beforeAndAfter._after);
      }
    }

    BDDFactory factory = _vars.getFactory();
    if (valuesBefore.isEmpty()) {
      return factory.zero();
    }

    BDD valueBefore = factory.orAll(valuesBefore);
    BDD valueAfter = factory.orAll(valuesAfter);

    return bdd.applyEx(valueBefore, BDDFactory.and, _vars).and(valueAfter);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    if (_valuesBeforeAndAfter.size() == 1) {
      ValueBeforeAndAfter beforeAndAfter = _valuesBeforeAndAfter.get(0);
      BDD valueBefore = beforeAndAfter._before;
      BDD valueAfter = beforeAndAfter._after;
      return bdd.applyEx(valueAfter, BDDFactory.and, _vars).and(valueBefore);
    }

    long t = System.currentTimeMillis();
    List<BDD> valuesBefore = new ArrayList<>(_valuesBeforeAndAfter.size());
    List<BDD> valuesAfter = new ArrayList<>(_valuesBeforeAndAfter.size());
    for (ValueBeforeAndAfter beforeAndAfter : _valuesBeforeAndAfter) {
      if (bdd.andSat(beforeAndAfter._after)) {
        valuesBefore.add(beforeAndAfter._before);
        valuesAfter.add(beforeAndAfter._after);
      }
    }

    BDDFactory factory = _vars.getFactory();
    if (valuesBefore.isEmpty()) {
      return factory.zero();
    }

    BDD valueBefore = factory.orAll(valuesBefore);
    BDD valueAfter = factory.orAll(valuesAfter);

    BDD result = bdd.applyEx(valueAfter, BDDFactory.and, _vars).and(valueBefore);
    t = System.currentTimeMillis() - t;
    System.err.println(
        String.format(
            "GuardEraseAndSet: evaluated %d of %d rules in %d ms",
            valuesBefore.size(), _valuesBeforeAndAfter.size(), t));
    return result;
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
    return _vars.equals(that._vars) && _valuesBeforeAndAfter.equals(that._valuesBeforeAndAfter);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_vars, _valuesBeforeAndAfter);
  }
}
