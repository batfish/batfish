package org.batfish.z3;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.bdp.BdpDataPlanePlugin;
import org.batfish.common.Pair;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NodJobTest {

  private BdpDataPlanePlugin _bdpDataPlanePlugin;
  private SortedMap<String, Configuration> _configs;
  private DataPlane _dataPlane;
  private Configuration _dstNode;
  private Configuration _srcNode;
  private Vrf _srcVrf;
  private Synthesizer _synthesizer;

  NodJob getNodJob(HeaderSpace headerSpace) {
    ReachabilityQuerySynthesizer querySynthesizer =
        new ReachabilityQuerySynthesizer(
            ImmutableSet.of(ForwardingAction.ACCEPT),
            headerSpace,
            // finalNodes
            ImmutableSet.of(_dstNode.getHostname()),
            // ingressNodeVrfs
            ImmutableMap.of(_srcNode.getHostname(), ImmutableSet.of(_srcVrf.getName())),
            // transitNodes
            ImmutableSet.of(),
            // notTransitNodes
            ImmutableSet.of());
    SortedSet<Pair<String, String>> ingressNodes =
        ImmutableSortedSet.of(new Pair<>(_srcNode.getHostname(), _srcVrf.getName()));
    return new NodJob(new Settings(), _synthesizer, querySynthesizer, ingressNodes, "tag");
  }

  @Before
  public void setup() throws IOException {
    setupConfigs();
    setupDataPlane();
    setupSynthesizer();
  }

  private void setupConfigs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    IpAccessList.Builder aclb = nf.aclBuilder();
    IpAccessListLine.Builder acllb = IpAccessListLine.builder();
    SourceNat.Builder snb = SourceNat.builder();
    Vrf.Builder vb = nf.vrfBuilder();

    _srcNode = cb.build();
    _dstNode = cb.build();
    _srcVrf = vb.setOwner(_srcNode).build();
    Vrf dstVrf = vb.setOwner(_dstNode).build();
    Prefix p1 = Prefix.parse("1.0.0.0/31");
    Ip poolIp1 = new Ip("1.0.0.10");
    Ip poolIp2 = new Ip("1.0.0.11");

    // apply NAT to all packets
    IpAccessList sourceNat1Acl =
        aclb.setLines(
                ImmutableList.of(
                    acllb
                        .setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.0/32")))
                        .setAction(LineAction.ACCEPT)
                        .build()))
            .setOwner(_srcNode)
            .build();

    SourceNat sourceNat1 =
        // TODO add a test with poolIp1 to poolIp2. That will exercise the range logic,
        // which is complex and inscrutable. Consider replacing that with bv_lte and bv_gte.
        // Would be easier to understand, and Nuno says it will likely be more efficient.
        snb.setPoolIpFirst(poolIp1).setPoolIpLast(poolIp1).setAcl(sourceNat1Acl).build();
    Interface srcInterface =
        ib.setOwner(_srcNode)
            .setVrf(_srcVrf)
            .setAddress(new InterfaceAddress(p1.getStartIp(), p1.getPrefixLength()))
            .setSourceNats(ImmutableList.of(sourceNat1))
            .build();
    Interface dstInterface =
        ib.setOwner(_dstNode)
            .setVrf(dstVrf)
            .setAddress(new InterfaceAddress(p1.getEndIp(), p1.getPrefixLength()))
            .setSourceNats(ImmutableList.of())
            .build();

    // For the destination
    Prefix pDest = Prefix.parse("2.0.0.0/32");
    Interface iB =
        ib.setOwner(_dstNode)
            .setVrf(dstVrf)
            .setAddress(new InterfaceAddress(pDest.getEndIp(), pDest.getPrefixLength()))
            .build();

    StaticRoute.Builder bld = StaticRoute.builder().setNetwork(pDest);
    _srcVrf.getStaticRoutes().add(bld.setNextHopIp(p1.getEndIp()).build());

    _configs =
        ImmutableSortedMap.of(
            _srcNode.getName(), _srcNode,
            _dstNode.getName(), _dstNode);
  }

  private void setupDataPlane() throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    Batfish batfish = BatfishTestUtils.getBatfish(_configs, tmp);
    _bdpDataPlanePlugin = new BdpDataPlanePlugin();
    _bdpDataPlanePlugin.initialize(batfish);
    batfish.registerDataPlanePlugin(_bdpDataPlanePlugin, "bdp");
    batfish.computeDataPlane(false);
    _dataPlane = batfish.loadDataPlane();
  }

  private void setupSynthesizer() {
    SynthesizerInput input =
        SynthesizerInputImpl.builder().setConfigurations(_configs).setDataPlane(_dataPlane).build();
    _synthesizer = new Synthesizer(input);
  }

  /** Test that traffic originating from 3.0.0.0 is NATed */
  @Test
  public void testNatted() throws IOException {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.0")));
    NodJob nodJob = getNodJob(headerSpace);

    Context z3Context = new Context();
    SmtInput smtInput = nodJob.computeSmtInput(System.currentTimeMillis(), z3Context);

    Model model = nodJob.getSmtModel(z3Context, smtInput._expr);
    assertThat(model, notNullValue());

    Map<HeaderField, Long> headerConstraints =
        nodJob.getHeaderConstraints(model, smtInput._variablesAsConsts);

    assertThat(
        headerConstraints, hasEntry(BasicHeaderField.ORIG_SRC_IP, new Ip("3.0.0.0").asLong()));
    assertThat(
        headerConstraints,
        hasEntry(equalTo(BasicHeaderField.SRC_IP), not(equalTo(new Ip("3.0.0.0").asLong()))));
    assertThat(
        headerConstraints,
        hasEntry(
            equalTo(BasicHeaderField.SRC_IP),
            Matchers.isOneOf(new Ip("1.0.0.10").asLong(), new Ip("1.0.0.11").asLong())));

    Set<Flow> flows = nodJob.getFlows(model, headerConstraints);
    _bdpDataPlanePlugin.processFlows(flows, _dataPlane);
    List<FlowTrace> flowTraces = _bdpDataPlanePlugin.getHistoryFlowTraces(_dataPlane);

    flowTraces.forEach(
        trace -> {
          assertThat(trace.getNotes(), is("ACCEPTED"));
          List<FlowTraceHop> hops = trace.getHops();
          assertThat(hops, hasSize(1));
          FlowTraceHop hop = hops.get(0);
          assertThat(hop.getTransformedFlow(), notNullValue());
        });
  }

  /** Test that traffic originating from 3.0.0.1 is not NATed */
  @Test
  public void testNotNatted() throws IOException {
    HeaderSpace headerSpace = new HeaderSpace();
    headerSpace.setSrcIps(ImmutableList.of(new IpWildcard("3.0.0.1")));
    NodJob nodJob = getNodJob(headerSpace);

    Context z3Context = new Context();
    SmtInput smtInput = nodJob.computeSmtInput(System.currentTimeMillis(), z3Context);

    Model model = nodJob.getSmtModel(z3Context, smtInput._expr);
    assertThat(model, notNullValue());

    Map<HeaderField, Long> headerConstraints =
        nodJob.getHeaderConstraints(model, smtInput._variablesAsConsts);

    assertThat(
        headerConstraints, hasEntry(BasicHeaderField.ORIG_SRC_IP, new Ip("3.0.0.1").asLong()));
    assertThat(headerConstraints, hasEntry(BasicHeaderField.SRC_IP, new Ip("3.0.0.1").asLong()));

    Set<Flow> flows = nodJob.getFlows(model, headerConstraints);
    _bdpDataPlanePlugin.processFlows(flows, _dataPlane);
    List<FlowTrace> flowTraces = _bdpDataPlanePlugin.getHistoryFlowTraces(_dataPlane);

    flowTraces.forEach(
        trace -> {
          assertThat(trace.getNotes(), is("ACCEPTED"));
          List<FlowTraceHop> hops = trace.getHops();
          assertThat(hops, hasSize(1));
          FlowTraceHop hop = hops.get(0);
          assertThat(hop.getTransformedFlow(), nullValue());
        });
  }
}
