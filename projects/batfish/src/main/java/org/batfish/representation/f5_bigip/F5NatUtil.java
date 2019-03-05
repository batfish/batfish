package org.batfish.representation.f5_bigip;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nullable;
import org.batfish.datamodel.transformation.Transformation;

/** Utility class providing general-purpose NAT functionality used in F5 conversion */
public final class F5NatUtil {

  /**
   * Adds nested orElse {@link Transformation}s of {@code input} to {@code outputList}, and then add
   * {@code input}.
   */
  @VisibleForTesting
  static void addOrElses(ImmutableList.Builder<Transformation> outputList, Transformation input) {
    if (input == null) {
      return;
    }
    addOrElses(outputList, input.getOrElse());
    outputList.add(input);
  }

  /**
   * Produces a transformation that chains the provided transformations orElse-wise. The input
   * transformations should appear in reverse order of precedence.
   */
  public static @Nullable Transformation orElseChain(Collection<Transformation> transformations) {
    if (transformations.isEmpty()) {
      return null;
    }
    if (transformations.size() == 1) {
      return transformations.iterator().next();
    }
    ImmutableList.Builder<Transformation> flatTransformations = ImmutableList.builder();
    for (Transformation transformation : transformations) {
      addOrElses(flatTransformations, transformation);
    }
    Transformation headOfTail = null;
    for (Transformation earlier : flatTransformations.build()) {
      headOfTail =
          Transformation.when(earlier.getGuard())
              .apply(earlier.getTransformationSteps())
              .setAndThen(earlier.getAndThen())
              .setOrElse(headOfTail)
              .build();
    }
    return headOfTail;
  }

  private F5NatUtil() {}
}
