package org.batfish.dataplane.ibdp;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.tracking.GenericTrackMethodVisitor;
import org.batfish.datamodel.tracking.NegatedTrackMethod;
import org.batfish.datamodel.tracking.StaticTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackInterface;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackMethodEvaluatorProvider;
import org.batfish.datamodel.tracking.TrackMethodReference;
import org.batfish.datamodel.tracking.TrackRoute;
import org.batfish.datamodel.tracking.TrackTrue;

/**
 * Evaluator for a {@link org.batfish.datamodel.tracking.TrackMethod} given knowledge of the
 * contents of a {@link Configuration}, its associated RIBs, and a {@link TracerouteEngine} that can
 * perform reachabilty checks.
 *
 * <p>Delegates to {@link StaticTrackMethodEvaluator} when only the contents of the {@link
 * Configuration} are needed for evaluation.
 */
@ParametersAreNonnullByDefault
public final class DataplaneTrackEvaluator implements GenericTrackMethodVisitor<Boolean> {

  @FunctionalInterface
  public interface DataPlaneTrackMethodEvaluatorProvider extends TrackMethodEvaluatorProvider {

    @Override
    @Nonnull
    DataplaneTrackEvaluator forConfiguration(Configuration c);
  }

  /**
   * Create a provider for {@link DataplaneTrackEvaluator}s given a fixed dataplane and traceroute
   * engine.
   */
  public static @Nonnull DataPlaneTrackMethodEvaluatorProvider createTrackMethodEvaluatorProvider(
      Map<String, Map<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs,
      TracerouteEngine tracerouteEngine) {
    return configuration ->
        new DataplaneTrackEvaluator(
            configuration, ribs.get(configuration.getHostname()), tracerouteEngine);
  }

  public DataplaneTrackEvaluator(
      Configuration configuration,
      Map<String, GenericRib<AnnotatedRoute<AbstractRoute>>> ribByVrf,
      TracerouteEngine tracerouteEngine) {
    _configuration = configuration;
    _staticEvaluator = new StaticTrackMethodEvaluator(configuration);
    _ribByVrf = ribByVrf;
    _tracerouteEngine = tracerouteEngine;
  }

  @Override
  public Boolean visitNegatedTrackMethod(NegatedTrackMethod negatedTrackMethod) {
    return !visit(negatedTrackMethod.getTrackMethod());
  }

  @Override
  public Boolean visitTrackInterface(TrackInterface trackInterface) {
    return _staticEvaluator.visit(trackInterface);
  }

  @Override
  public Boolean visitTrackMethodReference(TrackMethodReference trackMethodReference) {
    TrackMethod method = _configuration.getTrackingGroups().get(trackMethodReference.getId());
    assert method != null;
    return visit(method);
  }

  @Override
  public Boolean visitTrackRoute(TrackRoute trackRoute) {
    Set<AnnotatedRoute<AbstractRoute>> routesForPrefix =
        _ribByVrf.get(trackRoute.getVrf()).getRoutes(trackRoute.getPrefix());
    if (trackRoute.getProtocols().isEmpty()) {
      return !routesForPrefix.isEmpty();
    } else {
      return routesForPrefix.stream()
          .anyMatch(r -> trackRoute.getProtocols().contains(r.getRoute().getProtocol()));
    }
  }

  @Override
  public Boolean visitTrackTrue(TrackTrue trackTrue) {
    return _staticEvaluator.visit(trackTrue);
  }

  private final @Nonnull Configuration _configuration;
  private final @Nonnull Map<String, GenericRib<AnnotatedRoute<AbstractRoute>>> _ribByVrf;
  private final @Nonnull StaticTrackMethodEvaluator _staticEvaluator;

  // TODO: support track reachability
  @SuppressWarnings("unused")
  private final @Nonnull TracerouteEngine _tracerouteEngine;
}
