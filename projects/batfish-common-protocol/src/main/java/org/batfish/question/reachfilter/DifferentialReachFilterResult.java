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

  /**
   * @return An example {@link Flow} that is permitted by the base ACL but not the delta ACL, if any
   *     exists.
   */
  public Optional<Flow> getDecreasedFlow() {
    return Optional.ofNullable(_decreasedFlow);
  }

  /**
   * @return An example {@link Flow} that is permitted by the delta ACL but not the base ACL, if any
   *     exists.
   */
  public Optional<Flow> getIncreasedFlow() {
    return Optional.ofNullable(_increasedFlow);
  }
}
