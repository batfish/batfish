package org.batfish.question.specifiers;

import static org.batfish.datamodel.answers.Schema.FLOW;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_FLOW;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.matchers.FlowMatchers;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.traceroute.TracerouteAnswerer;
import org.batfish.specifier.DispositionSpecifier;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link SpecifiersReachabilityQuestion}. */
public class SpecifiersReachabilityTest {
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final Ip NODE1_FAST_ETHERNET_IP = Ip.parse("1.1.1.2");
  private static final Ip NODE1_LOOPBACK_IP = Ip.parse("1.1.1.1");
  private static final Ip NODE2_FAST_ETHERNET_IP = Ip.parse("1.1.1.3");
  private static final Ip NODE2_LOOPBACK_IP = Ip.parse("2.2.2.2");
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "specifiers-reachability";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String ALL = ".*";

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);

    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  /**
   * Test that the results we get with the default source IpSpace specifier ({@link
   * org.batfish.specifier.InferFromLocationIpSpaceSpecifier}) are correct for the network.
   */
  @Test
  public void testInferSrcIpFromLocation() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setPathConstraints(
                PathConstraintsInput.builder()
                    .setStartLocation(String.format("%s[%s]", NODE1, LOOPBACK))
                    .build())
            .build();
    AnswerElement answer =
        new SpecifiersReachabilityAnswerer(question, _batfish).answer(_batfish.getSnapshot());
    assertThat(answer, instanceOf(TableAnswerElement.class));
    TableAnswerElement tableAnswerElement = (TableAnswerElement) answer;
    assertThat(tableAnswerElement.getRowsList().size(), equalTo(1));
    assertThat(
        tableAnswerElement,
        hasRows(
            hasItem(
                hasColumn(
                    COL_FLOW, allOf(hasIngressNode(NODE1), hasSrcIp(NODE1_LOOPBACK_IP)), FLOW))));
  }

  /**
   * Test that we get a result with NO_ROUTE disposition from each interface. With the default
   * source IpSpace specifier, the srcIp should be that of the source interface. The dstIp should
   * not be one of those configured in the network.
   */
  @Test
  public void testNoRoute() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setPathConstraints(
                PathConstraintsInput.builder()
                    .setStartLocation(String.format("%s[%s]", NODE1, LOOPBACK))
                    .build())
            .setActions(new DispositionSpecifier(ImmutableSortedSet.of(FlowDisposition.NO_ROUTE)))
            .build();
    AnswerElement answer =
        new SpecifiersReachabilityAnswerer(question, _batfish).answer(_batfish.getSnapshot());
    assertThat(answer, instanceOf(TableAnswerElement.class));
    TableAnswerElement tableAnswerElement = (TableAnswerElement) answer;
    assertThat(tableAnswerElement.getRowsList().size(), equalTo(1));
    assertThat(
        tableAnswerElement,
        hasRows(
            hasItem(
                hasColumn(
                    COL_FLOW,
                    allOf(
                        hasIngressNode(NODE1),
                        hasSrcIp(NODE1_LOOPBACK_IP),
                        not(hasDstIp(NODE1_FAST_ETHERNET_IP)),
                        not(hasDstIp(NODE1_LOOPBACK_IP)),
                        not(hasDstIp(NODE2_FAST_ETHERNET_IP)),
                        not(hasDstIp(NODE2_LOOPBACK_IP))),
                    FLOW))));
  }

  /**
   * If we forbid transiting any node, then we should only get 0-hop flows (i.e. traffic sent from a
   * node to itself).
   */
  @Test
  public void testForbiddenTransitNodes() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setPathConstraints(
                PathConstraintsInput.builder()
                    .setStartLocation(ALL)
                    .setForbiddenLocations(ALL)
                    .build())
            .build();
    AnswerElement answer =
        new SpecifiersReachabilityAnswerer(question, _batfish).answer(_batfish.getSnapshot());
    assertThat(answer, instanceOf(TableAnswerElement.class));
    TableAnswerElement tableAnswerElement = (TableAnswerElement) answer;
    assertThat(tableAnswerElement.getRowsList().size(), equalTo(2));
    assertThat(
        tableAnswerElement,
        hasRows(
            hasItem(
                hasColumn(
                    COL_FLOW,
                    allOf(
                        hasIngressNode(NODE1),
                        hasSrcIp(
                            anyOf(equalTo(NODE1_FAST_ETHERNET_IP), equalTo(NODE1_LOOPBACK_IP))),
                        hasDstIp(
                            anyOf(equalTo(NODE1_FAST_ETHERNET_IP), equalTo(NODE1_LOOPBACK_IP)))),
                    FLOW))));
    assertThat(
        tableAnswerElement,
        hasRows(
            hasItem(
                hasColumn(
                    COL_FLOW,
                    allOf(
                        hasIngressNode(NODE2),
                        hasSrcIp(
                            anyOf(equalTo(NODE2_FAST_ETHERNET_IP), equalTo(NODE2_LOOPBACK_IP))),
                        hasDstIp(
                            anyOf(equalTo(NODE2_FAST_ETHERNET_IP), equalTo(NODE2_LOOPBACK_IP)))),
                    FLOW))));
  }

  /**
   * If we require some node is transited, then we should only get 1-hop flows (i.e. traffic sent
   * from one node to the other).
   */
  @Test
  public void testRequiredTransitNodes() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setPathConstraints(
                PathConstraintsInput.builder()
                    .setStartLocation(String.format("%s[%s]", ALL, LOOPBACK))
                    .setTransitLocations(ALL)
                    .build())
            .build();
    AnswerElement answer =
        new SpecifiersReachabilityAnswerer(question, _batfish).answer(_batfish.getSnapshot());
    assertThat(answer, instanceOf(TableAnswerElement.class));
    TableAnswerElement tableAnswerElement = (TableAnswerElement) answer;
    assertThat(tableAnswerElement.getRowsList().size(), equalTo(2));
    assertThat(
        tableAnswerElement,
        hasRows(
            hasItem(
                hasColumn(
                    COL_FLOW,
                    allOf(
                        hasIngressNode(NODE1),
                        hasSrcIp(NODE1_LOOPBACK_IP),
                        hasDstIp(
                            anyOf(equalTo(NODE2_FAST_ETHERNET_IP), equalTo(NODE2_LOOPBACK_IP)))),
                    FLOW))));
    assertThat(
        tableAnswerElement,
        hasRows(
            hasItem(
                hasColumn(
                    COL_FLOW,
                    allOf(
                        hasIngressNode(NODE2),
                        hasSrcIp(NODE2_LOOPBACK_IP),
                        hasDstIp(
                            anyOf(equalTo(NODE1_FAST_ETHERNET_IP), equalTo(NODE1_LOOPBACK_IP)))),
                    FLOW))));
  }

  /**
   * If we require some node is transited, then we should only get 1-hop flows (i.e. traffic sent
   * from one node to the other).
   */
  @Test
  public void testInvertSearch() {
    // 1. with no constraints, we get some flows, all with port=0
    AnswerElement answer =
        new SpecifiersReachabilityAnswerer(
                SpecifiersReachabilityQuestion.builder().build(), _batfish)
            .answer(_batfish.getSnapshot());
    assertThat(answer, instanceOf(TableAnswerElement.class));
    Set<Flow> flows =
        ((TableAnswerElement) answer)
            .getRowsList().stream()
                .map(row -> row.getFlow(TracerouteAnswerer.COL_FLOW))
                .collect(ImmutableSet.toImmutableSet());
    assertThat(flows, not(empty()));
    assertThat(flows, Matchers.everyItem(FlowMatchers.hasDstIp(NODE1_LOOPBACK_IP)));

    // 2. with the invert search, we get some flows, none with port=0
    answer =
        new SpecifiersReachabilityAnswerer(
                SpecifiersReachabilityQuestion.builder()
                    .setHeaderConstraints(
                        PacketHeaderConstraints.builder()
                            .setDstIp(NODE1_LOOPBACK_IP.toString())
                            .build())
                    .setInvertSearch(true)
                    .build(),
                _batfish)
            .answer(_batfish.getSnapshot());
    assertThat(answer, instanceOf(TableAnswerElement.class));
    flows =
        ((TableAnswerElement) answer)
            .getRowsList().stream()
                .map(row -> row.getFlow(TracerouteAnswerer.COL_FLOW))
                .collect(ImmutableSet.toImmutableSet());
    assertThat(flows, not(empty()));
    assertThat(flows, Matchers.everyItem(FlowMatchers.hasDstIp(not(NODE1_LOOPBACK_IP))));
  }
}
