package org.batfish.grammar.cisco;

import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.FAILURE_DISPOSITIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.junit.Assert.assertFalse;
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

/** Tests of bi-directional flow behavior of Cisco firewall under various scenarios */
public final class CiscoBidirectionalBehaviorTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull TracerouteEngine bidirTracerouteEngine(String hostname) throws IOException {
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    return new TracerouteEngineImpl(dp, batfish.getTopologyProvider().getLayer3Topology(snapshot));
  }

  /** Helper function that asserts specified forward flow is dropped. */
  private static void assertForwardDropped(TracerouteEngine tracerouteEngine, Flow forwardFlow) {
    List<TraceAndReverseFlow> forwardTracesAndReverseFlows =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(forwardFlow), false)
            .get(forwardFlow);

    assertThat(forwardTracesAndReverseFlows, hasSize(1));
    assertThat(
        forwardTracesAndReverseFlows.iterator().next().getTrace().getDisposition(),
        in(FAILURE_DISPOSITIONS));
  }

  /**
   * Helper function that asserts specified forward flow succeeds. Returns the corresponding trace
   * and reverse flow.
   */
  private static TraceAndReverseFlow assertForwardAccepted(
      TracerouteEngine tracerouteEngine, Flow forwardFlow) {
    List<TraceAndReverseFlow> forwardTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(forwardFlow), false)
            .get(forwardFlow);

    assertThat(forwardTraces, hasSize(1));

    TraceAndReverseFlow forwardTrace = forwardTraces.iterator().next();

    assertThat(forwardTrace.getTrace().getDisposition(), equalTo(DELIVERED_TO_SUBNET));

    return forwardTrace;
  }

  /**
   * Helper function that asserts specified forward flow and corresponding return flow both succeed.
   */
  private static void assertBidirAccepted(TracerouteEngine tracerouteEngine, Flow forwardFlow) {
    TraceAndReverseFlow forwardTrace = assertForwardAccepted(tracerouteEngine, forwardFlow);
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

  /**
   * Helper function that asserts the forward flow succeeds but the reverse flow fails when it has
   * the specified reverseFlow ingressInterface name.
   */
  private static void assertBidirDropped(
      TracerouteEngine tracerouteEngine,
      Flow forwardFlow,
      String reverseFlowIngressInterfaceOverride) {
    TraceAndReverseFlow forwardTrace = assertForwardAccepted(tracerouteEngine, forwardFlow);

    Flow reverseFlow =
        forwardTrace
            .getReverseFlow()
            .toBuilder()
            .setIngressInterface(reverseFlowIngressInterfaceOverride)
            .build();

    List<TraceAndReverseFlow> reverseTraces =
        tracerouteEngine
            .computeTracesAndReverseFlows(
                ImmutableSet.of(reverseFlow), forwardTrace.getNewFirewallSessions(), false)
            .get(reverseFlow);

    assertThat(reverseTraces, hasSize(1));
    assertFalse(reverseTraces.iterator().next().getTrace().getDisposition().isSuccessful());
  }

  /** Test that a return flow traversing the same interfaces as the initial flow is successful. */
  @Test
  public void testEstablishedSessionSameInterface() throws IOException {
    String hostname = "asa-session";

    String insideIface = "inside"; // 1.1.1.1/24
    String outsideIface = "outside"; // 2.2.2.2/24

    Flow.Builder baseFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(1)
            .setIngressNode(hostname)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            // Arbitrary port
            .setDstPort(NamedPort.SSH.number());

    Flow outsideInitialFlow =
        baseFlow
            .setIngressInterface(outsideIface)
            .setSrcIp(Ip.parse("2.2.2.11"))
            .setDstIp(Ip.parse("1.1.1.11"))
            .build();

    Flow insideInitialFlow =
        baseFlow
            .setIngressInterface(insideIface)
            .setSrcIp(Ip.parse("1.1.1.11"))
            .setDstIp(Ip.parse("2.2.2.11"))
            .build();

    // Cannot initiate connection from outside interface
    assertForwardDropped(bidirTracerouteEngine(hostname), outsideInitialFlow);
    // Bidirectional flow should be accepted, originating from inside interface
    assertBidirAccepted(bidirTracerouteEngine(hostname), insideInitialFlow);
  }

  /** Test that a return flow taking a different path than initial flow is not successful. */
  @Test
  public void testEstablishedSessionDifferentInterface() throws IOException {
    String hostname = "asa-session";

    // This is the ingress iface for the initial flow
    String insideIface = "inside"; // 1.1.1.1/24
    // This is the egress iface for the initial flow
    String outsideIface = "outside"; // 2.2.2.2/24

    // This is the ingress iface for the return flow
    String otherIface = "other"; // 3.3.3.3/24

    Flow.Builder baseFlow =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(1)
            .setIngressNode(hostname)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            // Arbitrary port
            .setDstPort(NamedPort.SSH.number());

    Flow initialFlow =
        baseFlow
            .setIngressInterface(insideIface)
            .setSrcIp(Ip.parse("1.1.1.11"))
            .setDstIp(Ip.parse("2.2.2.11"))
            .build();

    // Return flow should fail when coming from different interface than session was setup for
    assertBidirDropped(bidirTracerouteEngine(hostname), initialFlow, otherIface);
  }
}
