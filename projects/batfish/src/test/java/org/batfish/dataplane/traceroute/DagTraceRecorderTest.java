package org.batfish.dataplane.traceroute;

import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasReverseFlow;
import static org.batfish.datamodel.matchers.TraceAndReverseFlowMatchers.hasTrace;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.matchers.TraceMatchers.hasHops;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.HopTestUtils;
import org.batfish.dataplane.traceroute.DagTraceRecorder.NodeKey;
import org.junit.Test;

public class DagTraceRecorderTest {
  private static final Flow TEST_FLOW =
      Flow.builder().setDstIp(Ip.parse("1.1.1.1")).setIngressNode("node").build();

  private static Breadcrumb breadcrumb(String node, Flow flow) {
    return new Breadcrumb(node, "vrf", null, flow);
  }

  private static HopInfo acceptedHop(String node) {
    return HopInfo.successHop(
        HopTestUtils.acceptedHop(node),
        TEST_FLOW,
        FlowDisposition.ACCEPTED,
        TEST_FLOW,
        null,
        breadcrumb(node, TEST_FLOW));
  }

  private static HopInfo forwardedHop(String node) {
    return forwardedHop(node, "vrf", TEST_FLOW);
  }

  private static HopInfo forwardedHop(String node, String vrf, Flow flow) {
    return HopInfo.forwardedHop(
        HopTestUtils.forwardedHop(node, vrf), flow, breadcrumb(node, flow), null);
  }

  private static HopInfo loopHop(String node) {
    return loopHop(node, TEST_FLOW);
  }

  private static HopInfo loopHop(String node, Flow flow) {
    return HopInfo.loopHop(HopTestUtils.loopHop(node), flow, breadcrumb(node, flow), null);
  }

  /**
   * Test that a node on a looping path are not reused for paths that do not include the breadcrumb
   * required for detecting the loop.
   *
   * <p>Setup: We have a looping path A -> B -> A
   *
   * <p>We cannot record the partial trace: C -> B
   */
  @Test
  public void testNoReuse_requiredBreadcrumb() {
    HopInfo hopA = forwardedHop("A");
    HopInfo hopB = forwardedHop("B");
    HopInfo hopALoop = loopHop("A");
    HopInfo hopC = forwardedHop("C");
    DagTraceRecorder recorder = new DagTraceRecorder(TEST_FLOW);
    assertTrue(recorder.tryRecordPartialTrace(ImmutableList.of(hopA, hopB, hopALoop)));
    assertFalse(recorder.tryRecordPartialTrace(ImmutableList.of(hopC, hopB)));
  }

  /**
   * Test nodes along a non-looping path are not reused for paths that include a breadcrumb that
   * would cause a loop to be detected.
   *
   * <p>Setup: We have a non-looping path A -> B -> C -> D
   *
   * <p>We cannot record the partial trace: C -> B
   */
  @Test
  public void testNoReuse_forbiddenBreadcrumb() {
    Flow flow = Flow.builder().setDstIp(Ip.parse("1.1.1.1")).setIngressNode("node").build();
    HopInfo hopA = forwardedHop("A");
    HopInfo hopB = forwardedHop("B");
    HopInfo hopC = forwardedHop("C");
    HopInfo hopD = acceptedHop("D");
    DagTraceRecorder recorder = new DagTraceRecorder(flow);
    assertTrue(recorder.tryRecordPartialTrace(ImmutableList.of(hopA, hopB, hopC, hopD)));
    assertFalse(recorder.tryRecordPartialTrace(ImmutableList.of(hopC, hopB)));
  }

  /**
   * Test nodes constructed with one flow cannot be reused for other flows.
   *
   * <p>Setup: We have a non-looping path A -> B -> C
   *
   * <p>D transforms the flow, so we cannot record the partial trace: D -> B
   */
  @Test
  public void testNoReuse_transformedFlow() {
    Flow transformedFlow = TEST_FLOW.toBuilder().setDstIp(Ip.parse("2.2.2.2")).build();
    HopInfo hopA = forwardedHop("A");
    HopInfo hopB = forwardedHop("B");
    HopInfo hopC = acceptedHop("C");
    HopInfo hopD = forwardedHop("D");
    HopInfo hopBTransformed = forwardedHop("B", "vrf", transformedFlow);
    DagTraceRecorder recorder = new DagTraceRecorder(TEST_FLOW);
    assertTrue(recorder.tryRecordPartialTrace(ImmutableList.of(hopA, hopB, hopC)));
    assertFalse(recorder.tryRecordPartialTrace(ImmutableList.of(hopD, hopBTransformed)));
  }

