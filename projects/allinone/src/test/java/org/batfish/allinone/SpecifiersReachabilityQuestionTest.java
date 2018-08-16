package org.batfish.allinone;

import static org.batfish.datamodel.matchers.FlowHistoryInfoMatchers.hasFlow;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.specifiers.SpecifiersReachabilityAnswerer;
import org.batfish.question.specifiers.SpecifiersReachabilityQuestion;
import org.batfish.specifier.AllInterfacesLocationSpecifierFactory;
import org.batfish.specifier.ConstantUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.NameRegexInterfaceLocationSpecifierFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end SpecifiersReachabilityQuestion tests. */
public class SpecifiersReachabilityQuestionTest {
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final Ip NODE1_FAST_ETHERNET_IP = new Ip("1.1.1.2");
  private static final Ip NODE1_LOOPBACK_IP = new Ip("1.1.1.1");
  private static final Ip NODE2_FAST_ETHERNET_IP = new Ip("1.1.1.3");
  private static final Ip NODE2_LOOPBACK_IP = new Ip("2.2.2.2");
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "specifiers-reachability";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);

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

  /**
   * Test that the results we get with the default source IpSpace specifier ({@link
   * org.batfish.specifier.InferFromLocationIpSpaceSpecifier}) are correct for the network.
   */
  @Test
  public void testInferSrcIpFromLocation() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setDestinationIpSpaceSpecifierFactory(ConstantUniverseIpSpaceSpecifierFactory.NAME);
    question.setSourceLocationSpecifierFactory(NameRegexInterfaceLocationSpecifierFactory.NAME);
    question.setSourceLocationSpecifierInput(LOOPBACK);
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode(NODE1), hasSrcIp(NODE1_LOOPBACK_IP)))));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode(NODE2), hasSrcIp(NODE2_LOOPBACK_IP)))));
  }

  /**
   * Test that we get a result with DROP disposition from each interface. With the default source
   * IpSpace specifier, the srcIp should be that of the source interface. The dstIp should not be
   * one of those configured in the network.
   */
  @Test
  public void testDrop() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setDestinationIpSpaceSpecifierFactory(ConstantUniverseIpSpaceSpecifierFactory.NAME);
    question.setSourceLocationSpecifierFactory(NameRegexInterfaceLocationSpecifierFactory.NAME);
    question.setSourceLocationSpecifierInput(LOOPBACK);
    question.setActions(ImmutableSortedSet.of(ForwardingAction.DROP));
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode(NODE1),
                    hasSrcIp(NODE1_LOOPBACK_IP),
                    not(hasDstIp(NODE1_FAST_ETHERNET_IP)),
                    not(hasDstIp(NODE1_LOOPBACK_IP)),
                    not(hasDstIp(NODE2_FAST_ETHERNET_IP)),
                    not(hasDstIp(NODE2_LOOPBACK_IP))))));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode(NODE2),
                    hasSrcIp(NODE2_LOOPBACK_IP),
                    not(hasDstIp(NODE1_FAST_ETHERNET_IP)),
                    not(hasDstIp(NODE1_LOOPBACK_IP)),
                    not(hasDstIp(NODE2_FAST_ETHERNET_IP)),
                    not(hasDstIp(NODE2_LOOPBACK_IP))))));
  }

  /**
   * Test that a different source IpSpace specifier produces different source IPs. If an input is
   * given without a factory, {@link SpecifiersReachabilityQuestion} uses {@link
   * org.batfish.specifier.ConstantWildcardSetIpSpaceSpecifierFactory} by default.
   */
  @Test
  public void testConstantWildcard() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setDestinationIpSpaceSpecifierFactory(ConstantUniverseIpSpaceSpecifierFactory.NAME);
    question.setSourceIpSpaceSpecifierInput("5.5.5.5");
    AnswerElement answer = new SpecifiersReachabilityAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(FlowHistory.class));
    Collection<FlowHistoryInfo> flowHistoryInfos = ((FlowHistory) answer).getTraces().values();
    assertThat(flowHistoryInfos, hasSize(2));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode(NODE1), hasSrcIp(new Ip("5.5.5.5"))))));
    assertThat(
        flowHistoryInfos,
        hasItem(hasFlow(allOf(hasIngressNode(NODE2), hasSrcIp(new Ip("5.5.5.5"))))));
  }

  /**
   * If we forbid transiting any node, then we should only get 0-hop flows (i.e. traffic sent from a
   * node to itself).
   */
  @Test
  public void testForbiddenTransitNodes() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setDestinationIpSpaceSpecifierFactory(ConstantUniverseIpSpaceSpecifierFactory.NAME);
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
                allOf(
                    hasIngressNode(NODE1),
                    hasSrcIp(anyOf(equalTo(NODE1_FAST_ETHERNET_IP), equalTo(NODE1_LOOPBACK_IP))),
                    hasDstIp(
                        anyOf(equalTo(NODE1_FAST_ETHERNET_IP), equalTo(NODE1_LOOPBACK_IP)))))));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode(NODE2),
                    hasSrcIp(anyOf(equalTo(NODE2_FAST_ETHERNET_IP), equalTo(NODE2_LOOPBACK_IP))),
                    hasDstIp(
                        anyOf(equalTo(NODE2_FAST_ETHERNET_IP), equalTo(NODE2_LOOPBACK_IP)))))));
  }

  /**
   * If we require some node is transited, then we should only get 1-hop flows (i.e. traffic sent
   * from one node to the other).
   */
  @Test
  public void testRequiredTransitNodes() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    question.setDestinationIpSpaceSpecifierFactory(ConstantUniverseIpSpaceSpecifierFactory.NAME);
    question.setSourceLocationSpecifierFactory(NameRegexInterfaceLocationSpecifierFactory.NAME);
    question.setSourceLocationSpecifierInput(LOOPBACK);
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
                    hasIngressNode(NODE1),
                    hasSrcIp(NODE1_LOOPBACK_IP),
                    hasDstIp(
                        anyOf(equalTo(NODE2_FAST_ETHERNET_IP), equalTo(NODE2_LOOPBACK_IP)))))));
    assertThat(
        flowHistoryInfos,
        hasItem(
            hasFlow(
                allOf(
                    hasIngressNode(NODE2),
                    hasSrcIp(NODE2_LOOPBACK_IP),
                    hasDstIp(
                        anyOf(equalTo(NODE1_FAST_ETHERNET_IP), equalTo(NODE1_LOOPBACK_IP)))))));
  }
}
