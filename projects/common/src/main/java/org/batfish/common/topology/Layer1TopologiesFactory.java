package org.batfish.common.topology;

import static org.batfish.common.topology.Layer1Topologies.INVALID_INTERFACE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/**
 * Computes the {@link Layer1Topologies} for a given raw user-provided {@link Layer1Topology},
 * synthesized {@link Layer1Topology}, and the configurations for the snapshot they go with.
 */
@ParametersAreNonnullByDefault
public final class Layer1TopologiesFactory {
  private static final Logger LOGGER = LogManager.getLogger(Layer1TopologiesFactory.class);

  /**
   * Computes the {@link Layer1Topologies} for a given raw user-provided {@link Layer1Topology},
   * synthesized {@link Layer1Topology}, and the configurations for the snapshot they go with.
   */
  public static @Nonnull Layer1Topologies create(
      Layer1Topology userProvidedL1,
      Layer1Topology synthesizedL1,
      Map<String, Configuration> configs) {
    Layer1Topology canonicalUserL1 = canonicalizeLayer1TopologyNodes(userProvidedL1, configs);
    Layer1Topology logicalL1 = toLogicalTopology(canonicalUserL1, synthesizedL1, configs);
    Layer1Topology activeLogicalL1 = cleanLogicalTopology(logicalL1, configs);
    return new Layer1Topologies(canonicalUserL1, synthesizedL1, logicalL1, activeLogicalL1);
  }

  // Internal implementation details.

  /**
   * Attempts to line up the user-provided {@link Layer1Node} with an actual interface.
   *
   * <p>If no match is found (the device does not exist in this snapshot, or no corresponding
   * interface), then the original node is returned but the values are lowercased.
   */
  private static @Nonnull Layer1Node canonicalizeUserNode(
      Layer1Node node, Map<String, Configuration> configurations, Set<String> missingDevices) {
    Configuration c = configurations.get(node.getHostname());
    if (c == null) {
      if (missingDevices.add(node.getHostname())) {
        LOGGER.info(
            "Layer 1 topology has node {}, but device {} not found", node, node.getHostname());
      }
      // Host unknown, return node with the hostname in lowercase.
      return new Layer1Node(node.getHostname(), node.getInterfaceName().toLowerCase());
    }

    // Find a matching interface on the host, perhaps with a slightly different interface name.
    // If found, create a new Layer1Node with that name, else return original.
    return InterfaceUtil.matchingInterface(node.getInterfaceName(), c)
        .map(i -> new Layer1Node(c.getHostname(), i.getName()))
        .orElse(node);
  }

  /**
   * Returns a new {@link Layer1Topology} where every node with an interface in non-canonical form
   * (aka, not appearing in its corresponding {@link Configuration device's} {@link
   * Configuration#getAllInterfaces()}) has a name that does appear, if one exists.
   *
   * <p>Note: preserved unmodified any nodes that correspond to unknown devices or interfaces.
   */
  private static Layer1Topology canonicalizeLayer1TopologyNodes(
      Layer1Topology topology, Map<String, Configuration> configurations) {
    Map<Layer1Node, Layer1Node> replacements = new HashMap<>();
    Set<String> missingDevices = new HashSet<>(); // dedupe warnings about missing devices
    for (Layer1Node original : topology.nodes()) {
      Layer1Node canonical = canonicalizeUserNode(original, configurations, missingDevices);
      if (!canonical.equals(original)) {
        replacements.put(original, canonical);
        // Reduce log level when we are only changing case.
        Level level =
            canonical.getInterfaceName().equalsIgnoreCase(original.getInterfaceName())
                ? Level.DEBUG
                : Level.INFO;
        LOGGER.log(level, "Replacing provided {} with canonical {}", original, canonical);
      }
    }
    if (replacements.isEmpty()) {
      return topology;
    }
    return new Layer1Topology(
        topology
            .edgeStream()
            .map(
                edge ->
                    new Layer1Edge(
                        replacements.getOrDefault(edge.getNode1(), edge.getNode1()),
                        replacements.getOrDefault(edge.getNode2(), edge.getNode2()))));
  }

  /**
   * Maps physical interfaces that have been bundled into port-channels (or other {@link
   * org.batfish.datamodel.InterfaceType#AGGREGATED} interface types) to their aggregate parents.
   *
   * <p>Non-existent interfaces or interfaces with invalid bundling are replaced by {@link
   * Layer1Topologies#INVALID_INTERFACE}.
   */
  private static @Nonnull Layer1Node toLogicalNode(
      Layer1Node node, Map<String, Configuration> configs, Set<Layer1Node> invalidInterfaces) {
    Configuration c = configs.get(node.getHostname());
    if (c == null) {
      // No such device; already been warned on earlier.
      return INVALID_INTERFACE;
    }
    Interface iface = c.getAllInterfaces().get(node.getInterfaceName());
    if (iface == null) {
      if (invalidInterfaces.add(node)) {
        LOGGER.info(
            "Layer 1 topology has interface {}, but it is not found in {}",
            node,
            c.getAllInterfaces().keySet());
      }
      return INVALID_INTERFACE;
    }
    String aggregateName = iface.getChannelGroup();
    if (aggregateName == null) {
      // Not aggregated; return as-is.
      return node;
    }
    Interface aggregate = c.getAllInterfaces().get(iface.getChannelGroup());
    if (aggregate == null) {
      // No such aggregate interface.
      if (invalidInterfaces.add(node)) {
        LOGGER.warn(
            "Interface {} is marked as part of aggregated interface {} that is not found in {}",
            node,
            aggregateName,
            c.getAllInterfaces().keySet());
      }
      return INVALID_INTERFACE;
    }
    return new Layer1Node(c.getHostname(), aggregate.getName());
  }

  /**
   * @see Layer1Topologies#getLogicalL1()
   */
  private static @Nonnull Layer1Topology toLogicalTopology(
      Layer1Topology userProvidedL1,
      Layer1Topology syntheticL1,
      Map<String, Configuration> configs) {
    Set<Layer1Node> invalidInterfaces =
        new HashSet<>(); // dedupe warnings about missing or invalid interfaces.
    return new Layer1Topology(
        Stream.concat(userProvidedL1.edgeStream(), syntheticL1.edgeStream())
            .map(
                edge ->
                    new Layer1Edge(
                        toLogicalNode(edge.getNode1(), configs, invalidInterfaces),
                        toLogicalNode(edge.getNode2(), configs, invalidInterfaces))));
  }

  /**
   * Returns a copy of the input logical topology, in which any edge with an {@link
   * Layer1Topologies#INVALID_INTERFACE invalid} or inactive interface has been removed, and all
   * edges are bidirectional.
   */
  private static @Nonnull Layer1Topology cleanLogicalTopology(
      Layer1Topology logicalTopology, Map<String, Configuration> configs) {
    return new Layer1Topology(
        logicalTopology
            .edgeStream()
            .filter(
                edge -> isActive(edge.getNode1(), configs) && isActive(edge.getNode2(), configs))
            .flatMap(edge -> Stream.of(edge, edge.reverse())));
  }

  /** Returns true if the node corresponds to an existing, active interface. */
  private static boolean isActive(Layer1Node node, Map<String, Configuration> configs) {
    return !node.equals(INVALID_INTERFACE)
        && configs
            .get(node.getHostname())
            .getAllInterfaces()
            .get(node.getInterfaceName())
            .getActive();
  }

  private Layer1TopologiesFactory() {} // prevent instantiation of utility class.
}
