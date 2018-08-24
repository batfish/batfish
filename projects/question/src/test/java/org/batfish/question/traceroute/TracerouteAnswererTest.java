package org.batfish.question.traceroute;

import static org.batfish.question.traceroute.TracerouteAnswerer.COL_FLOW;
import static org.batfish.question.traceroute.TracerouteAnswerer.COL_TRACES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableDiff;
import org.junit.Test;

public class TracerouteAnswererTest {

  @Test
  public void testDiffFlowHistoryToRows() {

    Flow flow =
        Flow.builder().setTag("tag").setIngressNode("node").setDstIp(new Ip("1.1.1.1")).build();
    Environment environment = new Environment(null, null, null, null, null, null, null, null);

    FlowHistory flowHistory = new FlowHistory();
    flowHistory.addFlowTrace(
        flow,
        Flow.BASE_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes1"));
    flowHistory.addFlowTrace(
        flow,
        Flow.BASE_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.DENIED_OUT, ImmutableList.of(), "notes2"));
    flowHistory.addFlowTrace(
        flow,
        Flow.DELTA_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes1"));
    flowHistory.addFlowTrace(
        flow,
        Flow.DELTA_FLOW_TAG,
        environment,
        new FlowTrace(FlowDisposition.ACCEPTED, ImmutableList.of(), "notes2"));

    Multiset<Row> rows = TracerouteAnswerer.flowHistoryToRows(flowHistory, true);

    assertThat(rows, hasSize(1));

    FlowHistoryInfo flowHistoryInfo = flowHistory.getTraces().get(flow.toString());

    assertThat(
        rows.iterator().next(),
        equalTo(
            Row.of(
                COL_FLOW,
                flow,
                TableDiff.baseColumnName(COL_TRACES),
                flowHistoryInfo.getPaths().get(Flow.BASE_FLOW_TAG),
                TableDiff.deltaColumnName(COL_TRACES),
                flowHistoryInfo.getPaths().get(Flow.DELTA_FLOW_TAG))));
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

    Row row = TracerouteAnswerer.flowHistoryToRow(historyInfo);

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
