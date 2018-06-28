package org.batfish.z3;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.matchers.FlowHistoryInfoMatchers.hasFlow;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedMap;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Batfish;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.IntersectionLocationSpecifier;
import org.batfish.specifier.NameRegexInterfaceLocationSpecifier;
import org.batfish.specifier.NameRegexNodeSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLocationSpecifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ReducedReachabilityTest {
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final Ip NODE1_LOOPBACK_IP = new Ip("1.1.1.1");
  private static final String NODE2 = "node2";
  private static final Ip NODE2_ALTERNATE_IP = new Ip("2.2.2.2");
  private static final String PHYSICAL = "FastEthernet0/0";

  private Builder _cb;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private org.batfish.datamodel.Interface.Builder _ib;

  private NetworkFactory _nf;

  private org.batfish.datamodel.Vrf.Builder _vb;

  private SortedMap<String, Configuration> generateConfigs(boolean delta) {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf v1 = _vb.setOwner(node1).build();
    _ib.setOwner(node1).setVrf(v1);
    _ib.setName(LOOPBACK).setAddresses(new InterfaceAddress("1.1.1.1/32")).build();
    _ib.setName(PHYSICAL).setAddresses(new InterfaceAddress("1.1.1.2/31")).build();
    if (!delta) {
      v1.setStaticRoutes(
          ImmutableSortedSet.of(
              StaticRoute.builder()
                  .setNetwork(Prefix.parse("2.2.2.2/32"))
                  .setNextHopInterface(PHYSICAL)
                  .build()));
    }

    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf v2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2).setVrf(v2);
    _ib.setName(PHYSICAL)
        .setAddresses(new InterfaceAddress("1.1.1.3/31"), new InterfaceAddress("2.2.2.2/32"))
        .build();
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("1.1.1.1/32"))
                .setNextHopInterface(PHYSICAL)
                .build()));

    return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  private Batfish initBatfish() throws IOException {
    SortedMap<String, Configuration> baseConfigs = generateConfigs(false);
    SortedMap<String, Configuration> deltaConfigs = generateConfigs(true);
    Batfish batfish = getBatfish(baseConfigs, deltaConfigs, _folder);

    batfish.pushBaseEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    batfish.pushDeltaEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    return batfish;
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    _ib = _nf.interfaceBuilder().setActive(true);
  }

  /** Test that we detect reduced reachability caused by a removed static route. */
  @Test
  public void testStaticRouteRemoved() throws IOException {
    Batfish batfish = initBatfish();
    AnswerElement answer =
        batfish.reducedReachability(
            ReachabilityParameters.builder()
                .setActions(ImmutableSortedSet.of(ForwardingAction.ACCEPT))
                .setFinalNodesSpecifier(new NameRegexNodeSpecifier(Pattern.compile(NODE2)))
                .setHeaderSpace(
                    HeaderSpace.builder().setDstIps(NODE2_ALTERNATE_IP.toIpSpace()).build())
                .setSourceSpecifier(
                    /* Source = loopback0 on node1 */
                    new IntersectionLocationSpecifier(
                        new NodeNameRegexInterfaceLocationSpecifier(Pattern.compile(NODE1)),
                        new NameRegexInterfaceLocationSpecifier(Pattern.compile(LOOPBACK))))
                .build());
    assertThat(answer, instanceOf(FlowHistory.class));
    FlowHistory flowHistory = (FlowHistory) answer;
    assertThat(flowHistory.getTraces().entrySet(), hasSize(1));
    assertThat(
        flowHistory.getTraces().values(),
        contains(hasFlow(allOf(hasSrcIp(NODE1_LOOPBACK_IP), hasDstIp(NODE2_ALTERNATE_IP)))));
  }
}
