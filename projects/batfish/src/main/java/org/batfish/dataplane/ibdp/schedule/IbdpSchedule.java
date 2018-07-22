package org.batfish.dataplane.ibdp.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.ValueGraph;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.dataplane.ibdp.IncrementalDataPlaneSettings;
import org.batfish.dataplane.ibdp.Node;
import org.batfish.dataplane.ibdp.schedule.NodeColoredSchedule.Coloring;

/**
 * Dataplane computation schedule. Represents the order in which nodes are allowed to process
 * messages
 */
public abstract class IbdpSchedule implements Iterator<Map<String, Node>> {

  public enum Schedule {
    ALL,
    NODE_COLORED,
    NODE_SERIALIZED,
  }

  protected ImmutableMap<String, Node> _nodes;

  IbdpSchedule(Map<String, Node> nodes) {
    _nodes = ImmutableMap.copyOf(nodes);
  }

  /**
   * @throws UnsupportedOperationException since removing elements from the schedule is not
   *     supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing elements from schedule is not supported");
  }

  /**
   * Checks if unprocessed nodes are available in this schedule
   *
   * @return true if more unprocessed nodes are available
   */
  @Override
  public abstract boolean hasNext();

  /**
   * Get the next set of nodes that are allowed to be run in parallel during a dataplane iteration
   *
   * @return a map of nodes keyed by name, containing a subset of all network nodes
   */
  @Override
  public abstract Map<String, Node> next();

  /**
   * Return the remaining nodes a list grouped by color.
   *
   * @return a list of maps, where each map is a subset of network nodes of the same color, keyed by
   *     hostname
   */
  public List<Map<String, Node>> getAllRemaining() {
    return ImmutableList.copyOf(this);
  }

  /**
   * Create a new schedule based on type and the set of all nodes in the network
   *
   * @param settings {@link IncrementalDataPlaneSettings}
   * @param allNodes map of all nodes in the network
   * @param bgpTopology the bgp peering relationships
   * @return a new {@link IbdpSchedule}
   * @throws BatfishException if the schedule type specified is unsupported
   */
  public static IbdpSchedule getSchedule(
      IncrementalDataPlaneSettings settings,
      Map<String, Node> allNodes,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology) {
    Schedule schedule = settings.getScheduleName();
    switch (schedule) {
      case ALL:
        return new MaxParallelSchedule(allNodes);
      case NODE_SERIALIZED:
        return new NodeSerializedSchedule(allNodes);
      case NODE_COLORED:
        Coloring coloring = settings.getColoringType();
        return new NodeColoredSchedule(allNodes, coloring, bgpTopology);
      default:
        throw new BatfishException(String.format("Unsupported ibdp schedule: %s", schedule));
    }
  }
}
