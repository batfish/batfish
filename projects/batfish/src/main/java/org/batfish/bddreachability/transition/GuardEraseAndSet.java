package org.batfish.bddreachability.transition;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public final class GuardEraseAndSet implements Transition {
  public static final class ValueBeforeAndAfter {
    private final BDD _before;
    private final BDD _after;

    public ValueBeforeAndAfter(BDD before, BDD after) {
      _before = before;
      _after = after;
    }
  }

  private final BDD _vars;
  private final List<ValueBeforeAndAfter> _valuesBeforeAndAfter;

  public GuardEraseAndSet(BDD vars, List<ValueBeforeAndAfter> valuesBeforeAndAfter) {
    _vars = vars;
    _valuesBeforeAndAfter = ImmutableList.copyOf(valuesBeforeAndAfter);
  }

  public BDD getVars() {
    return _vars;
  }

  public List<ValueBeforeAndAfter> getValuesBeforeAndAfter() {
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
}
