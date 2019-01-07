package org.batfish.question.multipath;

import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstPort;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIcmpCode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIcmpType;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressVrf;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIpProtocol;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasTag;
import static org.batfish.datamodel.matchers.FlowTraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_FLOW;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_TRACES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.oneOf;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.bddreachability.TestNetwork;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.matchers.TraceMatchers;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** End-to-end tests of {@link MultipathConsistencyQuestion}. */
@RunWith(JUnit4.class)
public class MultipathConsistencyTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private TestNetwork _testNetwork;

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _testNetwork = new TestNetwork();
    _batfish = BatfishTestUtils.getBatfish(_testNetwork._configs, temp);
    _batfish.computeDataPlane(false);
  }

  @Test
  public void testMultipath_oldtraceroute() {
    _batfish.getSettings().setDebugFlags(ImmutableList.of("oldtraceroute"));
    MultipathConsistencyQuestion question = new MultipathConsistencyQuestion();
    MultipathConsistencyAnswerer answerer = new MultipathConsistencyAnswerer(question, _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer();

    assertThat(ae.getRows(), hasSize(1));

    assertThat(
        ae.getRows().getData().iterator().next(),
        hasColumn(
            COL_FLOW,
            allOf(
                ImmutableList.of(
                    hasDstIp(Ip.parse("2.1.0.0")),
                    hasDstPort(1234),
                    hasIcmpCode(0),
                    hasIcmpType(8),
                    hasIngressNode("~Configuration_0~"),
                    hasIngressVrf("default"),
                    hasIpProtocol(IpProtocol.ICMP),
                    hasSrcIp(oneOf(Ip.parse("2.0.0.0"), Ip.parse("1.0.0.0"))),
                    hasTag("BASE"))),
            Schema.FLOW));

    assertThat(
        ae.getRows().getData().iterator().next(),
        hasColumn(
            COL_TRACES,
            containsInAnyOrder(
                ImmutableList.of(
                    hasDisposition(FlowDisposition.DENIED_IN),
                    hasDisposition(FlowDisposition.ACCEPTED))),
            Schema.set(Schema.FLOW_TRACE)));
  }

  @Test
  public void testMultipath() {
    _batfish.getSettings().setDebugFlags(ImmutableList.of());
    MultipathConsistencyQuestion question = new MultipathConsistencyQuestion();
    MultipathConsistencyAnswerer answerer = new MultipathConsistencyAnswerer(question, _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer();

    assertThat(ae.getRows(), hasSize(1));

    Row row = ae.getRows().getData().iterator().next();
    assertThat(
        row,
        hasColumn(
            COL_FLOW,
            allOf(
                ImmutableList.of(
                    hasDstIp(Ip.parse("2.1.0.0")),
                    hasDstPort(1234),
                    hasIcmpCode(0),
                    hasIcmpType(8),
                    hasIngressNode("~Configuration_0~"),
                    hasIngressVrf("default"),
                    hasIpProtocol(IpProtocol.ICMP),
                    hasSrcIp(oneOf(Ip.parse("2.0.0.0"), Ip.parse("1.0.0.0"))),
                    hasTag("BASE"))),
            Schema.FLOW));

    assertThat(
        row,
        hasColumn(
            COL_TRACES,
            containsInAnyOrder(
                ImmutableList.of(
                    TraceMatchers.hasDisposition(FlowDisposition.DENIED_IN),
                    TraceMatchers.hasDisposition(FlowDisposition.ACCEPTED))),
            Schema.set(Schema.TRACE)));
  }
}
