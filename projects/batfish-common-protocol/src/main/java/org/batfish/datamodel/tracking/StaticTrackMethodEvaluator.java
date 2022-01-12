package org.batfish.datamodel.tracking;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/**
 * Evaluator for {@link TrackMethod}s whose value may be determined solely from the contents of a
 * {@link Configuration}. Visiting the method returns {@code true} if the {@link TrackMethod} would
 * be triggered and execute associated {@link TrackAction}s and {@code false} otherwise.
 *
 * <p>Note that caller is responsible for negating the result of evaluation if {@link
 * TrackAction#getNegateTrack()} is {@code true} for an associated action.
 *
 * <p>For {@link TrackMethod}s requiring data plane information for evaluation, throws {@link
 * UnsupportedOperationException}.
 */
@ParametersAreNonnullByDefault
public class StaticTrackMethodEvaluator implements GenericTrackMethodVisitor<Boolean> {
  public StaticTrackMethodEvaluator(Configuration configuration) {
    _configuration = configuration;
  }

  @Override
  public Boolean visitTrackInterface(TrackInterface trackInterface) {
    Interface trackedInterface =
        _configuration.getAllInterfaces().get(trackInterface.getTrackedInterface());
    if (trackedInterface == null) {
      // Assume an undefined interface cannot trigger this track method
      // TODO: Don't convert TrackInterface for undefined interface.
      //       Instead, use a singleton true TrackMethod or false TrackMethod as appropriate for
      //       vendor.
      return false;
    }
    return trackedInterface.getActive() && !trackedInterface.getBlacklisted();
  }

  @Override
  public Boolean visitTrackRoute(TrackRoute trackRoute) {
    throw new UnsupportedOperationException("Unsupported method for HSRP priority evaluation");
  }

  @Nonnull private final Configuration _configuration;
}
