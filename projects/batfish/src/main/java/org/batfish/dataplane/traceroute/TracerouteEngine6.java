package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib6;
import org.batfish.datamodel.FibEntry6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Route;

/**
 * Route/FIB-level IPv6 path tracer.
 *
 * <p>This engine is intentionally separate from the existing IPv4
 * {@code FlowTracer}. It models IPv6 forwarding and Neighbor Discovery using
 * {@link Fib6}. ACL/NAT/session processing can be layered on after the packet
 * model itself supports IPv6.
 */
@ParametersAreNonnullByDefault
public final class TracerouteEngine6 {

  private static final int DEFAULT_MAX_HOPS = 32;

  private static final class InterfaceLocation {
    private InterfaceLocation(
        String node,
        String vrf,
        String interfaceName) {
      _node = node;
      _vrf = vrf;
      _interfaceName = interfaceName;
    }

    private final String _interfaceName;
    private final String _node;
    private final String _vrf;
  }

  public static TracerouteEngine6 fromDataPlane(
      Map<String, Configuration> configurations,
      DataPlane dataPlane) {
    return new TracerouteEngine6(
        configurations,
        dataPlane.getFibs6());
  }

  public TracerouteEngine6(
      Map<String, Configuration> configurations,
      Map<String, Map<String, Fib6>> fibs) {
    _configurations =
        ImmutableMap.copyOf(configurations);
    _fibs = deepCopyFibs(fibs);
    _addressOwners =
        computeAddressOwners(_configurations);
  }

  public @Nonnull List<Ipv6Trace> computeTraces(
      String ingressNode,
      String ingressVrf,
      Ip6 destination) {
    return computeTraces(
        ingressNode,
        ingressVrf,
        destination,
        DEFAULT_MAX_HOPS);
  }

  public @Nonnull List<Ipv6Trace> computeTraces(
      String ingressNode,
      String ingressVrf,
      Ip6 destination,
      int maxHops) {

    checkArgument(
        maxHops > 0,
        "maxHops must be positive");

    checkArgument(
        _configurations.containsKey(
            ingressNode),
        "Unknown ingress node %s",
        ingressNode);

    List<Ipv6Trace> traces =
        new ArrayList<>();

    trace(
        ingressNode,
        ingressVrf,
        destination,
        maxHops,
        new ArrayList<>(),
        new HashSet<>(),
        traces);

    return ImmutableList.copyOf(traces);
  }

  private void trace(
      String node,
      String vrf,
      Ip6 destination,
      int maxHops,
      List<Ipv6TraceHop> hops,
      Set<String> visited,
      List<Ipv6Trace> traces) {

    String state = node + "\u0000" + vrf;

    if (visited.contains(state)) {
      List<Ipv6TraceHop> terminal =
          new ArrayList<>(hops);
      terminal.add(
          Ipv6TraceHop.terminal(
              node, vrf));
      traces.add(
          new Ipv6Trace(
              Ipv6TraceDisposition.LOOP,
              terminal));
      return;
    }

    if (hops.size() >= maxHops) {
      List<Ipv6TraceHop> terminal =
          new ArrayList<>(hops);
      terminal.add(
          Ipv6TraceHop.terminal(
              node, vrf));
      traces.add(
          new Ipv6Trace(
              Ipv6TraceDisposition.MAX_HOPS,
              terminal));
      return;
    }

    Configuration configuration =
        _configurations.get(node);

    if (configuration == null) {
      List<Ipv6TraceHop> terminal =
          new ArrayList<>(hops);
      terminal.add(
          Ipv6TraceHop.terminal(
              node, vrf));
      traces.add(
          new Ipv6Trace(
              Ipv6TraceDisposition
                  .NEIGHBOR_UNREACHABLE,
              terminal));
      return;
    }

    if (ownsAddress(
        configuration,
        vrf,
        destination)) {
      List<Ipv6TraceHop> accepted =
          new ArrayList<>(hops);
      accepted.add(
          Ipv6TraceHop.terminal(
              node, vrf));
      traces.add(
          new Ipv6Trace(
              Ipv6TraceDisposition.ACCEPTED,
              accepted));
      return;
    }

    Fib6 fib =
        getFib(node, vrf);

    if (fib == null) {
      addTerminal(
          traces,
          hops,
          node,
          vrf,
          Ipv6TraceDisposition.NO_ROUTE);
      return;
    }

    Set<FibEntry6> entries =
        fib.get(destination);

    if (entries.isEmpty()) {
      addTerminal(
          traces,
          hops,
          node,
          vrf,
          Ipv6TraceDisposition.NO_ROUTE);
      return;
    }

    Set<String> nextVisited =
        new HashSet<>(visited);
    nextVisited.add(state);

    entries.stream()
        .sorted(
            Comparator
                .comparing(
                    FibEntry6::getInterfaceName)
                .thenComparing(
                    entry ->
                        entry.getNextHopIp()
                            .map(Ip6::toString)
                            .orElse("")))
        .forEach(
            entry ->
                traceFibEntry(
                    configuration,
                    node,
                    vrf,
                    destination,
                    maxHops,
                    hops,
                    nextVisited,
                    traces,
                    entry));
  }

