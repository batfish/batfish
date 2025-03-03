package org.batfish.representation.azure;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;

public class ConvertedConfiguration implements Serializable {

  /** Map from hostname to Configuration. Hostname lookup is case-insensitive. */
  private final @Nonnull Map<String, Configuration> _configurationNodes;

  private final @Nonnull Set<Layer1Edge> _layer1Edges;

  public ConvertedConfiguration() {
    _configurationNodes = new HashMap<>();
    _layer1Edges = new HashSet<>();
  }

  public @Nonnull Map<String, Configuration> getConfigurationNodes() {
    return _configurationNodes;
  }

  public @Nonnull Set<Layer1Edge> getLayer1Edges() {
    return _layer1Edges;
  }

  public void addNode(Configuration node) {
    _configurationNodes.put(node.getHostname(), node);
  }

  public Configuration getNode(String hostname) {
    return _configurationNodes.get(hostname);
  }

  public Collection<Configuration> getAllNodes() {
    return _configurationNodes.values();
  }

  public void addLayer1Edge(
      String nodeName1, String ifaceName1, String nodeName2, String ifaceName2) {
    _layer1Edges.add(new Layer1Edge(nodeName1, ifaceName1, nodeName2, ifaceName2));
    _layer1Edges.add(new Layer1Edge(nodeName2, ifaceName2, nodeName1, ifaceName1));
  }
}
