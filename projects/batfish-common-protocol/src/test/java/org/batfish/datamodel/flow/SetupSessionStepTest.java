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
import org.batfish.datamodel.flow.SetupSessionStep.SetupIncomingSessionStepDetail;
import org.batfish.datamodel.flow.SetupSessionStep.SetupOriginationSessionStepDetail;
import org.batfish.datamodel.transformation.IpField;
import org.junit.Test;

/** Test for {@link SetupSessionStep}. */
public final class SetupSessionStepTest {
  @Test
  public void testConstructor() {
    Set<String> incomingInterfaces = ImmutableSet.of("a");
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    SetupSessionStep step =
        new SetupSessionStep(
            SetupIncomingSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(Accept.INSTANCE)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    assertEquals(step.getAction(), StepAction.SETUP_SESSION);
    assertEquals(
        ((SetupIncomingSessionStepDetail) step.getDetail()).getIncomingInterfaces(),
        incomingInterfaces);
    assertEquals(step.getDetail().getSessionAction(), Accept.INSTANCE);
    assertEquals(step.getDetail().getMatchCriteria(), matchCriteria);
    assertEquals(step.getDetail().getTransformation(), transformation);
  }

  @Test
  public void testJsonSerialization_incomingInterfaces() {
    Set<String> incomingInterfaces = ImmutableSet.of("b");
    ForwardOutInterface forwardAction =
        new ForwardOutInterface("a", NodeInterfacePair.of("a", "b"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    SetupSessionStep step =
        new SetupSessionStep(
            SetupIncomingSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(forwardAction)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    SetupSessionStep clone = BatfishObjectMapper.clone(step, SetupSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(
        ((SetupIncomingSessionStepDetail) clone.getDetail()).getIncomingInterfaces(),
        ((SetupIncomingSessionStepDetail) step.getDetail()).getIncomingInterfaces());
    assertEquals(clone.getDetail().getSessionAction(), step.getDetail().getSessionAction());
    assertEquals(clone.getDetail().getMatchCriteria(), step.getDetail().getMatchCriteria());
    assertEquals(clone.getDetail().getTransformation(), step.getDetail().getTransformation());
  }

  @Test
  public void testJsonSerialization_originatingVrf() {
    String originatingVrf = "b";
    ForwardOutInterface forwardAction =
        new ForwardOutInterface("a", NodeInterfacePair.of("a", "b"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    SetupSessionStep step =
        new SetupSessionStep(
            SetupOriginationSessionStepDetail.builder()
                .setOriginatingVrf(originatingVrf)
                .setSessionAction(forwardAction)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    SetupSessionStep clone = BatfishObjectMapper.clone(step, SetupSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(
        ((SetupOriginationSessionStepDetail) clone.getDetail()).getOriginatingVrf(),
        ((SetupOriginationSessionStepDetail) step.getDetail()).getOriginatingVrf());
    assertEquals(clone.getDetail().getSessionAction(), step.getDetail().getSessionAction());
    assertEquals(clone.getDetail().getMatchCriteria(), step.getDetail().getMatchCriteria());
    assertEquals(clone.getDetail().getTransformation(), step.getDetail().getTransformation());
  }
}
