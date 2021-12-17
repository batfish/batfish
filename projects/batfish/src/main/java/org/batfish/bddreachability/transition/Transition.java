package org.batfish.bddreachability.transition;

import net.sf.javabdd.BDD;

/**
 * Bidirectional transition function.
 *
 * <p>Implementation note: {@link Transition} implementations are expected to free any intermediate
 * BDDs they produce, that is, all BDDs other than the input and output. The returned {@link BDD}
 * may be {@link BDD#free freed} by downstream code, and it is the caller's responsibility to free
 * the input BDD.
 */
public interface Transition {
  BDD transitForward(BDD bdd);

  BDD transitBackward(BDD bdd);
}
