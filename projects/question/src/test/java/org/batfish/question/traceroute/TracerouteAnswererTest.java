package org.batfish.question.traceroute;

import static org.batfish.question.traceroute.TracerouteAnswerer.COL_FLOW;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_TRACES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Test;

public class TracerouteAnswererTest {

  private static final String BASE_FLOW_TAG = "BASE";

  private static final String DELTA_FLOW_TAG = "DELTA";

  @AutoService(IpSpaceRepresentative.class)
  public static class MockIpSpaceRepresentative implements IpSpaceRepresentative {

    @Override
    public Optional<Ip> getRepresentative(IpSpace ipSpace) {
      return null;
    }
  }

  static class MockBatfish extends IBatfishTestAdapter {

    @Override
    public String getBaseFlowTag() {
      return BASE_FLOW_TAG;
    }

    @Override
    public String getDeltaFlowTag() {
      return DELTA_FLOW_TAG;
    }

    @Override
    public BatfishLogger getLogger() {
      return null;
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations() {
      return new TreeMap<>();
    }

    @Override
    public SpecifierContext specifierContext() {
      return MockSpecifierContext.builder().setConfigs(loadConfigurations()).build();
    }
  }

  @Test
  public void testDiffFlowHistoryToRows() {

    Flow flow =
        Flow.builder().setTag("tag").setIngressNode("node").setDstIp(new Ip("1.1.1.1")).build();
    Environment environment = new Environment(null, null, null, null, null, null, null, null);

    FlowHistory flowHistory = new FlowHistory();
    flowHistory.addFlowTrace(
        flow,
        BASE_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes1"));
    flowHistory.addFlowTrace(
        flow,
        BASE_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.DENIED_OUT, ImmutableList.of(), "notes2"));
    flowHistory.addFlowTrace(
        flow,
        DELTA_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes1"));
    flowHistory.addFlowTrace(
        flow,
        DELTA_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes2"));

    TracerouteQuestion question = new TracerouteQuestion();
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, new MockBatfish());

    Multiset<Row> rows = answerer.flowHistoryToRows(flowHistory, true);

    assertThat(rows, hasSize(1));

    FlowHistoryInfo flowHistoryInfo = flowHistory.getTraces().get(flow.toString());

    assertThat(
        rows.iterator().next(),
        equalTo(
            Row.of(
                COL_FLOW,
                flow,
                TableDiff.baseColumnName(COL_TRACES),
                flowHistoryInfo.getPaths().get(BASE_FLOW_TAG),
                TableDiff.deltaColumnName(COL_TRACES),
                flowHistoryInfo.getPaths().get(DELTA_FLOW_TAG))));
  }

  @Test
  public void flowHistoryToRow() {

    Set<FlowTrace> traces =
        ImmutableSet.of(
            new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes1"),
            new FlowTrace(FlowDisposition.DENIED_OUT, ImmutableList.of(), "notes2"));
    Flow flow =
        Flow.builder().setTag("tag").setIngressNode("node").setDstIp(new Ip("1.1.1.1")).build();

    FlowHistoryInfo historyInfo =
        new FlowHistoryInfo(
            flow,
            ImmutableMap.of("env", new Environment(null, null, null, null, null, null, null, null)),
            ImmutableMap.of("env", traces));

    TracerouteQuestion question = new TracerouteQuestion();
    TracerouteAnswerer answerer = new TracerouteAnswerer(question, new MockBatfish());

    Row row = answerer.flowHistoryToRow(historyInfo);

    assertThat(
        row,
        equalTo(
            Row.of(
                TracerouteAnswerer.COL_FLOW,
                flow,
                TracerouteAnswerer.COL_TRACES,
                historyInfo.getPaths().values().stream().findAny())));
  }
}
