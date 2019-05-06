package org.batfish.dataplane.traceroute;

import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasNewFirewallSessions;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasTrace;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MockDataPlane;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.pojo.Node;
import org.junit.Test;

/** Tests for {@link FlowTracer}. */
public final class FlowTracerTest {
  @Test
  public void testBuildDeniedTraceNoNewSessions() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .setTag("tag")
            .build();

    List<TraceAndReverseFlow> traces = new ArrayList<>();

    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    FlowTracer flowTracer = new FlowTracer(ctxt, c.getHostname(), null, flow, traces::add);
    flowTracer.buildDeniedTrace(DENIED_IN);
    assertThat(
        traces,
        contains(allOf(hasTrace(hasDisposition(DENIED_IN)), hasNewFirewallSessions(empty()))));
  }

  @Test
  public void testBuildDeniedTraceNewSessions() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Flow flow =
        Flow.builder()
            .setDstIp(Ip.parse("1.1.1.1"))
            .setIngressNode(c.getHostname())
            .setIngressVrf(vrf.getName())
            .setTag("tag")
            .build();
    List<TraceAndReverseFlow> traces = new ArrayList<>();

    FirewallSessionTraceInfo sessionInfo =
        new FirewallSessionTraceInfo("hostname", null, null, ImmutableSet.of(), TRUE, null);
    TracerouteEngineImplContext ctxt =
        new TracerouteEngineImplContext(
            MockDataPlane.builder().setConfigs(ImmutableMap.of(c.getHostname(), c)).build(),
            Topology.EMPTY,
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(),
            false);
    FlowTracer flowTracer =
        new FlowTracer(
            ctxt,
            new Node(c.getHostname()),
            null,
            new Stack<>(),
            ImmutableList.of(),
            ImmutableList.of(),
            new NodeInterfacePair("node", "iface"),
            ImmutableSet.of(sessionInfo),
            flow,
            flow,
            traces::add);

    flowTracer.buildDeniedTrace(DENIED_IN);
    assertThat(
        traces,
        contains(
            allOf(
                hasTrace(hasDisposition(DENIED_IN)),
                hasNewFirewallSessions(contains(sessionInfo)))));
  }
}
