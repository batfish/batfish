package org.batfish.grammar.palo_alto;

import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.FAILURE_DISPOSITIONS;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests of bi-directional flow behavior of Palo Alto firewall under various scenarios */
public final class PaloAltoBidirectionalBehaviorTest {

  private static final Ip BIDIR_DEFAULT_DST_IP = Ip.parse("10.0.2.2");
  private static final Ip BIDIR_DEFAULT_SRC_IP = Ip.parse("10.0.1.2");
  private static final Ip BIDIR_OTHER_DST_IP = Ip.parse("10.0.2.3");
  private static final String INTERFACE1 = "ethernet1/1";
  private static final String INTERFACE2 = "ethernet1/2";
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Flow bidirForwardFlow(String hostname, Ip dstIp) {
    return Flow.builder()
        .setIpProtocol(IpProtocol.TCP)
        .setTcpFlagsSyn(1)
        .setIngressInterface(INTERFACE1)
        .setIngressNode(hostname)
        .setSrcIp(BIDIR_DEFAULT_SRC_IP)
        .setDstIp(dstIp)
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .setDstPort(NamedPort.SSH.number()) // arbitrary port
        .build();
  }

  private @Nonnull Flow bidirForwardOutsideFlow(String hostname) {
    return Flow.builder()
        .setIpProtocol(IpProtocol.TCP)
        .setTcpFlagsSyn(1)
        .setIngressInterface(INTERFACE2)
        .setIngressNode(hostname)
        .setSrcIp(BIDIR_DEFAULT_DST_IP)
        .setDstIp(BIDIR_DEFAULT_SRC_IP)
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .setDstPort(NamedPort.SSH.number()) // arbitrary port
        .build();
  }

  private @Nonnull TracerouteEngine bidirTracerouteEngine(String hostname) throws IOException {
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    return new TracerouteEngineImpl(
        dp,
        batfish.getTopologyProvider().getLayer3Topology(snapshot),
        batfish.loadConfigurations(snapshot));
  }

  private void assertForwardDropped(String hostname) throws IOException {
    assertForwardDropped(hostname, BIDIR_DEFAULT_DST_IP);
  }

  private void assertForwardDropped(String hostname, Ip dstIp) throws IOException {
    assertForwardDropped(bidirTracerouteEngine(hostname), bidirForwardFlow(hostname, dstIp));
  }

  private void assertForwardDropped(TracerouteEngine tracerouteEngine, Flow forwardFlow) {
    List<TraceAndReverseFlow> forwardTracesAndReverseFlows =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(forwardFlow), false)
            .get(forwardFlow);

