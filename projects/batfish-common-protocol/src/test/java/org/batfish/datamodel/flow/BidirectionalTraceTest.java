package org.batfish.datamodel.flow;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.BidirectionalTrace.Key;
import org.junit.Test;

/** Tests for {@link BidirectionalTrace}. */
public final class BidirectionalTraceTest {
  @Test
  public void testKeyEquals() {
    Flow flow1 = Flow.builder().setIngressNode("ingressNode").setTag("tag1").build();
    Flow flow2 = Flow.builder().setIngressNode("ingressNode").setTag("tag2").build();
    FirewallSessionTraceInfo session =
        new FirewallSessionTraceInfo("hostname", null, null, ImmutableSet.of(), TRUE, null);
    new EqualsTester()
        .addEqualityGroup(
            new Key(flow1, ImmutableSet.of(), flow1), new Key(flow1, ImmutableSet.of(), flow1))
        .addEqualityGroup(new Key(flow2, ImmutableSet.of(), flow1))
        .addEqualityGroup(new Key(flow1, ImmutableSet.of(session), flow1))
        .addEqualityGroup(new Key(flow1, ImmutableSet.of(), null))
        .testEquals();
  }

  @Test
  public void testKey() {
    Flow flow1 = Flow.builder().setIngressNode("ingressNode").setTag("tag1").build();
    Flow flow2 = Flow.builder().setIngressNode("ingressNode").setTag("tag2").build();
    Trace trace = new Trace(ACCEPTED, ImmutableList.of());
    assertThat(
        new BidirectionalTrace(flow1, trace, ImmutableSet.of(), flow2, trace).getKey(),
        equalTo(new Key(flow1, ImmutableSet.of(), flow2)));
    assertThat(
        new BidirectionalTrace(flow2, trace, ImmutableSet.of(), flow1, trace).getKey(),
        equalTo(new Key(flow2, ImmutableSet.of(), flow1)));
    FirewallSessionTraceInfo session =
        new FirewallSessionTraceInfo("hostname", null, null, ImmutableSet.of(), TRUE, null);
    assertThat(
        new BidirectionalTrace(flow1, trace, ImmutableSet.of(session), null, null).getKey(),
        equalTo(new Key(flow1, ImmutableSet.of(session), null)));
  }
}
