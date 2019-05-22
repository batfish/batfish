package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

/** A transition that erases a variable and then constrains it to have a new value. */
public final class EraseAndSet implements Transition {
  private final BDD _eraseVars;
  private final BDD _setValue;

  private static void vars(BDD bdd, Set<Integer> varSet) {
    if (bdd.isZero() || bdd.isOne()) {
      return;
    }
    varSet.add(bdd.var());
    vars(bdd.high(), varSet);
    vars(bdd.low(), varSet);
  }

  private static Set<Integer> vars(BDD bdd) {
    Set<Integer> varSet = new HashSet<>();
    vars(bdd, varSet);
    return varSet;
  }

  EraseAndSet(BDD eraseVars, BDD setValue) {
    Set<Integer> vars = vars(eraseVars);
    checkArgument(!vars.isEmpty(), "No variables to erase");

    checkArgument(!setValue.isOne(), "Value is one (always true). Use Identity instead");

    /* Require that the erased variables include the set variables. We consider it an error to
     * set a variable that wasn't erased. If we want to set a variable that wasn't erased, use a
     * different transition class.
     */
    checkArgument(vars.containsAll(vars(setValue)));

    _eraseVars = eraseVars;
    _setValue = setValue;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.exist(_eraseVars).and(_setValue);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.applyEx(_setValue, BDDFactory.and, _eraseVars);
  }
}
