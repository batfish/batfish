package org.batfish.question.specifiers;

import org.batfish.datamodel.PathConstraints;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Utility class to convert {@link PathConstraintsInput} to internal representation. */
public final class PathConstraintsUtil {
  private PathConstraintsUtil() {}

  /**
   * Converts {@link PathConstraintsInput} to {@link PathConstraints} for {@link
   * PathConstraintsInput#getTransitLocations()} {@link
   * PathConstraintsInput#getForbiddenLocations()}, and {@link
   * PathConstraintsInput#getEndLocation()}. If {@link PathConstraintsInput#getTransitLocations()}
   * {@link PathConstraintsInput#getForbiddenLocations()} are not specified, default to {@link
   * NoNodesNodeSpecifier}.
   */
  public static PathConstraints createPathConstraints(PathConstraintsInput input) {
    NodeSpecifier forbiddenTransitNodes =
        SpecifierFactories.getNodeSpecifierOrDefault(
            input.getForbiddenLocations(), NoNodesNodeSpecifier.INSTANCE);
    NodeSpecifier requiredTransitNodes =
        SpecifierFactories.getNodeSpecifierOrDefault(
            input.getTransitLocations(), NoNodesNodeSpecifier.INSTANCE);
    return PathConstraints.builder()
        .withStartLocation(
            SpecifierFactories.getLocationSpecifierOrDefault(
                input.getStartLocation(), LocationSpecifier.ALL_LOCATIONS))
        .withEndLocation(
            SpecifierFactories.getNodeSpecifierOrDefault(
                input.getEndLocation(), AllNodesNodeSpecifier.INSTANCE))
        .avoid(forbiddenTransitNodes)
        .through(requiredTransitNodes)
        .build();
  }
}
