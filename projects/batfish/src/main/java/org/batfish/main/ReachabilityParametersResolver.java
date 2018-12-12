package org.batfish.main;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.main.Batfish.CompressDataPlaneResult;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.Location;
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

  private final IpSpaceRepresentative _ipSpaceRepresentative;

  @VisibleForTesting
  ReachabilityParametersResolver(
      Batfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot) {
    _batfish = batfish;
    _params = params;
    _snapshot = snapshot;
    initConfigsAndDataPlane();
    _context = new SpecifierContextImpl(batfish, _snapshot);
    _ipSpaceRepresentative = new IpSpaceRepresentative();
  }

  public static ResolvedReachabilityParameters resolveReachabilityParameters(
      Batfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot)
      throws InvalidReachabilityParametersException {

    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(batfish, params, snapshot);
    SpecifierContextImpl context = resolver._context;

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
        .setConfigurations(resolver._configs)
        .setDataPlane(resolver._dataPlane)
        .setFinalNodes(finalNodes)
        .setForbiddenTransitNodes(forbiddenTransitNodes)
        .setHeaderSpace(resolver.resolveHeaderSpace())
        .setIgnoreFilters(params.getIgnoreFilters())
        .setMaxChunkSize(params.getMaxChunkSize())
        .setSourceIpSpaceAssignment(resolver.resolveSourceIpSpaceAssignment())
        .setSrcNatted(params.getSrcNatted())
        .setSpecialize(params.getSpecialize())
        .setRequiredTransitNodes(requiredTransitNodes)
        .setUseCompression(params.getUseCompression())
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

  /** Returns {@code true} iff the given {@link Location} is active (aka, interface is up). */
  @VisibleForTesting
  static boolean isActive(Location l, Map<String, Configuration> configs) {
    NodeInterfacePair iface;
    if (l instanceof InterfaceLocation) {
      iface =
          new NodeInterfacePair(
              ((InterfaceLocation) l).getNodeName(), ((InterfaceLocation) l).getInterfaceName());
    } else {
      assert l instanceof InterfaceLinkLocation;
      iface =
          new NodeInterfacePair(
              ((InterfaceLinkLocation) l).getNodeName(),
              ((InterfaceLinkLocation) l).getInterfaceName());
    }
    return configs
        .get(iface.getHostname())
        .getAllInterfaces()
        .get(iface.getInterface())
        .getActive();
  }

  @VisibleForTesting
  IpSpaceAssignment resolveSourceIpSpaceAssignment() throws InvalidReachabilityParametersException {
    Set<Location> sourceLocations =
        _params
            .getSourceLocationSpecifier()
            .resolve(_context)
            .stream()
            .filter(l -> isActive(l, _configs))
            .collect(ImmutableSet.toImmutableSet());
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

  private void initConfigsAndDataPlane() {
    /*
     * TODO Fix compression to use a more expressive representation of HeaderSpaces
     * The compression code expects the headerspace to be expressed as a HeaderSpace object,
     * but ReachabilityParameters use a more expressive representation. Until we update compression,
     * we can't use it.
     */
    Preconditions.checkArgument(!_params.getUseCompression(), "Compression is currently disabled.");
    boolean useCompression = false;
    HeaderSpace compressionHeaderSpace = null;

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
            ? _batfish.computeCompressedDataPlane(compressionHeaderSpace)
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
