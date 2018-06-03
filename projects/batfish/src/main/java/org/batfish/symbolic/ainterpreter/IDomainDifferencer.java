package org.batfish.symbolic.ainterpreter;

public interface IDomainDifferencer<U, T> {

  T difference(U x, T y);
}
