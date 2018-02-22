package org.batfish.z3;

import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.SortedSet;
import org.batfish.common.Pair;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.TestDataPlane;
import org.batfish.datamodel.Vrf;
import org.junit.Before;
import org.junit.Test;

public class NodJobTest {

  private IpAccessList.Builder _aclb;

  private IpAccessListLine.Builder _acllb;

  private Configuration.Builder _cb;

  private Interface.Builder _ib;

  private SynthesizerInputImpl.Builder _inputBuilder;

  private NetworkFactory _nf;

  private SourceNat.Builder _snb;

  private Vrf.Builder _vb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder();
    _ib = _nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    _aclb = _nf.aclBuilder();
    _acllb = IpAccessListLine.builder();
    _inputBuilder = SynthesizerInputImpl.builder();
    _snb = SourceNat.builder();
  }

  @Test
  public void testSimpleNatNetwork() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Ip ip11 = new Ip("1.0.0.0");
    Ip ip12 = new Ip("1.0.0.10");
    Ip ip2 = new Ip("2.0.0.0");
    IpAccessList sourceNat1Acl = _aclb.setLines(ImmutableList.of()).setOwner(srcNode).build();
    SourceNat sourceNat1 =
        _snb.setPoolIpFirst(ip11).setPoolIpLast(ip12).setAcl(sourceNat1Acl).build();
    Interface srcInterfaceOneSourceNat =
        _ib.setOwner(srcNode)
            .setVrf(srcVrf)
            .setAddress(new InterfaceAddress(ip11, 32))
            .setSourceNats(ImmutableList.of(sourceNat1))
            .build();
    Interface nextHopInterface =
        _ib.setOwner(nextHop)
            .setVrf(nextHopVrf)
            .setAddress(new InterfaceAddress(ip2, 32))
            .setSourceNats(ImmutableList.of())
            .build();
    Edge forwardEdge1 = new Edge(srcInterfaceOneSourceNat, nextHopInterface);
    Edge backEdge1 = new Edge(nextHopInterface, srcInterfaceOneSourceNat);
    SynthesizerInput input =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getName(), srcNode, nextHop.getName(), nextHop))
            .setDataPlane(
                TestDataPlane.builder()
                    .setTopologyEdges(ImmutableSortedSet.of(forwardEdge1, backEdge1))
                    .build())
            .build();

    Synthesizer synthesizer = new Synthesizer(input);
    ReachabilityQuerySynthesizer querySynthesizer =
        new ReachabilityQuerySynthesizer(
            ImmutableSet.of(ForwardingAction.ACCEPT),
            new HeaderSpace(),
            // finalNodes
            ImmutableSet.of(nextHop.getHostname()),
            // ingressNodeVrfs
            ImmutableMap.of(srcNode.getHostname(), ImmutableSet.of(srcVrf.getName())),
            // transitNodes
            ImmutableSet.of(),
            // notTransitNodes
            ImmutableSet.of());
    SortedSet<Pair<String, String>> nodeVrfs =
        ImmutableSortedSet.of(new Pair<>(srcNode.getHostname(), srcVrf.getName()));
    NodJob nodJob = new NodJob(new Settings(), synthesizer, querySynthesizer, nodeVrfs, "tag");

    Context z3Context = new Context();
    NodProgram nodProgram = nodJob.getNodProgram(z3Context);
    BoolExpr expr = nodJob.computeSmtConstraintsViaNod(nodProgram, false);

    assertFalse(expr.isFalse());
  }
}
