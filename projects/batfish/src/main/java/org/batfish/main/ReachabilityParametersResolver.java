package org.batfish.main;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.specifier.SpecifierUtils.resolveActiveLocations;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;

/**
 * Resolve a {@link ReachabilityParameters} and return a {@link ResolvedReachabilityParameters}
 * object. This involves resolving Location, Node, and IpSpace specifiers. All validation of user
 * input is done here.
 */
public final class ReachabilityParametersResolver {
  private final SpecifierContext _context;
  private final ReachabilityParameters _params;
  private final IpSpaceRepresentative _ipSpaceRepresentative;

  @VisibleForTesting
  ReachabilityParametersResolver(
      IBatfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot) {
    _params = params;
    _context = batfish.specifierContext(snapshot);
    _ipSpaceRepresentative = new IpSpaceRepresentative();
  }

  public static ResolvedReachabilityParameters resolveReachabilityParameters(
      IBatfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot)
      throws InvalidReachabilityParametersException {

    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(batfish, params, snapshot);
    SpecifierContext context = resolver._context;

    // validate actions
    SortedSet<FlowDisposition> actions = params.getActions();
    if (actions.isEmpty()) {
      throw new InvalidReachabilityParametersException("No actions");
    }

    // resolve and validate final nodes
    Set<String> finalNodes = params.getFinalNodesSpecifier().resolve(context);
    if (finalNodes.isEmpty()) {
      throw new InvalidReachabilityParametersException("No final nodes");
    }

    // resolve and validate transit nodes
    Set<String> forbiddenTransitNodes = params.getForbiddenTransitNodesSpecifier().resolve(context);
    Set<String> requiredTransitNodes = params.getRequiredTransitNodesSpecifier().resolve(context);
    if (!requiredTransitNodes.isEmpty()
        && forbiddenTransitNodes.containsAll(requiredTransitNodes)) {
      throw new InvalidReachabilityParametersException(
          "All required transit nodes are also forbidden");
    }
    return ResolvedReachabilityParameters.builder()
        .setActions(actions)
        .setFinalNodes(finalNodes)
        .setForbiddenTransitNodes(forbiddenTransitNodes)
        .setHeaderSpace(resolver.resolveHeaderSpace())
        .setIgnoreFilters(params.getIgnoreFilters())
        .setSourceIpSpaceAssignment(resolver.resolveSourceIpSpaceAssignment())
        .setSrcNatted(params.getSrcNatted())
        .setRequiredTransitNodes(requiredTransitNodes)
        .build();
  }

  private AclLineMatchExpr resolveHeaderSpace() throws InvalidReachabilityParametersException {
    AclLineMatchExpr expr =
        and(
            firstNonNull(_params.getHeaderSpace(), AclLineMatchExprs.TRUE),
            matchDst(resolveDestinationIpSpace()));
    return _params.getInvertSearch() ? AclLineMatchExprs.not(expr) : expr;
  }

  @VisibleForTesting
  IpSpace resolveDestinationIpSpace() throws InvalidReachabilityParametersException {
    IpSpace destinationIpSpace =
        AclIpSpace.union(
            _params
                .getDestinationIpSpaceSpecifier()
                .resolve(ImmutableSet.of(), _context)
                .getEntries()
                .stream()
                .map(Entry::getIpSpace)
                .collect(ImmutableList.toImmutableList()));

    /*
     * Make sure the destinationIpSpace is non-empty. Otherwise, we'll get no results and no
     * explanation why.
     */
    if (!_ipSpaceRepresentative.getRepresentative(destinationIpSpace).isPresent()) {
      throw new InvalidReachabilityParametersException("Empty destination IpSpace");
    }
    return destinationIpSpace;
  }

  @VisibleForTesting
  IpSpaceAssignment resolveSourceIpSpaceAssignment() throws InvalidReachabilityParametersException {
    Set<Location> sourceLocations =
        resolveActiveLocations(_params.getSourceLocationSpecifier(), _context);
    if (sourceLocations.isEmpty()) {
      throw new InvalidReachabilityParametersException("No matching source locations");
    }

    // resolve the IpSpaceSpecifier, and filter out entries with empty IpSpaces
    IpSpaceAssignment sourceIpSpaceAssignment =
        IpSpaceAssignment.of(
            _params
                .getSourceIpSpaceSpecifier()
                .resolve(sourceLocations, _context)
                .getEntries()
                .stream()
                .filter(
                    entry ->
                        _ipSpaceRepresentative.getRepresentative(entry.getIpSpace()).isPresent())
                .collect(ImmutableList.toImmutableList()));

    if (sourceIpSpaceAssignment.getEntries().isEmpty()) {
      throw new InvalidReachabilityParametersException("All sources have empty source IpSpaces");
    }
    return sourceIpSpaceAssignment;
  }
}
