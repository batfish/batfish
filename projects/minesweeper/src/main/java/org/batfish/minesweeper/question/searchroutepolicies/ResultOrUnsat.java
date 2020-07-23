package org.batfish.minesweeper.question.searchroutepolicies;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

/**
 * The result of trying to convert a BDD model from the symbolic route analysis to a concrete route
 * (or portion of a route) -- we either produce the desired result or identify a set of
 * unsatisfiable constraints that prevent us from doing so.
 */
@ParametersAreNonnullByDefault
class ResultOrUnsat<T> {
  // exactly one of these will be nonnull
  @Nullable private T _result;
  @Nullable private BDD _unsatConstraints;

  ResultOrUnsat(T result) {
    _result = result;
  }

  ResultOrUnsat(BDD unsatConstraints) {
    _unsatConstraints = unsatConstraints;
  }

  @Nullable
  T getResult() {
    return _result;
  }

  @Nullable
  BDD getUnsatConstraints() {
    return _unsatConstraints;
  }
}
