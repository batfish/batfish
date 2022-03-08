package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.SUCCESS_DISPOSITIONS;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.InboundStep;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.tracking.TrackReachability;
import org.batfish.datamodel.visitors.FibActionVisitor;

/** Utilities for evaluating {@link org.batfish.datamodel.tracking.TrackReachability}. */
public final class TrackReachabilityUtils {

  /**
   * Evaluate a {@link TrackReachability} given an originating node, its current {@link Fib}s, and a
   * {@link TracerouteEngine} based on the current dataplane FIBs.
   *
   * <p>The originating node's FIBs and the starting VRF in the provided {@link TrackReachability}
   * are used to determine potential source IPs for the test flow(s).
   */
  public static boolean evaluateTrackReachability(
      TrackReachability trackReachability,
      Configuration c,
      Map<String, Fib> fibsByVrf,
      TracerouteEngine tracerouteEngine) {
    Ip dstIp = trackReachability.getDestinationIp();
    return (trackReachability.getSourceIp() == null
            ? getPotentialSrcIpsAndVrfs(dstIp, trackReachability.getSourceVrf(), fibsByVrf, c)
            : Stream.of(
                Maps.immutableEntry(
                    trackReachability.getSourceIp(), trackReachability.getSourceVrf())))
        .anyMatch(
            ipAndVrf ->
                producesSuccessfulForwardAndReverseTraces(
                    ipAndVrf.getKey(),
                    trackReachability.getDestinationIp(),
                    c,
                    ipAndVrf.getValue(),
                    tracerouteEngine));
  }

  private static boolean producesSuccessfulForwardAndReverseTraces(
      Ip srcIp,
      Ip dstIp,
      Configuration originatingNode,
      String originatingVrf,
      TracerouteEngine tracerouteEngine) {
    Flow flow =
        Flow.builder()
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setIngressNode(originatingNode.getHostname())
            .setIngressVrf(originatingVrf)
            // TODO: support other flow types
            .setIpProtocol(IpProtocol.ICMP)
            .setIcmpType(IcmpType.ECHO_REQUEST)
            .setIcmpCode(0)
            .build();
    Map<Flow, List<TraceAndReverseFlow>> tracerouteResult =
        tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flow), false);
    List<TraceAndReverseFlow> traceAndReverseFlows = tracerouteResult.get(flow);
    return traceAndReverseFlows != null
        && traceAndReverseFlows.stream()
            .filter(TrackReachabilityUtils::isSuccessfulForwardFlow)
            .map(
                // Go backward direction
                tr -> {
                  assert tr.getReverseFlow() != null; // guaranteed by isSuccessfulFlow
                  return tracerouteEngine
                      .computeTracesAndReverseFlows(
                          ImmutableSet.of(tr.getReverseFlow()), tr.getNewFirewallSessions(), false)
                      .get(tr.getReverseFlow());
                })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .anyMatch(
                reverseTrace ->
                    isSuccessfulReverseFlow(reverseTrace, originatingNode, originatingVrf));
  }

  private static boolean isSuccessfulForwardFlow(TraceAndReverseFlow tr) {
    return SUCCESS_DISPOSITIONS.contains(tr.getTrace().getDisposition())
        && tr.getReverseFlow() != null;
  }

  private static boolean isSuccessfulReverseFlow(
      TraceAndReverseFlow reverseTrace, Configuration forwardSender, String forwardSenderVrf) {
    if (reverseTrace.getTrace().getDisposition() != ACCEPTED) {
      return false;
    }
    List<Hop> reverseHops = reverseTrace.getTrace().getHops();
    Hop finalReverseHop = reverseHops.get(reverseHops.size() - 1);
    if (!finalReverseHop.getNode().getName().equals(forwardSender.getHostname())) {
      return false;
    }
    return finalReverseHop.getSteps().stream()
        .anyMatch(
            step -> {
              if (!(step instanceof InboundStep)) {
                return false;
              }
              String inboundInterface = ((InboundStep) step).getDetail().getInterface();
              return forwardSender
                  .getAllInterfaces()
                  .get(inboundInterface)
                  .getVrfName()
                  .equals(forwardSenderVrf);
            });
  }

  /**
   * Returns the potential source IPs and corresponding originating VRFs for flows with the given
   * {@code dstIp} originating on the given {@link Configuration} with the given {@code fibs}
   * indexed by vrf, with the destination IP initially resolved in the given {@code
   * initialSenderVrf}, Concretely, finds LPM routes for {@code dstIp} and returns the IPs and VRFs
   * of those routes' forwarding interfaces.
   */
  private static @Nonnull Stream<Entry<Ip, String>> getPotentialSrcIpsAndVrfs(
      Ip dstIp, String initialSenderVrf, Map<String, Fib> fibs, Configuration c) {
    return getForwardingInterfaces(dstIp, initialSenderVrf, fibs)
        .map(forwardingIfaceName -> c.getActiveInterfaces().get(forwardingIfaceName))
        .filter(i -> i.getConcreteAddress() != null)
        .map(i -> Maps.immutableEntry(i.getConcreteAddress().getIp(), i.getVrfName()));
  }

  private static @Nonnull Stream<String> getForwardingInterfaces(
      Ip dstIp, String currentVrf, Map<String, Fib> fibs) {
    return fibs.get(currentVrf).get(dstIp).stream()
        .map(FibEntry::getAction)
        .flatMap(
            action ->
                action.accept(
                    new FibActionVisitor<Stream<String>>() {
                      @Override
                      public Stream<String> visitFibForward(FibForward fibForward) {
                        return Stream.of(fibForward.getInterfaceName());
                      }

                      @Override
                      public Stream<String> visitFibNextVrf(FibNextVrf fibNextVrf) {
                        // assumption: next-vrf cycles not allowed
                        String nextVrf = fibNextVrf.getNextVrf();
                        assert fibs.containsKey(nextVrf);
                        return getForwardingInterfaces(dstIp, fibNextVrf.getNextVrf(), fibs);
                      }

                      @Override
                      public Stream<String> visitFibNullRoute(FibNullRoute fibNullRoute) {
                        return Stream.of();
                      }
                    }));
  }

  private TrackReachabilityUtils() {}
}