    assertThat(forwardTracesAndReverseFlows, hasSize(1));
    assertThat(
        forwardTracesAndReverseFlows.iterator().next().getTrace().getDisposition(),
        in(FAILURE_DISPOSITIONS));
  }

  private void assertBidirAccepted(String hostname) throws IOException {
    assertBidirAccepted(hostname, BIDIR_DEFAULT_DST_IP);
  }

  private void assertBidirAccepted(String hostname, Ip dstIp) throws IOException {
    assertBidirAccepted(bidirTracerouteEngine(hostname), bidirForwardFlow(hostname, dstIp));
  }

  private void assertBidirAccepted(TracerouteEngine tracerouteEngine, Flow forwardFlow) {
    List<TraceAndReverseFlow> forwardTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(forwardFlow), false)
            .get(forwardFlow);

    assertThat(forwardTraces, hasSize(1));

    TraceAndReverseFlow forwardTrace = forwardTraces.iterator().next();

    assertThat(forwardTrace.getTrace().getDisposition(), equalTo(DELIVERED_TO_SUBNET));

    Flow reverseFlow = forwardTrace.getReverseFlow();

    List<TraceAndReverseFlow> reverseTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(
                ImmutableSet.of(reverseFlow), forwardTrace.getNewFirewallSessions(), false)
            .get(reverseFlow);

    assertThat(reverseTraces, hasSize(1));
    assertThat(
        reverseTraces.iterator().next().getTrace().getDisposition(), equalTo(DELIVERED_TO_SUBNET));
  }

  @Test
  public void testDropMissingVsys() throws IOException {
    String hostname = "drop-missing-vsys";

    // Cannot initiate connection through interface not in a vsys
    assertForwardDropped(hostname);
  }

  @Test
  public void testAllowSameZoneNoRules() throws IOException {
    String hostname = "allow-same-zone-no-rules";

    // Bidirectional traffic between interfaces in the same zone is allowed by default when there
    // are no security rules.
    assertBidirAccepted(hostname);
  }

  @Test
  public void testAllowSameZoneNoMatchingRules() throws IOException {
    String hostname = "allow-same-zone-no-matching-rules";

    // Bidirectional traffic between interfaces in the same zone is allowed by default if there are
    // security rules for intra-zone traffic but nothing matches.
    assertBidirAccepted(hostname);
  }

  @Test
  public void testDropSameZoneExplicit() throws IOException {
    String hostname = "drop-same-zone-explicit";

    // Traffic between interfaces in the same zone should be dropped if matched by deny rule
    assertForwardDropped(hostname);
  }

  @Test
  public void testDropDefaultCrossZone() throws IOException {
    String hostname = "drop-default-cross-zone";

    // Traffic between interfaces in the same zone should be dropped if not matched by an allow rule
    assertForwardDropped(hostname);
  }

  @Test
  public void testAllowExplicitCrossZone() throws IOException {
    String hostname = "allow-explicit-cross-zone";

    // Bidirectional traffic between interfaces in different zones should be allowed if matched by
    // allow in forward direction rule
    assertBidirAccepted(hostname);
  }

  @Test
  public void testDropInterVsysImplicit() throws IOException {
    String hostname = "drop-inter-vsys-implicit";

    // Traffic between interfaces in different vsys'es is denied without appropriate external zones
    // and policy
    assertForwardDropped(hostname);
  }

  @Test
  public void testDropInterVsysMissingExternalEgress() throws IOException {
    String hostname = "drop-inter-vsys-missing-external-egress";

    // Traffic between interfaces in different vsys'es is denied when external zone is missing from
    // egress vsys
    assertForwardDropped(hostname);
  }

  @Test
  public void testDropInterVsysMissingExternalIngress() throws IOException {
    String hostname = "drop-inter-vsys-missing-external-ingress";

    // Traffic between interfaces in different vsys'es is denied when external zone is missing from
    // ingress vsys
    assertForwardDropped(hostname);
  }

  @Test
  public void testDropInterVsysMisconfiguredExternalEgress() throws IOException {
    String hostname = "drop-inter-vsys-misconfigured-external-egress";

    // Traffic between interfaces in different vsys'es is denied when external zone on egress vsys
    // is misconfigured
    assertForwardDropped(hostname);
  }

  @Test
  public void testDropInterVsysMisconfiguredExternalIngress() throws IOException {
    String hostname = "drop-inter-vsys-misconfigured-external-ingress";

    // Traffic between interfaces in different vsys'es is denied when external zone on ingress vsys
    // is misconfigured
    assertForwardDropped(hostname);
  }

  @Test
  public void testAllowInterVsys() throws IOException {
    String hostname = "allow-inter-vsys";

    // Bidirectional traffic from interface in ingress vsys to interface in separate egress vsys is
    // allowed when all of following are true:
    // - external zones are defined in ingress and egress vsys and refer to each other's vsys'es
    // - policy allows cross-zone traffic from ingress zone to ingress external zone
    // - policy allows cross-zone traffic from egress external zone to ingress zone
    assertBidirAccepted(hostname);
  }

  @Test
  public void testDropVsysToSgMissingExternal() throws IOException {
    String hostname = "drop-vsys-to-sg-missing-external";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // Traffic from interface in vsys to interface in shared-gateway is denied when external zone is
    // missing from vsys
    assertForwardDropped(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));

    // Traffic from interface in shared-gateway to interface in vsys is denied when external zone is
    // missing from vsys
    assertForwardDropped(tracerouteEngine, bidirForwardOutsideFlow(hostname));
  }

  @Test
  public void testDropVsysToSgMisconfiguredExternal() throws IOException {
    String hostname = "drop-vsys-to-sg-misconfigured-external";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // Traffic from interface in vsys to interface in shared-gateway is denied when external zone is
    // on vsys is misconfigured
    assertForwardDropped(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));

    // Traffic from interface in shared-gateway to interface in vsys is denied when external zone is
    // on vsys is misconfigured
    assertForwardDropped(tracerouteEngine, bidirForwardOutsideFlow(hostname));
  }

  @Test
  public void testAllowVsysToSg() throws IOException {
    String hostname = "allow-vsys-to-sg";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // Bidirectional traffic from interface in vsys to interface in shared-gateway is allowed when
    // all are true:
    // - external zones is defined on vsys and refers to shared-gateway
    // - policy allows cross-zone traffic from vsys interface zone to shared-gateway zone
    assertBidirAccepted(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));

    // Bidirectional traffic from interface in shared-gateway to interface in vsys is denied when
    // policy does not allow cross-zone traffic from shared-gateway zone to vsys interface zone
    assertForwardDropped(tracerouteEngine, bidirForwardOutsideFlow(hostname));
  }

  @Test
  public void testAllowInterSg() throws IOException {
    String hostname = "allow-inter-sg";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // Bidirectional traffic from interface in shared-gateway to interface in another shared-gateway
    // is allowed.
    assertBidirAccepted(tracerouteEngine, bidirForwardOutsideFlow(hostname));
  }

  @Test
  public void testAllowIntraSg() throws IOException {
    String hostname = "allow-intra-sg";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // Bidirectional traffic from interface in shared-gateway to interface in same shared-gateway is
    // allowed.
    assertBidirAccepted(tracerouteEngine, bidirForwardOutsideFlow(hostname));
  }

  @Test
  public void testAllowSgToVsys() throws IOException {
    String hostname = "allow-sg-to-vsys";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // Bidirectional traffic from interface in shared-gateway to interface in vsys is allowed when
    // all are true:
    // - external zones is defined on vsys and refers to shared-gateway
    // - policy allows cross-zone traffic from shared-gateway zone to vsys interface zone
    assertBidirAccepted(tracerouteEngine, bidirForwardOutsideFlow(hostname));

    // Bidirectional traffic from interface in vsys to interface in shared-gateway is denied when
    // policy does not allow cross-zone traffic from vsys interface zone to shared-gateway zone
    assertForwardDropped(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));
  }

  @Test
  public void testDropIntraVsysNextVrMissingEgress() throws IOException {
    String hostname = "drop-intra-vsys-next-vr-missing-egress";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);
    Flow forwardFlow = bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP);
    List<TraceAndReverseFlow> forwardTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(forwardFlow), false)
            .get(forwardFlow);

    assertThat(forwardTraces, hasSize(1));

    TraceAndReverseFlow forwardTrace = forwardTraces.iterator().next();

    // forward flow should be delivered due to static route with next-vr in forward direction
    assertThat(forwardTrace.getTrace().getDisposition(), equalTo(DELIVERED_TO_SUBNET));

    Flow reverseFlow = forwardTrace.getReverseFlow();

    List<TraceAndReverseFlow> reverseTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(
                ImmutableSet.of(reverseFlow), forwardTrace.getNewFirewallSessions(), false)
            .get(reverseFlow);

    assertThat(reverseTraces, hasSize(1));
    // reverse flow should be dropped due to missing reverse route
    assertThat(reverseTraces.iterator().next().getTrace().getDisposition(), equalTo(NO_ROUTE));
  }

  @Test
  public void testDropIntraVsysNextVrMissingIngress() throws IOException {
    String hostname = "drop-intra-vsys-next-vr-missing-ingress";

    // forward flow should be dropped due to missing next-vr route in ingress virtual-router
    assertForwardDropped(hostname);
  }

  @Test
  public void testAllowIntraVsysNextVr() throws IOException {
    String hostname = "allow-intra-vsys-next-vr";

    // Bidirectional traffic between interfaces in different virtual-routers is allowed when:
    // - (forward traffic) route in ingress virtual-router has next-vr statement pointing to egress
    // virtual-router for (forward) dst-ip
    // - (reverse traffic) route in egress virtual-router has next-vr statement pointing to ingress
    // virtual-router for (forward) src-ip
    assertBidirAccepted(hostname);
  }

  @Test
  public void testAllowVsysToSgNextVr() throws IOException {
    String hostname = "allow-vsys-to-sg-next-vr";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // next-vr should work for properly configured vsys-to-shared-gateway traffic
    assertBidirAccepted(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));
    // next-vr should work for properly configured shared-gateway-to-vsys traffic
    assertBidirAccepted(tracerouteEngine, bidirForwardOutsideFlow(hostname));
  }

  @Test
  public void testAllowInterVsysNextVr() throws IOException {
    String hostname = "allow-inter-vsys-next-vr";

    // next-vr should work for properly configured inter-vsys traffic
    assertBidirAccepted(hostname);
  }

  @Test
  public void testMatchSharedAddress() throws IOException {
    String hostname = "match-shared-address";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // policy allows only BIDIR_DEFAULT_DST_IP via shared address
    assertBidirAccepted(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));
    assertForwardDropped(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_OTHER_DST_IP));
  }

  @Test
  public void testMatchVsysAddress() throws IOException {
    String hostname = "match-vsys-address";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // policy allows only BIDIR_DEFAULT_DST_IP via vsys address
    assertBidirAccepted(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));
    assertForwardDropped(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_OTHER_DST_IP));
  }

  @Test
  public void testMatchVsysAddressOverShared() throws IOException {
    String hostname = "match-vsys-address-over-shared";
    TracerouteEngine tracerouteEngine = bidirTracerouteEngine(hostname);

    // - shared address a1 only allows BIDIR_OTHER_DST_IP
    // - vsys address a1 only allows BIDIR_DEFAULT_DST_IP
    // - policy on vsys refers to a1; vsys address definition should take priority
    assertBidirAccepted(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_DEFAULT_DST_IP));
    assertForwardDropped(tracerouteEngine, bidirForwardFlow(hostname, BIDIR_OTHER_DST_IP));
  }
}
