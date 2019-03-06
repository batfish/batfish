package org.batfish.question.bidirectionalreachability;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.specifier.Location;

/** The result of a bidirectional reachability analysis. */
@ParametersAreNonnullByDefault
public class BidirectionalReachabilityResult {
  private final Map<Location, BDD> _startLocationReturnPassFailureBdds;

  private final Map<Location, BDD> _startLocationReturnPassSuccessBdds;

  public BidirectionalReachabilityResult(
      Map<Location, BDD> startLocationReturnPassSuccessBdds,
      Map<Location, BDD> startLocationReturnPassFailureBdds) {
    _startLocationReturnPassSuccessBdds = ImmutableMap.copyOf(startLocationReturnPassSuccessBdds);
    _startLocationReturnPassFailureBdds = ImmutableMap.copyOf(startLocationReturnPassFailureBdds);
  }

  /**
   * @return A {@link BDD} representing the set of packets at each {@link Location} for which the
   *     forward direction can succeed and then the return direction can fail.
   */
  public Map<Location, BDD> getStartLocationReturnPassFailureBdds() {
    return _startLocationReturnPassFailureBdds;
  }

  /**
   * @return A {@link BDD} representing the set of packets at each {@link Location} for which both
   *     directions (can) succeed.
   */
  public Map<Location, BDD> getStartLocationReturnPassSuccessBdds() {
    return _startLocationReturnPassSuccessBdds;
  }
}
