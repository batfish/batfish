package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Table;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.tracking.NegatedTrackMethod;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackAll;
import org.batfish.datamodel.tracking.TrackInterface;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackMethodEvaluatorProvider;
import org.batfish.datamodel.tracking.TrackMethodReference;
import org.batfish.datamodel.tracking.TrackReachability;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.datamodel.tracking.TrackTrue;

/**
 * Evaluator for a {@link org.batfish.datamodel.tracking.TrackMethod} given knowledge of the
 * contents of a {@link Configuration} and the results of dataplane-based tracking checks at a given
 * point in time.
 *
 * <p>Delegates to {@link PreDataPlaneTrackMethodEvaluator} when only the contents of the {@link
 * Configuration} are needed for evaluation.
 */
@ParametersAreNonnullByDefault
public final class DataplaneTrackEvaluator implements TrackMethodEvaluator {

  @FunctionalInterface
  public interface DataPlaneTrackMethodEvaluatorProvider extends TrackMethodEvaluatorProvider {
    @Override
    @Nonnull
    DataplaneTrackEvaluator forConfiguration(Configuration c);
  }

  /**
   * Create a provider for {@link DataplaneTrackEvaluator}s given current results for all
   * dataplane-based tracking checks.
   */
  public static @Nonnull DataPlaneTrackMethodEvaluatorProvider createTrackMethodEvaluatorProvider(
      Table<String, TrackReachability, Boolean> trackReachabilityResults,
      Table<String, TrackRoute, Boolean> trackRouteResults) {
    return configuration ->
        new DataplaneTrackEvaluator(
            configuration,
            trackReachabilityResults.row(configuration.getHostname()),
            trackRouteResults.row(configuration.getHostname()));
  }

  @Override
  public Boolean visitNegatedTrackMethod(NegatedTrackMethod negatedTrackMethod) {
    return !visit(negatedTrackMethod.getTrackMethod());
  }

  @Override
  public Boolean visitTrackAll(TrackAll trackAll) {
    for (TrackMethod conjunct : trackAll.getConjuncts()) {
      if (!visit(conjunct)) {
        return Boolean.FALSE;
      }
    }
    return Boolean.TRUE;
  }

  @Override
  public Boolean visitTrackInterface(TrackInterface trackInterface) {
    return _preDataPlaneTrackMethodEvaluator.visit(trackInterface);
  }

  @Override
  public Boolean visitTrackMethodReference(TrackMethodReference trackMethodReference) {
    TrackMethod method = _configuration.getTrackingGroups().get(trackMethodReference.getId());
    assert method != null;
    return visit(method);
  }

  @Override
  public Boolean visitTrackReachability(TrackReachability trackReachability) {
    return _trackReachabilityResults.get(trackReachability);
  }

  @Override
  public Boolean visitTrackRoute(TrackRoute trackRoute) {
    return _trackRouteResults.get(trackRoute);
  }

  @Override
  public Boolean visitTrackTrue(TrackTrue trackTrue) {
    return _preDataPlaneTrackMethodEvaluator.visit(trackTrue);
  }

  private final @Nonnull Configuration _configuration;
  private final @Nonnull Map<TrackReachability, Boolean> _trackReachabilityResults;
  private final @Nonnull Map<TrackRoute, Boolean> _trackRouteResults;
  private final @Nonnull PreDataPlaneTrackMethodEvaluator _preDataPlaneTrackMethodEvaluator;

  @VisibleForTesting
  DataplaneTrackEvaluator(
      Configuration configuration,
      Map<TrackReachability, Boolean> trackReachabilityResults,
      Map<TrackRoute, Boolean> trackRouteResults) {
    _configuration = configuration;
    _preDataPlaneTrackMethodEvaluator = new PreDataPlaneTrackMethodEvaluator(configuration);
    _trackReachabilityResults = trackReachabilityResults;
    _trackRouteResults = trackRouteResults;
  }
}
