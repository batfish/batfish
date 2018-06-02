package org.batfish.symbolic.ainterpreter;

import net.sf.javabdd.BDD;

public interface IDomainMixable<U, T> {

  BDD fibDifference(BDD x, BDD y);

  AbstractRib<T> ribDifference(AbstractRib<U> x, AbstractRib<T> y);
}
