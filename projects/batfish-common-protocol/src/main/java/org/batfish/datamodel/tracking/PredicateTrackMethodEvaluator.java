package org.batfish.datamodel.tracking;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/**
 * Evaluates a {@link TrackMethod} as a predicate. Visiting the method returns {@code true} if the
 * {@link TrackMethod} would be triggered and execute associated {@link TrackAction}s and {@code
 * false} otherwise.
 */
public class PredicateTrackMethodEvaluator implements GenericTrackMethodVisitor<Boolean> {
  public PredicateTrackMethodEvaluator(Configuration configuration) {
    _configuration = configuration;
  }

  @Override
  public Boolean visitTrackInterface(TrackInterface trackInterface) {
    Interface trackedInterface =
        _configuration.getAllInterfaces().get(trackInterface.getTrackedInterface());
    if (trackedInterface == null) {
      // Assume an undefined interface cannot trigger this track methood
      return false;
    }
    return !trackedInterface.getActive() || trackedInterface.getBlacklisted();
  }

  private final Configuration _configuration;
}
