package org.batfish.question.searchfilters;

import java.util.Optional;
import javax.annotation.Nullable;

/** The result of a differential reachFilter question. */
public final class DifferentialSearchFiltersResult {
  private final @Nullable SearchFiltersResult _decreasedResult;
  private final @Nullable SearchFiltersResult _increasedResult;

  public DifferentialSearchFiltersResult(
      @Nullable SearchFiltersResult increasedResult,
      @Nullable SearchFiltersResult decreasedResult) {
    _decreasedResult = decreasedResult;
    _increasedResult = increasedResult;
  }

  /** @return The result for the decreased space. */
  public Optional<SearchFiltersResult> getDecreasedResult() {
    return Optional.ofNullable(_decreasedResult);
  }

  /** @return The result for the decreased space. */
  public Optional<SearchFiltersResult> getIncreasedResult() {
    return Optional.ofNullable(_increasedResult);
  }
}
