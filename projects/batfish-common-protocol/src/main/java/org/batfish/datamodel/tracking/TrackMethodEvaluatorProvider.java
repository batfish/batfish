package org.batfish.datamodel.tracking;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;

/** Provides a {@link TrackMethod} evaluator for an input {@link Configuration}. */
@FunctionalInterface
@ParametersAreNonnullByDefault
public interface TrackMethodEvaluatorProvider {
  @Nonnull
  TrackMethodEvaluator forConfiguration(Configuration c);
}
