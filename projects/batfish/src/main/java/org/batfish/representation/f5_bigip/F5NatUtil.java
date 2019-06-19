package org.batfish.representation.f5_bigip;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.transformation.Transformation;

/** Utility class providing general-purpose NAT functionality used in F5 conversion */
@ParametersAreNonnullByDefault
public final class F5NatUtil {

  /**
   * Produces a {@link Transformation} that chains the provided transformations orElse-wise. The
   * input transformations should appear in reverse order of precedence.
   */
  public static @Nullable Transformation orElseChain(
      Iterable<SimpleTransformation> simpleTransformations) {
    Transformation current = null;
    for (SimpleTransformation earlier : simpleTransformations) {
      current =
          Transformation.when(earlier.getGuard())
              .apply(earlier.getStep())
              .setOrElse(current)
              .build();
    }
    return current;
  }

  private F5NatUtil() {}
}
