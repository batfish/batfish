package org.batfish.datamodel.flow;

import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;
import org.batfish.datamodel.transformation.IpField;
import org.junit.Test;

/** Test for {@link SetupSessionStep}. */
public final class SetupSessionStepTest {
  @Test
  public void testConstructor() {
    SessionScope sessionScope = new IncomingSessionScope(ImmutableSet.of("a"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    SetupSessionStep step =
        new SetupSessionStep(
            SetupSessionStepDetail.builder()
                .setSessionScope(sessionScope)
                .setSessionAction(Accept.INSTANCE)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    assertEquals(step.getAction(), StepAction.SETUP_SESSION);
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
    SetupSessionStep step =
        new SetupSessionStep(
            SetupSessionStepDetail.builder()
                .setSessionScope(sessionScope)
                .setSessionAction(forwardAction)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    SetupSessionStep clone = BatfishObjectMapper.clone(step, SetupSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(clone.getDetail().getSessionScope(), step.getDetail().getSessionScope());
    assertEquals(clone.getDetail().getSessionAction(), step.getDetail().getSessionAction());
    assertEquals(clone.getDetail().getMatchCriteria(), step.getDetail().getMatchCriteria());
    assertEquals(clone.getDetail().getTransformation(), step.getDetail().getTransformation());
  }
}
