package org.batfish.question.traceroute;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_FORWARD_FLOW;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_FORWARD_TRACES;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_NEW_SESSIONS;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_REVERSE_FLOW;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_REVERSE_TRACES;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.groupTraces;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.toRow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.flow.BidirectionalTrace;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

/** Tests for {@link BidirectionalTracerouteAnswerer}. */
public final class BidirectionalTracerouteAnswererTest {
  private static final Flow FORWARD_FLOW =
      Flow.builder()
          .setDstIp(Ip.parse("1.1.1.1"))
          .setTag("TAG")
          .setIngressNode("forwardIngressNode")
          .setIngressInterface("forwardIngressInterface")
          .build();

  private static final Flow REVERSE_FLOW =
      Flow.builder()
          .setDstIp(Ip.parse("1.1.1.1"))
          .setTag("TAG")
          .setIngressNode("reverseIngressNode")
          .setIngressInterface("reverseIngressInterface")
          .build();

  @Test
  public void testGroupTraces() {
    Trace t1 = new Trace(ACCEPTED, ImmutableList.of());
    Trace t2 = new Trace(EXITS_NETWORK, ImmutableList.of());
    Trace t3 = new Trace(NEIGHBOR_UNREACHABLE, ImmutableList.of());
    Trace t4 = new Trace(DENIED_IN, ImmutableList.of());

    FirewallSessionTraceInfo session1 =
        new FirewallSessionTraceInfo("session1", null, null, ImmutableSet.of(), TRUE, null);
    FirewallSessionTraceInfo session2 =
        new FirewallSessionTraceInfo("session2", null, null, ImmutableSet.of(), TRUE, null);

    {
      // All BidirectionalTraces have the same key, so are in the same group.
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t3);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t4);
      BidirectionalTrace bt3 =
          new BidirectionalTrace(FORWARD_FLOW, t2, ImmutableSet.of(), REVERSE_FLOW, t3);
      BidirectionalTrace bt4 =
          new BidirectionalTrace(FORWARD_FLOW, t2, ImmutableSet.of(), REVERSE_FLOW, t4);

