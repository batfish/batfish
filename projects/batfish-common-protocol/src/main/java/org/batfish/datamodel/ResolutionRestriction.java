package org.batfish.datamodel;

import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * A predicate on {@link org.batfish.datamodel.AbstractRouteDecorator} whose members may be used for
 * next-hop-ip resolution.
 */
public interface ResolutionRestriction<R extends AbstractRouteDecorator> extends Predicate<R> {

  ResolutionRestriction<AbstractRouteDecorator> ALWAYS_TRUE = r -> true;

  @SuppressWarnings("unchecked")
  static @Nonnull <R extends AbstractRouteDecorator> ResolutionRestriction<R> alwaysTrue() {
    return (ResolutionRestriction<R>) ALWAYS_TRUE;
  }
}
