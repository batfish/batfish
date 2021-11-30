package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.mergeComposed;

import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Bidirectional transition function */
public interface Transition {
  Logger LOGGER = LogManager.getLogger(Transition.class);

  BDD transitForward(BDD bdd);

  BDD transitBackward(BDD bdd);

  default @Nullable Transition andNotBefore(BDD bdd) {
    LOGGER.info("class using default andNotBefore: {}", this.getClass().getSimpleName());
    return mergeComposed(constraint(bdd.not()), this);
  }

  default Transition andNotAfter(BDD bdd) {
    return compose(this, constraint(bdd.not()));
  }
}
