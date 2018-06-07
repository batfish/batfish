package org.batfish.allinone;

import static org.batfish.datamodel.matchers.FlowHistoryInfoMatchers.hasFlow;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.ForwardingAnalysisImpl;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.specifiers.SpecifiersReachabilityAnswerer;
import org.batfish.question.specifiers.SpecifiersReachabilityQuestion;
import org.batfish.specifier.AllInterfacesLocationSpecifierFactory;
import org.batfish.specifier.NodeNameRegexInterfaceLocationSpecifierFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end SpecifiersReachabilityQuestion tests. */
public class SpecifiersReachabilityQuestionTest {
  private static final String TESTRIGS_PREFIX = "org/batfish/grammar/cisco/testrigs/";
  private static final String TESTRIG_NAME = "ios-default-originate";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of("listener", "originator");
  private static final Ip ORIGINATOR_IP = new Ip("1.1.1.2");
  private static final Ip LISTENER_IP = new Ip("1.1.1.3");

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);

    _batfish.computeDataPlane(false);
  }

  @Test
  public void testForwardingAnalysis() {
    Ip ip = new Ip("5.5.5.5");
    DataPlane dataPlane = _batfish.loadDataPlane();
    ForwardingAnalysisImpl forwardingAnalysis =
        new ForwardingAnalysisImpl(
            _batfish.loadConfigurations(),
            dataPlane.getRibs(),
            dataPlane.getFibs(),
            dataPlane.getTopology());
    assertThat(forwardingAnalysis.getRoutableIps().get("listener").get("default"), containsIp(ip));
    assertThat(
        forwardingAnalysis.getNullRoutedIps().get("listener").get("default"), not(containsIp(ip)));
    assertThat(
        forwardingAnalysis.getArpReplies().get("originator").get("FastEthernet0/0"),
        containsIp(ip));
    assertThat(
        forwardingAnalysis
            .getNeighborUnreachable()
            .get("listener")
            .get("default")
            .get("FastEthernet0/0"),
        not(containsIp(ip)));
    // should have the invariant: neighborUnreachable(iface) <=> not(one of iface's edges returns
    // arpTrue)
    ImmutableList<Edge> listenerEdges =
        forwardingAnalysis
            .getArpTrueEdge()
            .keySet()
            .stream()
            .filter(edge -> edge.getNode1().equals("listener"))
            .collect(ImmutableList.toImmutableList());
    assertThat(listenerEdges, hasSize(1));
    assertThat(
        forwardingAnalysis
            .getArpTrueEdge()
            .get(new Edge("listener", "FastEthernet0/0", "originator", "FastEthernet0/0")),
        containsIp(ip));
  }

  /**
   * Test that the results we get with the default source IpSpace specifier ({@link
   * org.batfish.specifier.InferFromLocationIpSpaceSpecifier}) are correct for the network.
   */
  @Test
  public void testInferSrcIpFromLocation() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setSourceLocationSpecifierFactory(AllInterfacesLocationSpecifierFactory.NAME);
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode("listener"), hasSrcIp(LISTENER_IP)))));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode("originator"), hasSrcIp(ORIGINATOR_IP)))));
  }

  /**
   * Test that the results we get with the default source IpSpace specifier ({@link
   * org.batfish.specifier.InferFromLocationIpSpaceSpecifier}) are correct for the network.
   */
  @Ignore
  @Test
  public void testDrop() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    // question.setSourceLocationSpecifierFactory(AllInterfacesLocationSpecifierFactory.NAME);
    question.setSourceLocationSpecifierFactory(NodeNameRegexInterfaceLocationSpecifierFactory.NAME);
    question.setSourceLocationSpecifierInput("listener");
    question.setActions(
        ImmutableSortedSet.of(
            ForwardingAction.DROP_NULL_ROUTE,
            ForwardingAction.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK));
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode("listener"),
                    hasSrcIp(LISTENER_IP),
                    not(hasDstIp(ORIGINATOR_IP))))));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode("originator"),
                    hasSrcIp(ORIGINATOR_IP),
                    not(hasDstIp(LISTENER_IP))))));
  }

  /**
   * The {@link HeaderSpace} constraint is extra -- if it conflicts with the source IpSpace
   * constraint, no flows will be found.
   */
  @Test
  public void testConflictWithHeaderSpaceConstraint() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setHeaderSpace(HeaderSpace.builder().setSrcIps(new Ip("5.5.5.5").toIpSpace()).build());
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(0));
  }

  /**
   * Test that a different source IpSpace specifier produces different source IPs. If an input is
   * given without a factory, {@link SpecifiersReachabilityQuestion} uses {@link
   * org.batfish.specifier.ConstantWildcardSetIpSpaceSpecifierFactory} by default.
   */
  @Test
  public void testConstantWildcard() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setSourceIpSpaceSpecifierInput("5.5.5.5");
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode("listener"), hasSrcIp(new Ip("5.5.5.5"))))));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode("originator"), hasSrcIp(new Ip("5.5.5.5"))))));
  }

  /**
   * If we forbid transiting any node, then we should only get 0-hop flows (i.e. traffic sent from a
   * node to itself).
   */
  @Test
  public void testForbiddenTransitNodes() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setSourceLocationSpecifierFactory(AllInterfacesLocationSpecifierFactory.NAME);
    question.setForbiddenTransitNodesNodeSpecifierInput(".*");
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(hasIngressNode("listener"), hasSrcIp(LISTENER_IP), hasDstIp(LISTENER_IP)))));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode("originator"),
                    hasSrcIp(ORIGINATOR_IP),
                    hasDstIp(ORIGINATOR_IP)))));
  }

  /**
   * If we require some node is transited, then we should only get 1-hop flows (i.e. traffic sent
   * from one node to the other).
   */
  @Test
  public void testRequiredTransitNodes() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setSourceLocationSpecifierFactory(AllInterfacesLocationSpecifierFactory.NAME);
    question.setRequiredTransitNodesNodeSpecifierInput(".*");
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode("listener"), hasSrcIp(LISTENER_IP), hasDstIp(ORIGINATOR_IP)))));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode("originator"),
                    hasSrcIp(ORIGINATOR_IP),
                    hasDstIp(LISTENER_IP)))));
  }
}
