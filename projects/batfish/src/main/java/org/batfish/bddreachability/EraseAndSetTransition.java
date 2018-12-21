package org.batfish.bddreachability;

import net.sf.javabdd.BDD;

/** A transition that erases a variable and then constrains it to have a new value. */
class EraseAndSetTransition implements Transition {
  private final BDD _eraseVars;
  private final BDD _setValue;

  EraseAndSetTransition(BDD eraseVars, BDD setValue) {
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
