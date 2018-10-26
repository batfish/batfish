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
        Optional.ofNullable(input.getForbiddenLocations())
            .map(nodeSpecifierFactory::buildNodeSpecifier)
            .orElse(NoNodesNodeSpecifier.INSTANCE);
    nodeSpecifierFactory.buildNodeSpecifier(input.getTransitLocations());
    return PathConstraints.builder()
        .withStartLocation(
            new FlexibleLocationSpecifierFactory().buildLocationSpecifier(input.getStartLocation()))
        .withEndLocation(nodeSpecifierFactory.buildNodeSpecifier(input.getEndLocation()))
        .avoid(forbiddenTransitNodes)
        .through(requiredTransitNodes)
        .build();
  }
}
