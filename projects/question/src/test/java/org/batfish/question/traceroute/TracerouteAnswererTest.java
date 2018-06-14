package org.batfish.question.traceroute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class TracerouteAnswererTest {

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
                TracerouteAnswerer.COL_NODE,
                new Node("node"),
                TracerouteAnswerer.COL_DST_IP,
                flow.getDstIp(),
                TracerouteAnswerer.COL_FLOW,
                flow,
                TracerouteAnswerer.COL_NUM_PATHS,
                2,
                TracerouteAnswerer.COL_RESULTS,
                ImmutableSet.of("ACCEPTED", "DENIED_OUT"),
                TracerouteAnswerer.COL_PATHS,
                traces)));
  }
}
