package org.batfish.minesweeper.question.comparelayerpolicies;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferBDD.Context;
import org.batfish.minesweeper.bdd.TransferBDDState;
import org.batfish.minesweeper.bdd.TransferParam;
import org.batfish.minesweeper.bdd.TransferResult;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.specifier.SpecifierContext;

/** An answerer for {@link CompareLayerPolicies}. */
@ParametersAreNonnullByDefault
public final class CompareLayerPoliciesAnswerer extends Answerer {

  public CompareLayerPoliciesAnswerer(CompareLayerPolicies question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext context = _batfish.specifierContext(snapshot);
    Map<String, Configuration> configs = context.getConfigs();
    Multimap<String, String> groups =
        TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, String.CASE_INSENSITIVE_ORDER);
    for (String hostname : configs.keySet()) {
      // String group = hostname.replaceAll("\\d+", "1");
      String group = hostname.replaceAll("-r\\d+", "-r1");
      groups.put(group, hostname);
    }
    groups.asMap().entrySet().parallelStream()
        .forEach(
            e -> {
              String group = e.getKey();
              Collection<String> hosts = e.getValue();
              if (hosts.size() < 2) {
                LOGGER.info("Skipping group {} with 1 host {}", group, hosts);
                return;
              }

              processGroup(group, hosts, context);
            });

    return new StringAnswerElement("foo");
  }

  private void processGroup(String group, Collection<String> hosts, SpecifierContext context) {
    LOGGER.info("Processing device group {}: {}", group, hosts);

    List<Configuration> configs =
        hosts.stream()
            .map(h -> context.getConfigs().get(h))
            .collect(ImmutableList.toImmutableList());

    Multimap<String, String> peerGroups =
        TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, String.CASE_INSENSITIVE_ORDER);
    for (Configuration c : configs) {
      allPeers(c)
          .filter(activePeer -> activePeer.getGroup() != null)
          .forEach(activePeer -> peerGroups.put(activePeer.getGroup(), c.getHostname()));
    }

    for (Entry<String, Collection<String>> peerGroup : peerGroups.asMap().entrySet()) {
      if (peerGroup.getValue().size() < 2) {
        LOGGER.info(
            "Skipping peer group {} only on 1 device {}", peerGroup.getKey(), peerGroup.getValue());
        continue;
      }
      processPeerGroup(peerGroup.getKey(), peerGroup.getValue(), context);
    }
  }

  private static Stream<BgpActivePeerConfig> allPeers(Configuration c) {
    return c.getVrfs().values().stream()
        .flatMap(v -> Optional.ofNullable(v.getBgpProcess()).stream())
        .flatMap(b -> b.getActiveNeighbors().values().stream());
  }

  private void processPeerGroup(String group, Collection<String> hosts, SpecifierContext context) {
    LOGGER.info("Processing peer group {} on devices {}", group, hosts);
    Map<String, Configuration> configs = context.getConfigs();
    List<Entry<Configuration, Collection<RoutingPolicy>>> importPolicies = new ArrayList<>();
    List<Entry<Configuration, Collection<RoutingPolicy>>> exportPolicies = new ArrayList<>();
    for (String host : hosts) {
      Configuration c = configs.get(host);
      allPeers(c)
          .filter(p -> group.equals(p.getGroup()))
          .filter(p -> p.getIpv4UnicastAddressFamily() != null)
          .forEach(
              p -> {
                String importPolicy = p.getIpv4UnicastAddressFamily().getImportPolicy();
                if (importPolicy != null) {
                  importPolicies.add(
                      new SimpleImmutableEntry<>(
                          c, Collections.singleton(c.getRoutingPolicies().get(importPolicy))));
                }
                String exportPolicy = p.getIpv4UnicastAddressFamily().getExportPolicy();
                if (exportPolicy != null) {
                  exportPolicies.add(
                      new SimpleImmutableEntry<>(
                          c, Collections.singleton(c.getRoutingPolicies().get(exportPolicy))));
                }
              });
    }

    testPolicies(group + "-in", importPolicies);
    testPolicies(group + "-out", exportPolicies);
  }

  public void testPolicies(
      String group, List<Entry<Configuration, Collection<RoutingPolicy>>> policies) {
    LOGGER.info("Getting Config APs for {} policies", policies.size());
    ConfigAtomicPredicates aps =
        new ConfigAtomicPredicates(policies, ImmutableSet.of(), ImmutableSet.of());
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);

    TransferBDD transferBDD = new TransferBDD(factory, aps);
    BDDRoute baseRoute = transferBDD.getOriginalRoute();
    BDD bgp = baseRoute.getProtocolHistory().value(RoutingProtocol.BGP);

    Multimap<Set<TransferReturn>, String> transferReturns = HashMultimap.create();
    for (Entry<Configuration, Collection<RoutingPolicy>> policy : policies) {
      Configuration c = policy.getKey();
      // TODO: only include static routes directionally.
      BDD staticRoutes =
          factory.orAllAndFree(
              c.getVrfs().values().stream()
                  // TODO: link VRFs to peers
                  .flatMap(v -> v.getStaticRoutes().stream())
                  .map(
                      sr ->
                          baseRoute
                              .getProtocolHistory()
                              .value(RoutingProtocol.STATIC)
                              // tag is -1 for missing, ugh.
                              .andWith(baseRoute.getTag().value(Math.max(sr.getTag(), 0))))
                  .toList());
      RoutingPolicy p = Iterables.getOnlyElement(policy.getValue());

      TransferBDDState baseState =
          new TransferBDDState(
              new TransferParam(baseRoute, false),
              new TransferResult(baseRoute, staticRoutes.orEq(bgp)));
      List<TransferReturn> paths =
          transferBDD
              .computePaths(baseState, p.getStatements(), Context.forPolicy(p), false)
              .stream()
              .map(TransferResult::getReturnValue)
              .toList();
      transferReturns.put(combineTransferReturns(paths), c.getHostname());
    }

    if (transferReturns.asMap().size() != 1) {
      LOGGER.warn(
          "Peer group {} is not configured uniformly: distinct entries size {}",
          group,
          transferReturns.asMap().entrySet().stream()
              .map(
                  e ->
                      new SimpleEntry(
                          e.getValue().stream().min(Ordering.natural()), e.getValue().size()))
              .collect(ImmutableList.toImmutableList()));
    } else {
      LOGGER.info("Peer group {} is configured uniformly", group);
    }
  }

  private static Set<TransferReturn> combineTransferReturns(List<TransferReturn> input) {
    if (input.size() < 2) {
      return ImmutableSet.copyOf(input);
    }
    BDDFactory factory = input.get(0).getInputConstraints().getFactory();
    Map<BDDRoute, Collection<BDD>> grouped =
        input.stream()
            .filter(TransferReturn::getAccepted)
            .collect(
                ImmutableListMultimap.toImmutableListMultimap(
                    TransferReturn::getOutputRoute, TransferReturn::getInputConstraints))
            .asMap();
    if (grouped.size() == input.size()) {
      return ImmutableSet.copyOf(input);
    }
    LOGGER.info(
        "Condensed {} computed paths into {} distinct accepted outputs",
        input.size(),
        grouped.size());
    ImmutableSet.Builder<TransferReturn> ret = ImmutableSet.builderWithExpectedSize(grouped.size());
    grouped.forEach(
        (output, inputConstraints) ->
            ret.add(new TransferReturn(output, factory.orAll(inputConstraints), true)));
    return ret.build();
  }

  private static final Logger LOGGER = LogManager.getLogger(CompareLayerPoliciesAnswerer.class);
}
