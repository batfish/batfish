package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;

/**
 * Records information about a {@link Hop trace hop} needed to build a {@link
 * org.batfish.common.traceroute.TraceDag}.
 */
public final class HopInfo {
  private final Hop _hop;
  // Flow as it entered the hop
  private final Flow _initialFlow;
  private final @Nullable FlowDisposition _disposition;
  private final @Nullable Flow _returnFlow;
  private final @Nullable FirewallSessionTraceInfo _firewallSessionTraceInfo;
  private final @Nullable Breadcrumb _loopDetectedBreadcrumb;
  private final @Nullable Breadcrumb _visitedBreadcrumb;

  static HopInfo successHop(
      Hop hop,
      Flow initialFlow,
      FlowDisposition successDisposition,
      Flow returnFlow,
      @Nullable FirewallSessionTraceInfo firewallSessionTraceInfo,
      @Nullable Breadcrumb visitedBreadcrumb) {
    checkArgument(successDisposition.isSuccessful());
    return new HopInfo(
        hop,
        initialFlow,
        successDisposition,
        returnFlow,
        firewallSessionTraceInfo,
        null,
        visitedBreadcrumb);
  }

  static HopInfo failureHop(
      Hop hop,
      Flow initialFlow,
      FlowDisposition failureDisposition,
      @Nullable FirewallSessionTraceInfo firewallSessionTraceInfo,
      @Nullable Breadcrumb visitedBreadcrumb) {
    checkArgument(!failureDisposition.isSuccessful());
    checkArgument(failureDisposition != FlowDisposition.LOOP);
    return new HopInfo(
        hop,
        initialFlow,
        failureDisposition,
        null,
        firewallSessionTraceInfo,
        null,
        visitedBreadcrumb);
  }

  static HopInfo loopHop(
      Hop hop,
      Flow initialFlow,
      Breadcrumb loopDetectedBreadcrumb,
      @Nullable FirewallSessionTraceInfo firewallSessionTraceInfo) {
    return new HopInfo(
        hop,
        initialFlow,
        FlowDisposition.LOOP,
        null,
        firewallSessionTraceInfo,
        loopDetectedBreadcrumb,
        null);
  }

  static HopInfo forwardedHop(
      Hop hop,
      Flow initialFlow,
      Breadcrumb visitedBreadcrumb,
      @Nullable FirewallSessionTraceInfo firewallSessionTraceInfo) {
    return new HopInfo(
        hop, initialFlow, null, null, firewallSessionTraceInfo, null, visitedBreadcrumb);
  }

  private HopInfo(
      Hop hop,
      Flow initialFlow,
      @Nullable FlowDisposition disposition,
      @Nullable Flow returnFlow,
      @Nullable FirewallSessionTraceInfo firewallSessionTraceInfo,
      @Nullable Breadcrumb loopDetectedBreadcrumb,
      @Nullable Breadcrumb visitedBreadcrumb) {
    checkArgument(
        loopDetectedBreadcrumb == null || visitedBreadcrumb == null,
        "Cannot have loopDetectBreadcrumb and visitedBreadcrumb");
    checkArgument(
        (disposition != null && disposition.isSuccessful()) == (returnFlow != null),
        "return flow should be present if and only if the hop has a successful disposition");
    _disposition = disposition;
    _firewallSessionTraceInfo = firewallSessionTraceInfo;
    _hop = hop;
    _initialFlow = initialFlow;
    _returnFlow = returnFlow;
    _loopDetectedBreadcrumb = loopDetectedBreadcrumb;
    _visitedBreadcrumb = visitedBreadcrumb;
  }

  public Hop getHop() {
    return _hop;
  }

  Flow getInitialFlow() {
    return _initialFlow;
  }

  @Nullable
  Breadcrumb getLoopDetectedBreadcrumb() {
    return _loopDetectedBreadcrumb;
  }

  @Nullable
  Breadcrumb getVisitedBreadcrumb() {
    return _visitedBreadcrumb;
  }

  /** Returns the return flow of this hop, if the trace ends here. */
  @Nullable
  Flow getReturnFlow() {
    return _returnFlow;
  }

  @Nullable
  FirewallSessionTraceInfo getFirewallSessionTraceInfo() {
    return _firewallSessionTraceInfo;
  }

  @Nullable
  public FlowDisposition getDisposition() {
    return _disposition;
  }
}
