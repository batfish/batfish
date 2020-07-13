package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;

/** Represents vendor independent configuration data after conversion of native AWS data. */
@ParametersAreNonnullByDefault
class ConvertedConfiguration implements Serializable {

  @Nonnull private final Map<String, Configuration> _configurationNodes;
  @Nonnull private final Set<Layer1Edge> _layer1Edges;
  /** A multimap from Subnet -> Instance targets within subnet */
  @Nonnull private final Multimap<Subnet, Instance> _subnetsToInstanceTargets;

  /** A multimap from Subnet -> NLBs within subnet that have instance targets */
  @Nonnull private final Multimap<Subnet, LoadBalancer> _subnetsToNlbs;

  /** A multimap of NLB -> instance targets */
  @Nonnull private final Multimap<LoadBalancer, Instance> _nlbsToInstanceTargets;

  /** A set of all VPCs that contain load balancers with active instance targets */
  @Nonnull private final Set<Vpc> _vpcsWithInstanceTargets;

  public ConvertedConfiguration(AwsConfiguration awsConfiguration) {
    this(
        new HashMap<>(),
        new HashSet<>(),
        awsConfiguration.getSubnetsToInstanceTargets(),
        awsConfiguration.getSubnetsToNlbs(),
        awsConfiguration.getNlbsToInstanceTargets(),
        awsConfiguration.getVpcsWithInstanceTargets());
  }

  @VisibleForTesting
  public ConvertedConfiguration() {
    this(
        new HashMap<>(),
        new HashSet<>(),
        ImmutableMultimap.of(),
        ImmutableMultimap.of(),
        ImmutableMultimap.of(),
        ImmutableSet.of());
  }

  @VisibleForTesting
  ConvertedConfiguration(Map<String, Configuration> configurationNodes) {
    this(
        configurationNodes,
        new HashSet<>(),
        ImmutableMultimap.of(),
        ImmutableMultimap.of(),
        ImmutableMultimap.of(),
        ImmutableSet.of());
  }

  @VisibleForTesting
  ConvertedConfiguration(
      Map<String, Configuration> configurationNodes,
      Set<Layer1Edge> layer1Edges,
      Multimap<Subnet, Instance> subnetsToInstanceTargets,
      Multimap<Subnet, LoadBalancer> subnetsToNlbs,
      Multimap<LoadBalancer, Instance> nlbsToInstanceTargets,
      Set<Vpc> vpcsWithInstanceTargets) {
    _configurationNodes = configurationNodes;
    _layer1Edges = layer1Edges;
    _subnetsToInstanceTargets = ImmutableMultimap.copyOf(subnetsToInstanceTargets);
    _subnetsToNlbs = ImmutableMultimap.copyOf(subnetsToNlbs);
    _nlbsToInstanceTargets = ImmutableMultimap.copyOf(nlbsToInstanceTargets);
    _vpcsWithInstanceTargets = ImmutableSet.copyOf(vpcsWithInstanceTargets);
  }

  /** Return configuration nodes across all accounts */
  public Collection<Configuration> getAllNodes() {
    return _configurationNodes.values();
  }

  @Nullable
  public Configuration getNode(String hostname) {
    return _configurationNodes.get(hostname);
  }

  @Nonnull
  public Set<Layer1Edge> getLayer1Edges() {
    return _layer1Edges;
  }

  void addNode(Configuration cfgNode) {
    _configurationNodes.put(cfgNode.getHostname(), cfgNode);
  }

  void addEdge(String nodeName1, String ifaceName1, String nodeName2, String ifaceName2) {
    _layer1Edges.add(new Layer1Edge(nodeName1, ifaceName1, nodeName2, ifaceName2));
  }

  @VisibleForTesting
  Multimap<Subnet, Instance> getSubnetsToInstanceTargets() {
    return _subnetsToInstanceTargets;
  }

  @VisibleForTesting
  Multimap<Subnet, LoadBalancer> getSubnetsToNlbs() {
    return _subnetsToNlbs;
  }

  @VisibleForTesting
  Multimap<LoadBalancer, Instance> getNlbsToInstanceTargets() {
    return _nlbsToInstanceTargets;
  }

  @VisibleForTesting
  Set<Vpc> getVpcsWithInstanceTargets() {
    return _vpcsWithInstanceTargets;
  }
}