      List<BidirectionalTrace> bts = ImmutableList.of(bt1, bt2, bt3, bt4);
      assertThat(groupTraces(bts), hasEntry(equalTo(bt1.getKey()), equalTo(bts)));
    }

    {
      // Traces with different forward flows are in different groups
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(REVERSE_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);

      assertThat(
          groupTraces(ImmutableList.of(bt1, bt2)),
          equalTo(
              ImmutableMap.of(
                  bt1.getKey(), ImmutableList.of(bt1), bt2.getKey(), ImmutableList.of(bt2))));
    }

    {
      // Traces with different number of sessions are in different groups
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(session1), REVERSE_FLOW, t2);

      assertThat(
          groupTraces(ImmutableList.of(bt1, bt2)),
          equalTo(
              ImmutableMap.of(
                  bt1.getKey(), ImmutableList.of(bt1), bt2.getKey(), ImmutableList.of(bt2))));
    }

    {
      // Traces with different sessions are in different groups
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(session1), REVERSE_FLOW, t2);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(session2), REVERSE_FLOW, t2);

      assertThat(
          groupTraces(ImmutableList.of(bt1, bt2)),
          equalTo(
              ImmutableMap.of(
                  bt1.getKey(), ImmutableList.of(bt1), bt2.getKey(), ImmutableList.of(bt2))));
    }

    {
      // Traces with different reverse flows are in different groups
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), FORWARD_FLOW, t2);

      assertThat(
          groupTraces(ImmutableList.of(bt1, bt2)),
          equalTo(
              ImmutableMap.of(
                  bt1.getKey(), ImmutableList.of(bt1), bt2.getKey(), ImmutableList.of(bt2))));
    }
  }

  @Test
  public void testToRow() {
    Trace t1 = new Trace(ACCEPTED, ImmutableList.of());
    Trace t2 = new Trace(EXITS_NETWORK, ImmutableList.of());

    {
      // Same key, different forward flows
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t2, ImmutableSet.of(), REVERSE_FLOW, t2);

      Row row = toRow(bt1.getKey(), ImmutableList.of(bt1, bt2));
      assertThat(
          row,
          allOf(
              hasColumn(COL_FORWARD_FLOW, equalTo(FORWARD_FLOW), Schema.FLOW),
              hasColumn(COL_FORWARD_TRACES, contains(t1, t2), Schema.list(Schema.TRACE)),
              hasColumn(COL_NEW_SESSIONS, empty(), Schema.list(Schema.STRING)),
              hasColumn(COL_REVERSE_FLOW, equalTo(REVERSE_FLOW), Schema.FLOW),
              hasColumn(COL_REVERSE_TRACES, contains(t2), Schema.list(Schema.TRACE))));
    }

    {
      // Same key, different reverse flows
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t1);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);

      Row row = toRow(bt1.getKey(), ImmutableList.of(bt1, bt2));
      assertThat(
          row,
          allOf(
              hasColumn(COL_FORWARD_FLOW, equalTo(FORWARD_FLOW), Schema.FLOW),
              hasColumn(COL_FORWARD_TRACES, contains(t1), Schema.list(Schema.TRACE)),
              hasColumn(COL_NEW_SESSIONS, empty(), Schema.list(Schema.STRING)),
              hasColumn(COL_REVERSE_FLOW, equalTo(REVERSE_FLOW), Schema.FLOW),
              hasColumn(COL_REVERSE_TRACES, contains(t1, t2), Schema.list(Schema.TRACE))));
    }

    {
      // 4 traces get collapsed to 2x2
      BidirectionalTrace bt1 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t1);
      BidirectionalTrace bt2 =
          new BidirectionalTrace(FORWARD_FLOW, t1, ImmutableSet.of(), REVERSE_FLOW, t2);
      BidirectionalTrace bt3 =
          new BidirectionalTrace(FORWARD_FLOW, t2, ImmutableSet.of(), REVERSE_FLOW, t1);
      BidirectionalTrace bt4 =
          new BidirectionalTrace(FORWARD_FLOW, t2, ImmutableSet.of(), REVERSE_FLOW, t2);

      Row row = toRow(bt1.getKey(), ImmutableList.of(bt1, bt2, bt3, bt4));
      assertThat(
          row,
          allOf(
              hasColumn(COL_FORWARD_FLOW, equalTo(FORWARD_FLOW), Schema.FLOW),
              hasColumn(COL_FORWARD_TRACES, contains(t1, t2), Schema.list(Schema.TRACE)),
              hasColumn(COL_NEW_SESSIONS, empty(), Schema.list(Schema.STRING)),
              hasColumn(COL_REVERSE_FLOW, equalTo(REVERSE_FLOW), Schema.FLOW),
              hasColumn(COL_REVERSE_TRACES, contains(t1, t2), Schema.list(Schema.TRACE))));
    }

    {
      // Sessions in the key
      FirewallSessionTraceInfo session1 =
          new FirewallSessionTraceInfo("session1", null, null, ImmutableSet.of(), TRUE, null);
      FirewallSessionTraceInfo session2 =
          new FirewallSessionTraceInfo("session2", null, null, ImmutableSet.of(), TRUE, null);
      Set<FirewallSessionTraceInfo> sessions = ImmutableSet.of(session1, session2);

      BidirectionalTrace bt = new BidirectionalTrace(FORWARD_FLOW, t1, sessions, REVERSE_FLOW, t1);
      Row row = toRow(bt.getKey(), ImmutableList.of(bt));
      assertThat(
          row,
          allOf(
              hasColumn(COL_FORWARD_FLOW, equalTo(FORWARD_FLOW), Schema.FLOW),
              hasColumn(COL_FORWARD_TRACES, contains(t1), Schema.list(Schema.TRACE)),
              hasColumn(
                  COL_NEW_SESSIONS, contains("session1", "session2"), Schema.list(Schema.STRING)),
              hasColumn(COL_REVERSE_FLOW, equalTo(REVERSE_FLOW), Schema.FLOW),
              hasColumn(COL_REVERSE_TRACES, contains(t1), Schema.list(Schema.TRACE))));
    }

    {
      // No return traces
      Trace t = new Trace(DENIED_IN, ImmutableList.of());
      BidirectionalTrace bt =
          new BidirectionalTrace(FORWARD_FLOW, t, ImmutableSet.of(), null, null);
      Row row = toRow(bt.getKey(), ImmutableList.of(bt));
      assertThat(
          row,
          allOf(
              hasColumn(COL_FORWARD_FLOW, equalTo(FORWARD_FLOW), Schema.FLOW),
              hasColumn(COL_FORWARD_TRACES, contains(t), Schema.list(Schema.TRACE)),
              hasColumn(COL_NEW_SESSIONS, empty(), Schema.list(Schema.STRING)),
              hasColumn(COL_REVERSE_FLOW, nullValue(), Schema.FLOW),
              hasColumn(COL_REVERSE_TRACES, empty(), Schema.list(Schema.TRACE))));
    }
  }
}
