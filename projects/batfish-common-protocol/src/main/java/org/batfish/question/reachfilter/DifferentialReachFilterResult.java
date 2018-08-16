package org.batfish.question.reachfilter;

import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

/** The result of a differential reachFilter question. */
public final class DifferentialReachFilterResult {
  private final @Nullable Flow _decreasedFlow;
  private final @Nullable Flow _increasedFlow;

  public DifferentialReachFilterResult(@Nullable Flow increasedFlow, @Nullable Flow decreasedFlow) {
    _decreasedFlow = decreasedFlow;
    _increasedFlow = increasedFlow;
  }

  public Optional<Flow> getDecreasedFlow() {
    return Optional.ofNullable(_decreasedFlow);
  }

  public Optional<Flow> getIncreasedFlow() {
    return Optional.ofNullable(_increasedFlow);
  }
}