  @Test
  public void testRecordPartialTrace() {
    HopInfo hopA = forwardedHop("A");
    HopInfo hopB = forwardedHop("B");
    HopInfo hopC = acceptedHop("C");
    HopInfo hopD = forwardedHop("D");
    DagTraceRecorder recorder = new DagTraceRecorder(TEST_FLOW);
    assertTrue(recorder.tryRecordPartialTrace(ImmutableList.of(hopA, hopB, hopC)));
    assertTrue(recorder.tryRecordPartialTrace(ImmutableList.of(hopD, hopB)));
  }

  /**
   * Test that trace order is preserved. This is important for making TraceDag transparent to users.
   */
  @Test
  public void testTraceOrder() {
    HopInfo hopA1 = forwardedHop("A1");
    HopInfo hopA2 = forwardedHop("A2");
    HopInfo hopB = forwardedHop("B");
    HopInfo hopC1 = acceptedHop("C1");
    HopInfo hopC2 = acceptedHop("C2");
    DagTraceRecorder recorder = new DagTraceRecorder(TEST_FLOW);
    // simulating the sequence of calls FlowTracer will make
    assertFalse(recorder.tryRecordPartialTrace(ImmutableList.of(hopA1)));
    assertFalse(recorder.tryRecordPartialTrace(ImmutableList.of(hopA1, hopB)));
    recorder.recordTrace(ImmutableList.of(hopA1, hopB, hopC1));
    recorder.recordTrace(ImmutableList.of(hopA1, hopB, hopC2));
    assertFalse(recorder.tryRecordPartialTrace(ImmutableList.of(hopA2)));
    assertTrue(recorder.tryRecordPartialTrace(ImmutableList.of(hopA2, hopB)));
    TraceDag dag = recorder.build();
    assertEquals(5, dag.countNodes());
    assertEquals(4, dag.countEdges());
    assertThat(
        dag.getTraces().collect(ImmutableList.toImmutableList()),
        contains(
            // A1 -> B -> C1
            allOf(
                hasTrace(
                    allOf(
                        hasDisposition(FlowDisposition.ACCEPTED),
                        hasHops(contains(hopA1.getHop(), hopB.getHop(), hopC1.getHop())))),
                hasReverseFlow(hopC1.getReturnFlow())),
            // A1 -> B -> C2
            allOf(
                hasTrace(
                    allOf(
                        hasDisposition(FlowDisposition.ACCEPTED),
                        hasHops(contains(hopA1.getHop(), hopB.getHop(), hopC2.getHop())))),
                hasReverseFlow(hopC2.getReturnFlow())),
            // A2 -> B -> C1
            allOf(
                hasTrace(
                    allOf(
                        hasDisposition(FlowDisposition.ACCEPTED),
                        hasHops(contains(hopA2.getHop(), hopB.getHop(), hopC1.getHop())))),
                hasReverseFlow(hopC1.getReturnFlow())),
            // A2 -> B -> C2
            allOf(
                hasTrace(
                    allOf(
                        hasDisposition(FlowDisposition.ACCEPTED),
                        hasHops(contains(hopA2.getHop(), hopB.getHop(), hopC2.getHop())))),
                hasReverseFlow(hopC2.getReturnFlow()))));
  }

  @Test
  public void testNodeKeyEquals() {
    Flow flow1 = TEST_FLOW;
    Flow flow2 = TEST_FLOW.toBuilder().setDstIp(Ip.parse("2.2.2.2")).build();
    Hop hop1 = HopTestUtils.acceptedHop("hop1");
    Hop hop2 = HopTestUtils.acceptedHop("hop2");
    new EqualsTester()
        .addEqualityGroup(new NodeKey(flow1, hop1), new NodeKey(flow1, hop1))
        .addEqualityGroup(new NodeKey(flow2, hop1))
        .addEqualityGroup(new NodeKey(flow1, hop2))
        .testEquals();
  }
}
