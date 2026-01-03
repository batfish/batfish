package org.batfish.question.differentialreachability;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;

/**
 * The result of {@link IBatfish#bddDifferentialReachability} -- sets of flows demonstrating
 * increased and reduced reachability.
 */
public final class DifferentialReachabilityResult {
  private final Set<Flow> _increasedReachabilityFlows;
  private final Set<Flow> _decreasedReachabilityFlows;

  public DifferentialReachabilityResult(
      Set<Flow> increasedReachabilityFlows, Set<Flow> decreasedReachabilityFlows) {
    _increasedReachabilityFlows = ImmutableSet.copyOf(increasedReachabilityFlows);
    _decreasedReachabilityFlows = ImmutableSet.copyOf(decreasedReachabilityFlows);
  }

  public Set<Flow> getIncreasedReachabilityFlows() {
    return _increasedReachabilityFlows;
  }

  public Set<Flow> getDecreasedReachabilityFlows() {
    return _decreasedReachabilityFlows;
  }
}
