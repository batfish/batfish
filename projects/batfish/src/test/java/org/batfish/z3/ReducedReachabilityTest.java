package org.batfish.z3;

import static org.batfish.datamodel.matchers.FlowHistoryInfoMatchers.hasFlow;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.batfish.main.BatfishTestUtils.parseTextConfigs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Batfish;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.IntersectionLocationSpecifier;
import org.batfish.specifier.NameRegexInterfaceLocationSpecifier;
import org.batfish.specifier.NameRegexNodeSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLocationSpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ReducedReachabilityTest {
  private static final String BASE = "base";
  private static final String DELTA = "delta";
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final Ip NODE1_LOOPBACK_IP = new Ip("1.1.1.1");
  private static final Ip NODE2_LOOPBACK_IP = new Ip("2.2.2.2");
  private static final String TESTRIGS_PREFIX =
      "org/batfish/grammar/cisco/testrigs/reduced-reachability/";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);
  private static final String REMOVED_STATIC_ROUTE_TESTRIG_NAME = "removed-static-route";

  @Rule public final ExpectedException _exception = ExpectedException.none();
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish initBatfish(String testrig) throws IOException {
    SortedMap<String, Configuration> baseConfigs =
        parseTextConfigs(_folder, configFiles(testrig, BASE));
    SortedMap<String, Configuration> deltaConfigs =
        parseTextConfigs(_folder, configFiles(testrig, DELTA));
    Batfish batfish = getBatfish(baseConfigs, deltaConfigs, _folder);

    batfish.pushBaseEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    batfish.pushDeltaEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    return batfish;
  }

  private static String[] configFiles(String testrig, String version) {
    return TESTRIG_NODE_NAMES
        .stream()
        .map(name -> String.format("%s/%s/%s/configs/%s", TESTRIGS_PREFIX, testrig, version, name))
        .collect(ImmutableList.toImmutableList())
        .toArray(new String[0]);
  }

  /** Test that we detect reduced reachability caused by a removed static route. */
  @Test
  public void testStaticRouteRemoved() throws IOException {
    Batfish batfish = initBatfish(REMOVED_STATIC_ROUTE_TESTRIG_NAME);
    AnswerElement answer =
        batfish.reducedReachability(
            ReachabilityParameters.builder()
                .setActions(ImmutableSortedSet.of(ForwardingAction.ACCEPT))
                .setFinalNodesSpecifier(new NameRegexNodeSpecifier(Pattern.compile(NODE2)))
                .setHeaderSpace(
                    HeaderSpace.builder().setDstIps(NODE2_LOOPBACK_IP.toIpSpace()).build())
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
        contains(hasFlow(allOf(hasSrcIp(NODE1_LOOPBACK_IP), hasDstIp(NODE2_LOOPBACK_IP)))));
  }
}
