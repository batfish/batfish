package org.batfish.datamodel.flow;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.flow.BidirectionalTracePruner.prune;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.pojo.Node;
import org.junit.Test;

/** A test for {@link BidirectionalTracePruner}. */
public class BidirectionalTracePrunerTest {
  private static final Flow FLOW1;
  private static final Flow FLOW2;
  private static final Flow FLOW3;

  static {
    Flow.Builder fb = Flow.builder();
    FLOW1 = fb.setIngressNode("flow1").build();
    FLOW2 = fb.setIngressNode("flow2").build();
    FLOW3 = fb.setIngressNode("flow3").build();
  }

  private static final Hop HOP1 = new Hop(new Node("hop1"), ImmutableList.of(LoopStep.INSTANCE));
  private static final Hop HOP2 = new Hop(new Node("hop2"), ImmutableList.of(LoopStep.INSTANCE));
  private static final Hop HOP3 = new Hop(new Node("hop3"), ImmutableList.of(LoopStep.INSTANCE));

  /** Test that forward flow values are covered before reverse flow, dispositions, or hops. */
  @Test
  public void testForwardFlow() {
    Trace trace1 = new Trace(ACCEPTED, ImmutableList.of(HOP1));
    Trace trace2 = new Trace(EXITS_NETWORK, ImmutableList.of(HOP2));
    Trace trace3 = new Trace(DELIVERED_TO_SUBNET, ImmutableList.of(HOP3));

    BidirectionalTrace bTrace1 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace2 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW2, trace2);
    BidirectionalTrace bTrace3 =
        new BidirectionalTrace(FLOW1, trace3, ImmutableSet.of(), FLOW3, trace3);
    BidirectionalTrace bTrace4 =
        new BidirectionalTrace(FLOW2, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace5 =
        new BidirectionalTrace(FLOW3, trace1, ImmutableSet.of(), FLOW1, trace1);
    Collection<BidirectionalTrace> pruned =
        prune(ImmutableList.of(bTrace1, bTrace2, bTrace3, bTrace4, bTrace5), 3);
    assertThat(pruned, containsInAnyOrder(bTrace1, bTrace4, bTrace5));
  }

  /** Test that reverse flow values are covered before dispositions or hops. */
  @Test
  public void testReverseFlow() {
    Trace trace1 = new Trace(ACCEPTED, ImmutableList.of(HOP1));
    Trace trace2 = new Trace(EXITS_NETWORK, ImmutableList.of(HOP2));
    Trace trace3 = new Trace(DELIVERED_TO_SUBNET, ImmutableList.of(HOP3));

    BidirectionalTrace bTrace1 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace2 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW1, trace2);
    BidirectionalTrace bTrace3 =
        new BidirectionalTrace(FLOW1, trace3, ImmutableSet.of(), FLOW1, trace3);
    BidirectionalTrace bTrace4 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW2, trace1);
    BidirectionalTrace bTrace5 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW3, trace1);
    Collection<BidirectionalTrace> pruned =
        prune(ImmutableList.of(bTrace1, bTrace2, bTrace3, bTrace4, bTrace5), 3);
    assertThat(pruned, containsInAnyOrder(bTrace1, bTrace4, bTrace5));
  }

  /** Test that forward dispositions are covered before reverse dispositions or hops. */
  @Test
  public void testForwardDisposition() {
    Trace trace1 = new Trace(ACCEPTED, ImmutableList.of(HOP1));
    Trace trace2 = new Trace(ACCEPTED, ImmutableList.of(HOP2));
    Trace trace3 = new Trace(EXITS_NETWORK, ImmutableList.of(HOP3));
    Trace trace4 = new Trace(DELIVERED_TO_SUBNET, ImmutableList.of(HOP3));

    BidirectionalTrace bTrace1 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace2 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW1, trace2);
    BidirectionalTrace bTrace3 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW1, trace3);
    BidirectionalTrace bTrace4 =
        new BidirectionalTrace(FLOW1, trace3, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace5 =
        new BidirectionalTrace(FLOW1, trace4, ImmutableSet.of(), FLOW1, trace1);
    Collection<BidirectionalTrace> pruned =
        prune(ImmutableList.of(bTrace1, bTrace2, bTrace3, bTrace4, bTrace5), 3);
    assertThat(pruned, containsInAnyOrder(bTrace1, bTrace4, bTrace5));
  }

  /** Test that reverse dispositions are covered before hops. */
  @Test
  public void testReverseDisposition() {
    Trace trace1 = new Trace(ACCEPTED, ImmutableList.of(HOP1));
    Trace trace2 = new Trace(ACCEPTED, ImmutableList.of(HOP2));
    Trace trace3 = new Trace(DENIED_IN, ImmutableList.of(HOP3));
    Trace trace4 = new Trace(DENIED_OUT, ImmutableList.of(HOP3));

    BidirectionalTrace bTrace1 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace2 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW1, trace2);
    BidirectionalTrace bTrace3 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace4 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW1, trace3);
    BidirectionalTrace bTrace5 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace4);
    Collection<BidirectionalTrace> pruned =
        prune(ImmutableList.of(bTrace1, bTrace2, bTrace3, bTrace4, bTrace5), 3);
    assertThat(pruned, containsInAnyOrder(bTrace1, bTrace4, bTrace5));
  }

  /** Test that forward hops are covered before reverse hops. */
  @Test
  public void testForwardHops() {
    Trace trace1 = new Trace(ACCEPTED, ImmutableList.of(HOP1));
    Trace trace2 = new Trace(ACCEPTED, ImmutableList.of(HOP2));
    Trace trace3 = new Trace(ACCEPTED, ImmutableList.of(HOP3));

    BidirectionalTrace bTrace1 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace2 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace2);
    BidirectionalTrace bTrace3 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace3);
    BidirectionalTrace bTrace4 =
        new BidirectionalTrace(FLOW1, trace2, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace5 =
        new BidirectionalTrace(FLOW1, trace3, ImmutableSet.of(), FLOW1, trace1);
    Collection<BidirectionalTrace> pruned =
        prune(ImmutableList.of(bTrace1, bTrace2, bTrace3, bTrace4, bTrace5), 3);
    assertThat(pruned, containsInAnyOrder(bTrace1, bTrace4, bTrace5));
  }

  /** Test that reverse hops are covered. */
  @Test
  public void testReverseHops() {
    Trace trace1 = new Trace(ACCEPTED, ImmutableList.of(HOP1));
    Trace trace2 = new Trace(ACCEPTED, ImmutableList.of(HOP2));
    Trace trace3 = new Trace(ACCEPTED, ImmutableList.of(HOP3));

    BidirectionalTrace bTrace1 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace2 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace3 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace1);
    BidirectionalTrace bTrace4 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace2);
    BidirectionalTrace bTrace5 =
        new BidirectionalTrace(FLOW1, trace1, ImmutableSet.of(), FLOW1, trace3);
    Collection<BidirectionalTrace> pruned =
        prune(ImmutableList.of(bTrace1, bTrace2, bTrace3, bTrace4, bTrace5), 3);
    assertThat(pruned, containsInAnyOrder(bTrace1, bTrace4, bTrace5));
  }
}
