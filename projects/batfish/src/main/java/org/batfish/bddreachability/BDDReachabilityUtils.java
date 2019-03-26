package org.batfish.bddreachability;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.z3.expr.StateExpr;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
public final class BDDReachabilityUtils {
  static Map<StateExpr, Map<StateExpr, Edge>> computeForwardEdgeMap(Iterable<Edge> edges) {
    Map<StateExpr, Map<StateExpr, Edge>> forwardEdges = new HashMap<>();

    edges.forEach(
        edge ->
            forwardEdges
                .computeIfAbsent(edge.getPreState(), k -> new HashMap<>())
                .put(edge.getPostState(), edge));

    // freeze
    return toImmutableMap(
        forwardEdges, Entry::getKey, entry -> ImmutableMap.copyOf(entry.getValue()));
  }

  static Map<StateExpr, Map<StateExpr, Edge>> computeReverseEdgeMap(Iterable<Edge> edges) {
    Map<StateExpr, Map<StateExpr, Edge>> reverseEdges = new HashMap<>();
    edges.forEach(
        edge ->
            reverseEdges
                .computeIfAbsent(edge.getPostState(), k -> new HashMap<>())
                .put(edge.getPreState(), edge));
    // freeze
    return toImmutableMap(
        reverseEdges, Entry::getKey, entry -> ImmutableMap.copyOf(entry.getValue()));
  }

  public static Map<String, Map<String, IpSpace>> computeIpsNotOwnedByInactiveInterfaces(
      Map<String, Configuration> configs) {
    return toImmutableMap(
        configs,
        Entry::getKey,
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue().getVrfs(),
                Entry::getKey,
                vrfEntry ->
                    firstNonNull(
                            AclIpSpace.union(
                                vrfEntry.getValue().getInterfaces().values().stream()
                                    .filter(i -> !i.getActive())
                                    .map(Interface::getAllAddresses)
                                    .flatMap(Set::stream)
                                    .map(InterfaceAddress::getIp)
                                    .map(Ip::toIpSpace)
                                    .collect(Collectors.toList())),
                            EmptyIpSpace.INSTANCE)
                        .complement()));
  }
}
