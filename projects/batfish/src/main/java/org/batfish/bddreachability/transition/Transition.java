package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;

import net.sf.javabdd.BDD;

/** Bidirectional transition function */
public interface Transition {
  BDD transitForward(BDD bdd);

  BDD transitBackward(BDD bdd);

  default Transition andNotBefore(BDD bdd) {
    return compose(constraint(bdd.not()), this);
  }

  default Transition andNotAfter(BDD bdd) {
    return compose(this, constraint(bdd.not()));
  }
}
