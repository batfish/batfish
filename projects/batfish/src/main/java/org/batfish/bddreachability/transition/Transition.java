package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/** Bidirectional transition function */
public interface Transition {
  BDD transitForward(BDD bdd);

  BDD transitBackward(BDD bdd);
}
