package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.Set;
import net.sf.javabdd.BDD;

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
    /* Require that the erased variables exactly match the set variables. We consider it an error to
     * erase a variable that isn't set, or to set a variable that wasn't erased. If we want to set
     * a variable that wasn't erased, or erase a variable without setting it afterwards, we should
     * use a different transition class.
     */
    checkArgument(vars(eraseVars).equals(vars(setValue)));

    _eraseVars = eraseVars;
    _setValue = setValue;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.exist(_eraseVars).and(_setValue);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return bdd.and(_setValue).exist(_eraseVars);
  }
}
