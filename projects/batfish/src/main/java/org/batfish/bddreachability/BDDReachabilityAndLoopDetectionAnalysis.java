package org.batfish.bddreachability;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.IngressLocation;

/**
 * An analysis that does both reachability and loop detection. Simply wraps a {@link
 * BDDReachabilityAnalysis} and a {@link BDDLoopDetectionAnalysis}. The point of this class is to
 * reduce repeated work in constructing the analyses.
 */
public class BDDReachabilityAndLoopDetectionAnalysis {
  private final BDDReachabilityAnalysis _reachabilityAnalysis;
  private final BDDLoopDetectionAnalysis _loopDetectionAnalysis;

  public BDDReachabilityAndLoopDetectionAnalysis(
      BDDReachabilityAnalysis reachabilityAnalysis,
      BDDLoopDetectionAnalysis loopDetectionAnalysis) {
    _reachabilityAnalysis = reachabilityAnalysis;
    _loopDetectionAnalysis = loopDetectionAnalysis;
  }

  public Map<IngressLocation, BDD> getIngressLocationBdds() {
    return Stream.concat(
            _reachabilityAnalysis.getIngressLocationReachableBDDs().entrySet().stream(),
            _loopDetectionAnalysis.detectLoops().entrySet().stream())
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue, BDD::or));
  }
}
