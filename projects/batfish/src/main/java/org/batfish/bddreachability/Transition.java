package org.batfish.bddreachability;

import net.sf.javabdd.BDD;

/** Bidirectional transition function */
interface Transition {
  BDD transitForward(BDD bdd);

  BDD transitBackward(BDD bdd);
}
