package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.flow.StepAction.PERMITTED;
import static org.batfish.datamodel.flow.StepAction.TRANSFORMED;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.transformation.TransformationEvaluator.TransformationResult;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link TransformationEvaluator}. */
public class TransformationEvaluatorTest {

  Flow.Builder _flowBuilder;

  @Before
  public void setup() {
    _flowBuilder =
        Flow.builder()
            .setIngressNode("node")
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(0)
            .setDstPort(0);
  }

  private static Flow eval(Transformation transformation, Flow flow) {
    return evalResult(transformation, flow).getOutputFlow();
  }

  private static TransformationResult evalResult(Transformation transformation, Flow flow) {
    return TransformationEvaluator.eval(
        transformation, flow, "iface", ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testAssignIpAddressFromPool() {
    Ip srcIp = Ip.parse("1.1.1.1");
    Ip origDstIp = Ip.parse("2.2.2.2");
    Ip poolIp = Ip.parse("3.3.3.3");
    Transformation transformation =
        when(matchSrc(srcIp)).apply(assignDestinationIp(poolIp, poolIp)).build();

    _flowBuilder.setSrcIp(srcIp).setDstIp(origDstIp);

    Flow origFlow = _flowBuilder.build();
    TransformationResult result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow.toBuilder().setDstIp(poolIp).build()));

    List<Step<?>> traceSteps = result.getTraceSteps();
    assertThat(traceSteps, hasSize(1));
    assertThat(
        traceSteps.get(0),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT,
                    ImmutableSortedSet.of(flowDiff(IpField.DESTINATION, origDstIp, poolIp))),
                TRANSFORMED)));

    // test no match
    origFlow = _flowBuilder.setSrcIp(Ip.parse("1.1.1.2")).build();
    result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow));
    traceSteps = result.getTraceSteps();
    assertThat(traceSteps, hasSize(0));
  }

  @Test
  public void testShiftIpAddressIntoSubnet() {
    Ip origDstIp = Ip.parse("1.2.3.4");
    Prefix prefix = Prefix.parse("15.16.0.0/16");

    Transformation transformation = always().apply(shiftDestinationIp(prefix)).build();
    Flow origFlow = _flowBuilder.setDstIp(origDstIp).build();
    TransformationResult result = evalResult(transformation, origFlow);
    Ip transformedDstIp = Ip.parse("15.16.3.4");
    assertThat(
        result.getOutputFlow(), equalTo(origFlow.toBuilder().setDstIp(transformedDstIp).build()));
    List<Step<?>> steps = result.getTraceSteps();
    assertThat(steps, hasSize(1));
    assertThat(
        steps.get(0),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT,
                    ImmutableSortedSet.of(
                        flowDiff(IpField.DESTINATION, origDstIp, transformedDstIp))),
                TRANSFORMED)));

    // less trivial example
    origDstIp = Ip.parse("1.2.3.4");
    prefix = Prefix.parse("1.2.3.32/27");
    transformation = always().apply(shiftDestinationIp(prefix)).build();
    origFlow = _flowBuilder.setDstIp(origDstIp).build();
    result = evalResult(transformation, origFlow);
    assertThat(
        result.getOutputFlow(),
        equalTo(origFlow.toBuilder().setDstIp(Ip.parse("1.2.3.36")).build()));
  }

  @Test
  public void testNoop() {
    Transformation transformation = always().apply(new Noop(SOURCE_NAT)).build();
    Flow origFlow =
        _flowBuilder.setSrcIp(Ip.parse("1.1.1.1")).setDstIp(Ip.parse("2.2.2.2")).build();
    TransformationResult result = evalResult(transformation, origFlow);
    assertThat(
        "Noop transformations should return the original (==) flow",
        result.getOutputFlow(),
        sameInstance(origFlow));

    assertThat(
        result.getTraceSteps(),
        contains(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(SOURCE_NAT, ImmutableSortedSet.of()),
                StepAction.PERMITTED)));
  }

  @Test
  public void testMultipleSteps() {
    Ip srcIp = Ip.parse("1.2.3.4");
    Ip dstIp = Ip.parse("9.8.7.6");
    Prefix subnet = Prefix.parse("15.16.0.0/16");
    Ip poolIp = Ip.parse("5.5.5.5");

    // 2 steps transforming different fields
    Transformation transformation =
        always().apply(shiftDestinationIp(subnet), assignSourceIp(poolIp, poolIp)).build();
    Flow origFlow = _flowBuilder.setSrcIp(srcIp).setDstIp(dstIp).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(
        transformedFlow,
        equalTo(origFlow.toBuilder().setSrcIp(poolIp).setDstIp(Ip.parse("15.16.7.6")).build()));

    // 2 steps transforming the same field
    transformation =
        always().apply(shiftDestinationIp(subnet), assignDestinationIp(poolIp, poolIp)).build();
    origFlow = _flowBuilder.setSrcIp(srcIp).setDstIp(dstIp).build();
    transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow.toBuilder().setDstIp(poolIp).build()));
  }

  @Test
  public void testBranching() {
    // then branch sets destIp = 1.1.1.1
    Ip thenDstIp = Ip.parse("1.1.1.1");
    Transformation andThen = always().apply(assignDestinationIp(thenDstIp, thenDstIp)).build();

    // else branch sets destIp = 2.2.2.2
    Ip elseDstIp = Ip.parse("2.2.2.2");
    Transformation orElse = always().apply(assignDestinationIp(elseDstIp, elseDstIp)).build();

    // branch on if destIp is in 5.0.0.0/8
    Prefix matchPrefix = Prefix.parse("5.0.0.0/8");
    Ip transformedSrcIp = Ip.parse("9.9.9.9");
    Transformation transformation =
        when(matchDst(matchPrefix))
            .apply(assignSourceIp(transformedSrcIp, transformedSrcIp))
            .setAndThen(andThen)
            .setOrElse(orElse)
            .build();

    Ip origDstIp = Ip.parse("5.5.5.5");
    Ip origSrcIp = Ip.parse("8.8.8.8");
    Flow origFlow = _flowBuilder.setDstIp(origDstIp).setSrcIp(origSrcIp).build();

    TransformationResult result = evalResult(transformation, origFlow);
    assertThat(
        result.getOutputFlow(),
        equalTo(_flowBuilder.setSrcIp(transformedSrcIp).setDstIp(thenDstIp).build()));

    List<Step<?>> steps = result.getTraceSteps();
    assertThat(steps, hasSize(2));

    assertThat(
        steps.get(0),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    SOURCE_NAT,
                    ImmutableSortedSet.of(flowDiff(SOURCE, origSrcIp, transformedSrcIp))),
                TRANSFORMED)));
    assertThat(
        steps.get(1),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT, ImmutableSortedSet.of(flowDiff(DESTINATION, origDstIp, thenDstIp))),
                TRANSFORMED)));

    origDstIp = Ip.parse("6.6.6.6");
    origFlow = _flowBuilder.setDstIp(origDstIp).build();
    result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(_flowBuilder.setDstIp(elseDstIp).build()));

    steps = result.getTraceSteps();
    assertThat(steps, hasSize(1));

    assertThat(
        steps.get(0),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT, ImmutableSortedSet.of(flowDiff(DESTINATION, origDstIp, elseDstIp))),
                TRANSFORMED)));
  }

  @Test
  public void testTransformedTwice() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Transformation transformation =
        always()
            .apply(assignDestinationIp(ip2, ip2))
            .setAndThen(always().apply(assignDestinationIp(ip3, ip3)).build())
            .build();

    TransformationResult result = evalResult(transformation, _flowBuilder.setDstIp(ip1).build());

    assertThat(result.getOutputFlow(), equalTo(_flowBuilder.setDstIp(ip3).build()));

    List<Step<?>> steps = result.getTraceSteps();

    assertThat(steps, hasSize(2));
    assertThat(
        steps.get(0),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT, ImmutableSortedSet.of(flowDiff(IpField.DESTINATION, ip1, ip2))),
                TRANSFORMED)));
    assertThat(
        steps.get(1),
        equalTo(
            new org.batfish.datamodel.flow.TransformationStep(
                new TransformationStepDetail(
                    DEST_NAT, ImmutableSortedSet.of(flowDiff(IpField.DESTINATION, ip2, ip3))),
                TRANSFORMED)));
  }

  @Test
  public void testAssignPortFromPoolDst() {
    Ip srcIp = Ip.parse("1.1.1.1");
    int dstPort = 3000;
    int poolPort = 2000;
    Transformation transformation =
        when(matchSrc(srcIp)).apply(assignDestinationPort(poolPort, poolPort)).build();

    _flowBuilder.setSrcIp(srcIp).setDstPort(dstPort);

    // the flow is transformed
    Flow origFlow = _flowBuilder.build();
    TransformationResult result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow.toBuilder().setDstPort(poolPort).build()));

    List<Step<?>> traceSteps = result.getTraceSteps();
    TransformationStep step =
        new TransformationStep(
            new TransformationStepDetail(
                DEST_NAT,
                ImmutableSortedSet.of(flowDiff(PortField.DESTINATION, dstPort, poolPort))),
            TRANSFORMED);
    assertThat(traceSteps, contains(step));

    // the flow is not transformed
    origFlow = _flowBuilder.setDstPort(poolPort).build();
    result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow.toBuilder().setDstPort(poolPort).build()));

    traceSteps = result.getTraceSteps();
    step =
        new TransformationStep(
            new TransformationStepDetail(DEST_NAT, ImmutableSortedSet.of()), PERMITTED);
    assertThat(traceSteps, contains(step));
  }

  @Test
  public void testAssignPortFromPoolSrc() {
    Ip srcIp = Ip.parse("1.1.1.1");
    int srcPort = 3000;
    int poolPort = 2000;
    Transformation transformation =
        when(matchSrc(srcIp)).apply(assignSourcePort(poolPort, poolPort)).build();

    _flowBuilder.setSrcIp(srcIp).setSrcPort(srcPort);

    // the flow is transformed
    Flow origFlow = _flowBuilder.build();
    TransformationResult result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow.toBuilder().setSrcPort(poolPort).build()));

    List<Step<?>> traceSteps = result.getTraceSteps();
    TransformationStep step =
        new TransformationStep(
            new TransformationStepDetail(
                SOURCE_NAT, ImmutableSortedSet.of(flowDiff(PortField.SOURCE, srcPort, poolPort))),
            TRANSFORMED);
    assertThat(traceSteps, contains(step));

    // the flow is not transformed
    origFlow = _flowBuilder.setSrcPort(poolPort).build();
    result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow.toBuilder().setSrcPort(poolPort).build()));

    traceSteps = result.getTraceSteps();
    step =
        new TransformationStep(
            new TransformationStepDetail(SOURCE_NAT, ImmutableSortedSet.of()), PERMITTED);
    assertThat(traceSteps, contains(step));
  }

  @Test
  public void testAssignPortFromPoolSrc_nonPortProtocol() {
    Transformation transformation = always().apply(assignSourcePort(2000, 2000)).build();

    Flow origFlow =
        _flowBuilder.setIpProtocol(IpProtocol.ICMP).setIcmpCode(0).setIcmpType(1).build();

    // the flow is not transformed
    TransformationResult result = evalResult(transformation, origFlow);
    assertThat(result.getOutputFlow(), equalTo(origFlow));

    List<Step<?>> traceSteps = result.getTraceSteps();
    TransformationStep step =
        new TransformationStep(
            new TransformationStepDetail(SOURCE_NAT, ImmutableSortedSet.of()), PERMITTED);
    assertThat(traceSteps, contains(step));
  }
}
