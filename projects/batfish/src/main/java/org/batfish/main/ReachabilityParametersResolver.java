package org.batfish.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.datamodel.visitors.IpSpaceRepresentativeImpl;
import org.batfish.main.Batfish.CompressDataPlaneResult;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.Location;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierContextImpl;

/**
 * Resolve a {@link ReachabilityParameters} and return a {@link ResolvedReachabilityParameters}
 * object. This involves getting the right configs and dataplane, and resolving Location, Node, and
 * IpSpace specifiers. All validation of user input is done here.
 */
final class ReachabilityParametersResolver {
  private final Batfish _batfish;

  private final SpecifierContextImpl _context;

  private Map<String, Configuration> _configs;

  private DataPlane _dataPlane;

  private final ReachabilityParameters _params;

  private final NetworkSnapshot _snapshot;

  private final IpSpaceRepresentativeImpl _ipSpaceRepresentative;

  @VisibleForTesting
  ReachabilityParametersResolver(
      Batfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot) {
    _batfish = batfish;
    _params = params;
    _snapshot = snapshot;
    initConfigsAndDataPlane();
    _context = new SpecifierContextImpl(batfish, _configs);
    _ipSpaceRepresentative = new IpSpaceRepresentativeImpl();
  }

  public static ResolvedReachabilityParameters resolveReachabilityParameters(
      Batfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot)
      throws InvalidReachabilityParametersException {

    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(batfish, params, snapshot);

    // validate actions
    SortedSet<ForwardingAction> actions = params.getActions();
    if (actions.isEmpty()) {
      throw new InvalidReachabilityParametersException("No actions");
    }

    // resolve and validate final nodes
    Set<String> finalNodes = resolver.resolveNodes("finalNodes", params.getFinalNodesSpecifier());
    if (finalNodes.isEmpty()) {
      throw new InvalidReachabilityParametersException("No final nodes");
    }

    // resolve and validate transit nodes
    Set<String> forbiddenTransitNodes =
        resolver.resolveNodes("forbiddenTransitNodes", params.getForbiddenTransitNodesSpecifier());
    Set<String> requiredTransitNodes =
        resolver.resolveNodes("requiredTransitNodes", params.getRequiredTransitNodesSpecifier());
    if (!requiredTransitNodes.isEmpty()
        && forbiddenTransitNodes.containsAll(requiredTransitNodes)) {
      throw new InvalidReachabilityParametersException(
          "All required transit nodes are also forbidden");
    }
    return ResolvedReachabilityParameters.builder()
        .setActions(actions)
        .setConfigurations(resolver._configs)
        .setDataPlane(resolver._dataPlane)
        .setFinalNodes(finalNodes)
        .setForbiddenTransitNodes(forbiddenTransitNodes)
        .setHeaderSpace(resolver.resolveHeaderSpace())
        .setMaxChunkSize(params.getMaxChunkSize())
        .setSourceIpSpaceAssignment(resolver.resolveSourceIpSpaceAssignment())
        .setSrcNatted(params.getSrcNatted())
        .setSpecialize(params.getSpecialize())
        .setRequiredTransitNodes(requiredTransitNodes)
        .setUseCompression(params.getUseCompression())
        .build();
  }

  private HeaderSpace resolveHeaderSpace() throws InvalidReachabilityParametersException {
    IpSpace destinationIpSpace = resolveDestinationIpSpace();

    HeaderSpace headerSpace = _params.getHeaderSpace();
    if (headerSpace == null) {
      headerSpace = HeaderSpace.builder().setDstIps(destinationIpSpace).build();
    } else {
      headerSpace.setDstIps(destinationIpSpace);
    }
    return headerSpace;
  }

  @VisibleForTesting
  Set<String> resolveNodes(@Nonnull String name, @Nullable NodeSpecifier spec)
      throws InvalidReachabilityParametersException {
    if (spec == null) {
      return ImmutableSet.of();
    }
    Set<String> nodes = spec.resolve(_context);
    if (nodes.isEmpty()) {
      throw new InvalidReachabilityParametersException(
          String.format("No nodes match %s specifier", name));
    }
    return nodes;
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
    Set<Location> sourceLocations = _params.getSourceLocationSpecifier().resolve(_context);
    if (sourceLocations.isEmpty()) {
      throw new InvalidReachabilityParametersException("No matching source locations");
    }

    // resolve the IpSpaceSpecifier, and filter out entries with empty IpSpaces
    IpSpaceAssignment sourceIpSpaceAssignment =
        new IpSpaceAssignment(
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

  private void initConfigsAndDataPlane() {
    boolean useCompression = _params.getUseCompression();

    /*
     * TODO specialized compression is currently broken.
     * With the new Location and IpSpaceSpecifiers system, we no longer have a single destination
     * IpSpace to specialize to.
     *
     * What we should do instead is: resolve the destination IpSpaces using the uncompressed
     * dataplane. Then for each destination IpSpace (which is a separate query anyway), compress
     * the network to that IpSpace and proceed.
     *
     * So with specialized compression, we may have multiple dataplanes, which means multiple
     * forwarding analyses and synthesizer input will need to be computed. Each of these is
     * relatively expensive and should be only be done once when specialized compression is
     * disabled.
     *
     * ResolvedReachabilityParameters already has the mapping IpSpace -> Set<Ingress Location>.
     * We could add a field of type: List<Configs, DataPlane, Set<IpSpace>>
     * - Without specialized compression, we'll have just one entry.
     * - With specialized compression, we'll have multiple entries with only singleton IpSpace sets.
     *
     * Alternatively, consider doing data plane compression for specialized compression. That would
     * reduce the overhead of computing multiple dataplanes, forwarding analyses, etc.
     */

    boolean useSpecializedCompression = false;

    CompressDataPlaneResult compressionResult =
        useCompression && useSpecializedCompression
            ? _batfish.computeCompressedDataPlane(_params.getHeaderSpace())
            : null;

    _configs =
        useCompression && useSpecializedCompression
            ? compressionResult._compressedConfigs
            : useCompression
                ? _batfish.loadCompressedConfigurations(_snapshot)
                : _batfish.loadConfigurations(_snapshot);

    if (_configs == null) {
      throw new BatfishException("error loading configurations");
    }

    _dataPlane =
        useCompression && useSpecializedCompression
            ? compressionResult._compressedDataPlane
            : _batfish.loadDataPlane(useCompression);

    if (_dataPlane == null) {
      throw new BatfishException("error loading data plane");
    }
  }
}
