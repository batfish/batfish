package org.batfish.question.multipath;

import static org.batfish.datamodel.matchers.FlowTraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

import com.sun.tools.javac.util.List;
import java.io.IOException;
import org.batfish.bddreachability.TestNetwork;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link MultipathConsistencyQuestion}. */
@RunWith(JUnit4.class)
public class MultipathConsistencyQuestionTest {
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
  public void testMultipath() {
    MultipathConsistencyQuestion question = new MultipathConsistencyQuestion();
    MultipathConsistencyAnswerer answerer = new MultipathConsistencyAnswerer(question, _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer();

    Flow testFlow =
        Flow.builder()
            .setIngressNode("~Configuration_0~")
            .setIngressVrf("default")
            .setSrcIp(new Ip("2.0.0.0"))
            .setDstIp(new Ip("2.1.0.0"))
            .setIpProtocol(IpProtocol.HOPOPT)
            .setState(State.NEW)
            .setTag("BASE")
            .setDstPort(1234)
            .setIcmpCode(0)
            .setIcmpType(0)
            .build();

    assertThat(
        ae.getRows().getData(), everyItem(hasColumn("flow", equalTo(testFlow), Schema.FLOW)));

    assertThat(
        ae.getRows().getData(),
        everyItem(
            hasColumn(
                "traces",
                containsInAnyOrder(
                    List.of(
                        hasDisposition(FlowDisposition.DENIED_IN),
                        hasDisposition(FlowDisposition.ACCEPTED))),
                Schema.set(Schema.FLOW_TRACE))));
  }
}
