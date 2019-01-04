package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link TransformationEvaluator}. */
public class TransformationEvaluatorTest {

  Flow.Builder _flowBuilder;

  @Before
  public void setup() {
    _flowBuilder = Flow.builder().setIngressNode("node").setTag("tag");
  }

  private static Flow eval(Transformation transformation, Flow flow) {
    return TransformationEvaluator.eval(
            transformation, flow, "iface", ImmutableMap.of(), ImmutableMap.of())
        .getOutputFlow();
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
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow.toBuilder().setDstIp(poolIp).build()));

    // test no match
    origFlow = _flowBuilder.setSrcIp(Ip.parse("1.1.1.2")).build();
    transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow));
  }

  @Test
  public void testShiftIpAddressIntoSubnet() {
    Ip dstIp = Ip.parse("1.2.3.4");
    Prefix prefix = Prefix.parse("15.16.0.0/16");

    Transformation transformation = always().apply(shiftDestinationIp(prefix)).build();
    Flow origFlow = _flowBuilder.setDstIp(dstIp).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(
        transformedFlow, equalTo(origFlow.toBuilder().setDstIp(Ip.parse("15.16.3.4")).build()));

    // less trivial example
    dstIp = Ip.parse("1.2.3.4");
    prefix = Prefix.parse("1.2.3.32/27");
    transformation = always().apply(shiftDestinationIp(prefix)).build();
    origFlow = _flowBuilder.setDstIp(dstIp).build();
    transformedFlow = eval(transformation, origFlow);
    assertThat(
        transformedFlow, equalTo(origFlow.toBuilder().setDstIp(Ip.parse("1.2.3.36")).build()));
  }

  @Test
  public void testNoop() {
    Transformation transformation = always().apply(new Noop(SOURCE_NAT)).build();
    Flow origFlow =
        _flowBuilder.setSrcIp(Ip.parse("1.1.1.1")).setDstIp(Ip.parse("2.2.2.2")).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow));
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
    Ip thenIp = Ip.parse("1.1.1.1");
    Transformation andThen = always().apply(assignDestinationIp(thenIp, thenIp)).build();

    // else branch sets destIp = 2.2.2.2
    Ip elseIp = Ip.parse("2.2.2.2");
    Transformation orElse = always().apply(assignDestinationIp(elseIp, elseIp)).build();

    // branch on if destIp is in 5.0.0.0/8
    Prefix matchPrefix = Prefix.parse("5.0.0.0/8");
    Ip transformedSrcIp = Ip.parse("9.9.9.9");
    Transformation transformation =
        when(matchDst(matchPrefix))
            .apply(assignSourceIp(transformedSrcIp, transformedSrcIp))
            .setAndThen(andThen)
            .setOrElse(orElse)
            .build();

    Ip origSrcIp = Ip.parse("8.8.8.8");
    _flowBuilder.setSrcIp(origSrcIp);

    Flow origFlow = _flowBuilder.setSrcIp(origSrcIp).setDstIp(Ip.parse("5.5.5.5")).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(
        transformedFlow, equalTo(_flowBuilder.setSrcIp(transformedSrcIp).setDstIp(thenIp).build()));

    origFlow = _flowBuilder.setDstIp(Ip.parse("6.6.6.6")).build();
    transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(_flowBuilder.setDstIp(elseIp).build()));
  }
}