  private void traceFibEntry(
      Configuration configuration,
      String node,
      String vrf,
      Ip6 destination,
      int maxHops,
      List<Ipv6TraceHop> hops,
      Set<String> visited,
      List<Ipv6Trace> traces,
      FibEntry6 entry) {

    String outgoingInterfaceName =
        entry.getInterfaceName();

    if (Route.UNSET_NEXT_HOP_INTERFACE.equals(
        outgoingInterfaceName)) {
      addTerminal(
          traces,
          hops,
          node,
          vrf,
          Ipv6TraceDisposition.NO_ROUTE);
      return;
    }

    Interface outgoingInterface =
        configuration
            .getAllInterfaces()
            .get(outgoingInterfaceName);

    if (outgoingInterface == null
        || !outgoingInterface.getActive()
        || !vrf.equals(
            outgoingInterface.getVrfName())) {
      addTerminal(
          traces,
          hops,
          node,
          vrf,
          Ipv6TraceDisposition
              .NEIGHBOR_UNREACHABLE);
      return;
    }

    boolean hasExplicitNextHop =
        entry.getNextHopIp().isPresent();

    Ip6 ndTarget =
        entry.getNextHopIp()
            .orElse(destination);

    List<Ipv6TraceHop> forwarded =
        new ArrayList<>(hops);
    forwarded.add(
        Ipv6TraceHop.forwarding(
            node,
            vrf,
            outgoingInterfaceName,
            ndTarget));

    if (!isOnLink(
        outgoingInterface,
        ndTarget)) {
      traces.add(
          new Ipv6Trace(
              Ipv6TraceDisposition
                  .NEIGHBOR_UNREACHABLE,
              forwarded));
      return;
    }

    List<InterfaceLocation> owners =
        _addressOwners.get(ndTarget);

    if (owners == null || owners.isEmpty()) {
      traces.add(
          new Ipv6Trace(
              hasExplicitNextHop
                  ? Ipv6TraceDisposition
                      .NEIGHBOR_UNREACHABLE
                  : Ipv6TraceDisposition
                      .EXITS_NETWORK,
              forwarded));
      return;
    }

    for (InterfaceLocation owner : owners) {
      // Ignore an address on the same node unless the destination was already
      // accepted above. Forwarding a packet back into the same node through
      // another interface is not a valid NDP adjacency in this model.
      if (node.equals(owner._node)) {
        continue;
      }

      trace(
          owner._node,
          owner._vrf,
          destination,
          maxHops,
          forwarded,
          new HashSet<>(visited),
          traces);
    }

    // Every exact owner was local to this node, so no usable neighbor existed.
    if (owners.stream()
        .allMatch(
            owner ->
                node.equals(owner._node))) {
      traces.add(
          new Ipv6Trace(
              Ipv6TraceDisposition
                  .NEIGHBOR_UNREACHABLE,
              forwarded));
    }
  }

  private Fib6 getFib(
      String node,
      String vrf) {
    Map<String, Fib6> nodeFibs =
        _fibs.get(node);

    return nodeFibs == null
        ? null
        : nodeFibs.get(vrf);
  }

  private static boolean ownsAddress(
      Configuration configuration,
      String vrf,
      Ip6 ip) {
    return configuration
        .getAllInterfaces()
        .values()
        .stream()
        .filter(Interface::getActive)
        .filter(
            iface ->
                vrf.equals(
                    iface.getVrfName()))
        .flatMap(
            iface ->
                iface.getAllConcreteAddresses6()
                    .stream())
        .anyMatch(
            address ->
                address.getIp().equals(ip));
  }

  private static boolean isOnLink(
      Interface iface,
      Ip6 ip) {
    return iface
        .getAllConcreteAddresses6()
        .stream()
        .anyMatch(
            address ->
                address.getPrefix()
                    .contains(ip));
  }

  private static void addTerminal(
      List<Ipv6Trace> traces,
      List<Ipv6TraceHop> hops,
      String node,
      String vrf,
      Ipv6TraceDisposition disposition) {
    List<Ipv6TraceHop> terminal =
        new ArrayList<>(hops);
    terminal.add(
        Ipv6TraceHop.terminal(
            node, vrf));
    traces.add(
        new Ipv6Trace(
            disposition,
            terminal));
  }

  private static Map<
          Ip6, List<InterfaceLocation>>
      computeAddressOwners(
          Map<String, Configuration>
              configurations) {

    Map<Ip6, List<InterfaceLocation>>
        owners = new HashMap<>();

    configurations.forEach(
        (node, configuration) ->
            configuration
                .getAllInterfaces()
                .values()
                .stream()
                .filter(Interface::getActive)
                .forEach(
                    iface ->
                        iface
                            .getAllConcreteAddresses6()
                            .forEach(
                                address ->
                                    owners
                                        .computeIfAbsent(
                                            address.getIp(),
                                            ignored ->
                                                new ArrayList<>())
                                        .add(
                                            new InterfaceLocation(
                                                node,
                                                iface.getVrfName(),
                                                iface.getName())))));

    return owners;
  }

  private static Map<String, Map<String, Fib6>>
      deepCopyFibs(
          Map<String, Map<String, Fib6>> fibs) {

    ImmutableMap.Builder<
            String, Map<String, Fib6>>
        outer = ImmutableMap.builder();

    fibs.forEach(
        (node, vrfs) ->
            outer.put(
                node,
                ImmutableMap.copyOf(vrfs)));

    return outer.build();
  }

  private final @Nonnull
      Map<Ip6, List<InterfaceLocation>>
          _addressOwners;
  private final @Nonnull
      Map<String, Configuration>
          _configurations;
  private final @Nonnull
      Map<String, Map<String, Fib6>>
          _fibs;
}
