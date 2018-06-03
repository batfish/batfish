package org.batfish.symbolic.ainterpreter;

public interface IDomainMixable<U, T> {

  AbstractRib<T> difference(AbstractRib<U> x, AbstractRib<T> y);

}
