package org.batfish.datamodel.tracking;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;

/** Provides {@link TrackMethod} evaluator for an input configuration. */
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface TrackMethodEvaluatorProvider {
  GenericTrackMethodVisitor<Boolean> getProvider(Configuration configuration);
}
