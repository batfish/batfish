package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
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

  public ConvertedConfiguration() {
    this(new HashMap<>(), new HashSet<>());
  }

  @VisibleForTesting
  ConvertedConfiguration(Map<String, Configuration> configurationNodes) {
    this(configurationNodes, new HashSet<>());
  }

  @VisibleForTesting
  ConvertedConfiguration(
      Map<String, Configuration> configurationNodes, Set<Layer1Edge> layer1Edges) {
    _configurationNodes = configurationNodes;
    _layer1Edges = layer1Edges;
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

  @VisibleForTesting
  void addNode(Configuration cfgNode) {
    _configurationNodes.put(cfgNode.getHostname(), cfgNode);
  }

  void addEdge(String nodeName1, String ifaceName1, String nodeName2, String ifaceName2) {
    _layer1Edges.add(new Layer1Edge(nodeName1, ifaceName1, nodeName2, ifaceName2));
  }
}
