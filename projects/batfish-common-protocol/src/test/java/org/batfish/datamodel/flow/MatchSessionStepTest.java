package org.batfish.datamodel.flow;

import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.ip.Ip;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.batfish.datamodel.transformation.IpField;
import org.junit.Test;

/** Test for {@link MatchSessionStep}. */
public final class MatchSessionStepTest {
  @Test
  public void testConstructor() {
    Set<String> incomingInterfaces = ImmutableSet.of("a");
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(Accept.INSTANCE)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    assertEquals(step.getAction(), StepAction.MATCHED_SESSION);
    assertEquals(step.getDetail().getIncomingInterfaces(), incomingInterfaces);
    assertEquals(step.getDetail().getSessionAction(), Accept.INSTANCE);
    assertEquals(step.getDetail().getMatchCriteria(), matchCriteria);
    assertEquals(step.getDetail().getTransformation(), transformation);
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Set<String> incomingInterfaces = ImmutableSet.of("b");
    ForwardOutInterface forwardAction =
        new ForwardOutInterface("a", NodeInterfacePair.of("a", "b"));
    SessionMatchExpr matchCriteria =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, null);
    Set<FlowDiff> transformation =
        ImmutableSet.of(flowDiff(IpField.SOURCE, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")));
    MatchSessionStep step =
        new MatchSessionStep(
            MatchSessionStepDetail.builder()
                .setIncomingInterfaces(incomingInterfaces)
                .setSessionAction(forwardAction)
                .setMatchCriteria(matchCriteria)
                .setTransformation(transformation)
                .build());
    MatchSessionStep clone = BatfishObjectMapper.clone(step, MatchSessionStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertThat(
        clone.getDetail().getIncomingInterfaces(),
        equalTo(step.getDetail().getIncomingInterfaces()));
    assertThat(clone.getDetail().getSessionAction(), equalTo(step.getDetail().getSessionAction()));
    assertThat(clone.getDetail().getMatchCriteria(), equalTo(step.getDetail().getMatchCriteria()));
    assertThat(
        clone.getDetail().getTransformation(), equalTo(step.getDetail().getTransformation()));
  }
}
