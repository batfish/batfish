package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;

/** A transition that erases a variable and then constrains it to have a new value. */
public final class EraseAndSet implements Transition {
  private final BDD _eraseVars;
  private final BDD _setValue;

  EraseAndSet(BDD eraseVars, BDD setValue) {
    checkArgument((!eraseVars.isOne() && !eraseVars.isZero()), "No variables to erase");

    checkArgument(!setValue.isOne(), "Value is one (always true). Use Identity instead");

    /* Require that the erased variables include the set variables. We consider it an error to
     * set a variable that wasn't erased. If we want to set a variable that wasn't erased, use a
     * different transition class.
     */
    checkArgument(setValue.exist(eraseVars).isOne(), "Erasing the value should result in one");

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
