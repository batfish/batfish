package org.batfish.question.searchfilters;

import java.util.Optional;
import javax.annotation.Nullable;

/** The result of a differential reachFilter question. */
public final class DifferentialSearchFilterResult {
  private final @Nullable SearchFilterResult _decreasedResult;
  private final @Nullable SearchFilterResult _increasedResult;

  public DifferentialSearchFilterResult(
      @Nullable SearchFilterResult increasedResult, @Nullable SearchFilterResult decreasedResult) {
    _decreasedResult = decreasedResult;
    _increasedResult = increasedResult;
  }

  /** @return The result for the decreased space. */
  public Optional<SearchFilterResult> getDecreasedResult() {
    return Optional.ofNullable(_decreasedResult);
  }

  /** @return The result for the decreased space. */
  public Optional<SearchFilterResult> getIncreasedResult() {
    return Optional.ofNullable(_increasedResult);
  }
}
