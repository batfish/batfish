package org.batfish.question.specifiers;

import java.util.Optional;
import org.batfish.datamodel.PathConstraints;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;

/** Utility class to convert {@link PathConstraintsInput} to internal representation. */
public final class PathConstraintsUtil {
  private PathConstraintsUtil() {}

  /**
   * Converts {@link PathConstraintsInput} to {@link PathConstraints} using {@link
   * FlexibleNodeSpecifierFactory} for {@link PathConstraintsInput#getTransitLocations()} {@link
   * PathConstraintsInput#getForbiddenLocations()}, and {@link
   * PathConstraintsInput#getEndLocation()}. If {@link PathConstraintsInput#getTransitLocations()}
   * {@link PathConstraintsInput#getForbiddenLocations()} are not specified, default to {@link
   * NoNodesNodeSpecifier} instead of {@link FlexibleNodeSpecifierFactory the factory's} usual
   * default {@link org.batfish.specifier.AllNodesNodeSpecifier}.
   */
  public static PathConstraints createPathConstraints(PathConstraintsInput input) {
    FlexibleNodeSpecifierFactory nodeSpecifierFactory = new FlexibleNodeSpecifierFactory();
    /* FlexibleNodeSpecifierFactory defaults to all nodes. We want a different default for transit
     * nodes.
     */
    NodeSpecifier forbiddenTransitNodes =
        Optional.ofNullable(input.getForbiddenLocations())
            .map(nodeSpecifierFactory::buildNodeSpecifier)
            .orElse(NoNodesNodeSpecifier.INSTANCE);
    NodeSpecifier requiredTransitNodes =
        Optional.ofNullable(input.getTransitLocations())
            .map(nodeSpecifierFactory::buildNodeSpecifier)
            .orElse(NoNodesNodeSpecifier.INSTANCE);
    return PathConstraints.builder()
        .withStartLocation(
            new FlexibleLocationSpecifierFactory().buildLocationSpecifier(input.getStartLocation()))
        .withEndLocation(nodeSpecifierFactory.buildNodeSpecifier(input.getEndLocation()))
        .avoid(forbiddenTransitNodes)
        .through(requiredTransitNodes)
        .build();
  }
}
