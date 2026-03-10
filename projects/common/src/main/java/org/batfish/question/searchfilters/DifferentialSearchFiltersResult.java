package org.batfish.question.searchfilters;

import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

/** The result of a differential reachFilter question. */
public final class DifferentialSearchFiltersResult {
  private final @Nullable Flow _decreasedFlow;
  private final @Nullable Flow _increasedFlow;

  public DifferentialSearchFiltersResult(
      @Nullable Flow increasedFlow, @Nullable Flow decreasedFlow) {
    _decreasedFlow = decreasedFlow;
    _increasedFlow = increasedFlow;
  }

  /**
   * @return The result for the decreased space.
   */
  public Optional<Flow> getDecreasedFlow() {
    return Optional.ofNullable(_decreasedFlow);
  }

  /**
   * @return The result for the decreased space.
   */
  public Optional<Flow> getIncreasedFlow() {
    return Optional.ofNullable(_increasedFlow);
  }
}
