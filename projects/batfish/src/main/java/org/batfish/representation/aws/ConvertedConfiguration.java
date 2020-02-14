package org.batfish.representation.aws;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.Prefix;

/** Represents vendor independent configuration data after conversion of native AWS data. */
@ParametersAreNonnullByDefault
public class ConvertedConfiguration implements Serializable {

  private static final long INITIAL_GENERATED_IP = Ip.FIRST_CLASS_E_EXPERIMENTAL_IP.asLong();

  @Nonnull private final Map<String, Configuration> _configurationNodes;

  @Nonnull private final Set<Layer1Edge> _layer1Edges;

  @Nonnull private final AtomicLong _currentGeneratedIpAsLong;

  public ConvertedConfiguration() {
    this(new HashMap<>(), new HashSet<>(), new AtomicLong(INITIAL_GENERATED_IP));
  }

  @VisibleForTesting
  ConvertedConfiguration(Map<String, Configuration> configurationNodes) {
    this(configurationNodes, new HashSet<>(), new AtomicLong(INITIAL_GENERATED_IP));
  }

  @VisibleForTesting
  ConvertedConfiguration(
      Map<String, Configuration> configurationNodes,
      Set<Layer1Edge> layer1Edges,
      AtomicLong currentGeneratedIpAsLong) {
    _configurationNodes = configurationNodes;
    _layer1Edges = layer1Edges;
    _currentGeneratedIpAsLong = currentGeneratedIpAsLong;
  }

  @Nonnull
  Prefix getNextGeneratedLinkSubnet() {
    long base = _currentGeneratedIpAsLong.getAndAdd(2L);
    assert base % 2 == 0;
    return Prefix.create(Ip.create(base), Prefix.MAX_PREFIX_LENGTH - 1);
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
