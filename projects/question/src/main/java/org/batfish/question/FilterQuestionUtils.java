package org.batfish.question;

import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationVisitor;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Utilities for questions about {@link IpAccessList Filters}. */
public final class FilterQuestionUtils {
  private FilterQuestionUtils() {}

  /** Get filters specified by the given filter specifier. */
  public static Multimap<String, String> getSpecifiedFilters(
      SpecifierContext specifierContext,
      NodeSpecifier nodeSpecifier,
      FilterSpecifier filterSpecifier,
      boolean ignoreComposites) {
    Set<String> nodes = nodeSpecifier.resolve(specifierContext);
    ImmutableMultimap.Builder<String, String> filters = ImmutableMultimap.builder();
    Map<String, Configuration> configs = specifierContext.getConfigs();
    nodes.stream()
        .map(configs::get)
        .forEach(
            config ->
                filterSpecifier.resolve(config.getHostname(), specifierContext).stream()
                    .filter(f -> !ignoreComposites || !f.isComposite())
                    .forEach(filter -> filters.put(config.getHostname(), filter.getName())));
    return filters.build();
  }

  /**
   * Instantiate a {@link BDDSourceManager} that tracks sources that are active and referenced on
   * both the current and reference version of a node. Further scope references to the input ACLs,
   * and active sources to those specified by the input {@link LocationSpecifier}.
   */
  public static BDDSourceManager differentialBDDSourceManager(
      BDDPacket bddPacket,
      SpecifierContext baseSpecifierContext,
      SpecifierContext refSpecifierContext,
      Configuration baseConfig,
      Configuration refConfig,
      Set<String> aclNames,
      LocationSpecifier startLocationSpecifier) {
    String hostname = baseConfig.getHostname();

    // resolve specified source interfaces that exist in both configs.
    Set<String> commonSources =
        Sets.intersection(
            resolveSources(baseSpecifierContext, startLocationSpecifier, hostname),
            resolveSources(refSpecifierContext, startLocationSpecifier, hostname));

    Set<String> inactiveInterfaces =
        Sets.union(
            Sets.difference(
                baseConfig.getAllInterfaces().keySet(), baseConfig.activeInterfaceNames()),
            Sets.difference(
                refConfig.getAllInterfaces().keySet(), refConfig.activeInterfaceNames()));

    // effectively active sources are those of interest that are active in both configs.
    Set<String> activeSources = Sets.difference(commonSources, inactiveInterfaces);
    Set<String> referencedSources =
        Sets.union(
            referencedSources(baseConfig.getIpAccessLists(), aclNames),
            referencedSources(refConfig.getIpAccessLists(), aclNames));

    return BDDSourceManager.forSources(bddPacket, activeSources, referencedSources);
  }

  /** Return a concrete flow satisfying the input {@link BDD}, if one exists. */
  public static Optional<Flow> getFlow(
      BDDPacket pkt, BDDSourceManager bddSourceManager, String hostname, BDD bdd) {
    if (bdd.isZero()) {
      return Optional.empty();
    }
    BDD assignment = pkt.getFlowBDD(bdd, FlowPreference.TESTFILTER).fullSatOne();
    return Optional.of(
        pkt.getRepresentativeFlow(assignment)
            .setIngressNode(hostname)
            .setIngressInterface(bddSourceManager.getSourceFromAssignment(assignment).orElse(null))
            .build());
  }

  /**
   * Resolve the set of filter sources (for {@link MatchSrcInterface} and {@link
   * org.batfish.datamodel.acl.OriginatingFromDevice} specified by the input {@link
   * LocationSpecifier}.
   */
  public static Set<String> resolveSources(
      SpecifierContext specifierContext, LocationSpecifier startLocationSpecifier, String node) {
    LocationVisitor<String> locationToSource =
        new LocationVisitor<String>() {
          @Override
          public String visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
            return interfaceLinkLocation.getInterfaceName();
          }

          @Override
          public String visitInterfaceLocation(InterfaceLocation interfaceLocation) {
            return SOURCE_ORIGINATING_FROM_DEVICE;
          }
        };

    return startLocationSpecifier.resolve(specifierContext).stream()
        .filter(loc -> loc.getNodeName().equals(node))
        .map(locationToSource::visit)
        .collect(ImmutableSet.toImmutableSet());
  }
}
