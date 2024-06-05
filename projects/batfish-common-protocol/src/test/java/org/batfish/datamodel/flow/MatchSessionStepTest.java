package org.batfish.datamodel.flow;

import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.batfish.datamodel.transformation.IpField;
import org.junit.Test;

/** Test for {@link MatchSessionStep}. */
public final class MatchSessionStepTest {
  @Test
  public void testConstructor() {
    SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of("a"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setSessionScope(sessionScope)
                .setSessionAction(Accept.INSTANCE)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);
    assertEquals(step.getDetail().getSessionScope(), sessionScope);
    assertEquals(step.getDetail().getSessionAction(), Accept.INSTANCE);
    assertEquals(step.getDetail().getMatchCriteria(), matchCriteria);
    assertEquals(step.getDetail().getTransformation(), transformation);
  }

  @Test
  public void testJsonSerialization() {
    SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of("b"));
    ForwardOutInterface forwardAction =
        new ForwardOutInterface("a", NodeInterfacePair.of("a", "b"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setSessionScope(sessionScope)
                .setSessionAction(forwardAction)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    MatchSessionStep clone = BatfishObjectMapper.clone(step, MatchSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertThat(clone.getDetail().getSessionScope(), equalTo(step.getDetail().getSessionScope()));
    assertThat(clone.getDetail().getSessionAction(), equalTo(step.getDetail().getSessionAction()));
    assertThat(clone.getDetail().getMatchCriteria(), equalTo(step.getDetail().getMatchCriteria()));
    assertThat(
        clone.getDetail().getTransformation(), equalTo(step.getDetail().getTransformation()));
  }

  @Test
  public void testEquals() {
    SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of("a"));
    SessionScope sessionScope2 = new IncomingSessionScope(ImmutableSet.of("b"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    SessionMatchExpr matchCriteria2 =
        new SessionMatchExpr(IpProtocol.OSPF, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    Set<FlowDiff> transformation2 =
        ImmutableSet.of(flowDiff(IpField.DESTINATION, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new MatchSessionStep(
                MatchSessionStepDetail.builder()
                    .setSessionScope(sessionScope)
                    .setSessionAction(Accept.INSTANCE)
                    .setMatchCriteria(matchCriteria)
                    .setTransformation(transformation)
                    .build()),
            new MatchSessionStep(
                MatchSessionStepDetail.builder()
                    .setSessionScope(sessionScope)
                    .setSessionAction(Accept.INSTANCE)
                    .setMatchCriteria(matchCriteria)
                    .setTransformation(transformation)
                    .build()))
        .addEqualityGroup(
            new MatchSessionStep(
                MatchSessionStepDetail.builder()
                    .setSessionScope(sessionScope2)
                    .setSessionAction(Accept.INSTANCE)
                    .setMatchCriteria(matchCriteria)
                    .setTransformation(transformation)
                    .build()))
        .addEqualityGroup(
            new MatchSessionStep(
                MatchSessionStepDetail.builder()
                    .setSessionScope(sessionScope2)
                    .setSessionAction(PostNatFibLookup.INSTANCE)
                    .setMatchCriteria(matchCriteria)
                    .setTransformation(transformation)
                    .build()))
        .addEqualityGroup(
            new MatchSessionStep(
                MatchSessionStepDetail.builder()
                    .setSessionScope(sessionScope2)
                    .setSessionAction(PostNatFibLookup.INSTANCE)
                    .setMatchCriteria(matchCriteria2)
                    .setTransformation(transformation)
                    .build()))
        .addEqualityGroup(
            new MatchSessionStep(
                MatchSessionStepDetail.builder()
                    .setSessionScope(sessionScope2)
                    .setSessionAction(PostNatFibLookup.INSTANCE)
                    .setMatchCriteria(matchCriteria2)
                    .setTransformation(transformation2)
                    .build()))
        .testEquals();
  }
}
