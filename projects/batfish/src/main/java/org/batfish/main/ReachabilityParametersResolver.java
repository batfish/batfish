package org.batfish.main;

import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.main.Batfish.CompressDataPlaneResult;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierContextImpl;

/**
 * Resolve a {@link ReachabilityParameters} and return a {@link ResolvedReachabilityParameters}
 * object. This involves getting the right configs and dataplane, and resolving Location, Node, and
 * IpSpace specifiers.
 */
final class ReachabilityParametersResolver {
  private final Batfish _batfish;

  private Map<String, Configuration> _configs;

  private DataPlane _dataPlane;

  private final ReachabilityParameters _params;

  private final NetworkSnapshot _snapshot;

  private ReachabilityParametersResolver(
      Batfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot) {
    _batfish = batfish;
    _params = params;
    _snapshot = snapshot;
    initConfigsAndDataPlane();
  }

  public static ResolvedReachabilityParameters resolveReachabilityParameters(
      Batfish batfish, ReachabilityParameters params, NetworkSnapshot snapshot)
      throws InvalidReachabilityParametersException {

    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(batfish, params, snapshot);

    SpecifierContext context = new SpecifierContextImpl(batfish, resolver._configs);
    return ResolvedReachabilityParameters.builder()
        .setActions(params.getActions())
        .setConfigurations(resolver._configs)
        .setDataPlane(resolver._dataPlane)
        .setFinalNodes(params.getFinalNodesSpecifier().resolve(context))
        .setForbiddenTransitNodes(params.getForbiddenTransitNodesSpecifier().resolve(context))
        .setHeaderSpace(params.getHeaderSpace())
        .setMaxChunkSize(params.getMaxChunkSize())
        .setSourceIpSpaceAssignment(
            params
                .getSourceIpSpaceSpecifier()
                .resolve(params.getSourceLocationSpecifier().resolve(context), context))
        .setSrcNatted(params.getSrcNatted())
        .setSpecialize(params.getSpecialize())
        .setRequiredTransitNodes(params.getRequiredTransitNodesSpecifier().resolve(context))
        .setUseCompression(params.getUseCompression())
        .build();
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
