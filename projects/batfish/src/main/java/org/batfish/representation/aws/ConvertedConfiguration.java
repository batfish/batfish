package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;

/** Represents vendor independent configuration data after conversion of native AWS data. */
@ParametersAreNonnullByDefault
public class ConvertedConfiguration implements Serializable {

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

  @Nonnull
  public Map<String, Configuration> getConfigurationNodes() {
    return _configurationNodes;
  }

  @Nonnull
  public Set<Layer1Edge> getLayer1Edges() {
    return _layer1Edges;
  }

  public void addNode(Configuration cfgNode) {
    _configurationNodes.put(cfgNode.getHostname(), cfgNode);
  }

  public void addEdge(String nodeName1, String ifaceName1, String nodeName2, String ifaceName2) {
    _layer1Edges.add(new Layer1Edge(nodeName1, ifaceName1, nodeName2, ifaceName2));
  }
}
