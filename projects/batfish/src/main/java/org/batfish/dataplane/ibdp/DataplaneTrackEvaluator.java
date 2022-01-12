package org.batfish.dataplane.ibdp;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericRibReadOnly;
import org.batfish.datamodel.tracking.GenericTrackMethodVisitor;
import org.batfish.datamodel.tracking.StaticTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackInterface;
import org.batfish.datamodel.tracking.TrackRoute;

/**
 * Evaluator for a {@link org.batfish.datamodel.tracking.TrackMethod} given knowledge of the routes
 * in a main RIB, and the contents of the associated {@link Configuration}.
 *
 * <p>Delegates to {@link StaticTrackMethodEvaluator} when only the contents of the {@link
 * Configuration} are needed for evaluation.
 */
@ParametersAreNonnullByDefault
public class DataplaneTrackEvaluator implements GenericTrackMethodVisitor<Boolean> {

  public DataplaneTrackEvaluator(
      Configuration configuration, GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> mainRib) {
    _staticEvaluator = new StaticTrackMethodEvaluator(configuration);
    _mainRib = mainRib;
  }

  @Override
  public Boolean visitTrackInterface(TrackInterface trackInterface) {
    return _staticEvaluator.visit(trackInterface);
  }

  @Override
  public Boolean visitTrackRoute(TrackRoute trackRoute) {
    Set<AnnotatedRoute<AbstractRoute>> routesForPrefix = _mainRib.getRoutes(trackRoute.getPrefix());
    if (trackRoute.getProtocols().isEmpty()) {
      return !routesForPrefix.isEmpty();
    } else {
      return routesForPrefix.stream()
          .anyMatch(r -> trackRoute.getProtocols().contains(r.getRoute().getProtocol()));
    }
  }

  private final @Nonnull GenericRibReadOnly<AnnotatedRoute<AbstractRoute>> _mainRib;
  private final @Nonnull StaticTrackMethodEvaluator _staticEvaluator;
}
