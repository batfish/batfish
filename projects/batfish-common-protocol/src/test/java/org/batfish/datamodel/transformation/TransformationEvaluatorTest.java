package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
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
        transformation, flow, "iface", ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testAssignIpAddressFromPool() {
    Ip srcIp = new Ip("1.1.1.1");
    Ip origDstIp = new Ip("2.2.2.2");
    Ip poolIp = new Ip("3.3.3.3");
    Transformation transformation =
        when(matchSrc(srcIp)).apply(assignDestinationIp(poolIp, poolIp)).build();

    _flowBuilder.setSrcIp(srcIp).setDstIp(origDstIp);

    Flow origFlow = _flowBuilder.build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow.toBuilder().setDstIp(poolIp).build()));

    // test no match
    origFlow = _flowBuilder.setSrcIp(new Ip("1.1.1.2")).build();
    transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow));
  }

  @Test
  public void testShiftIpAddressIntoSubnet() {
    Ip dstIp = new Ip("1.2.3.4");
    Prefix prefix = Prefix.parse("15.16.0.0/16");

    Transformation transformation = always().apply(shiftDestinationIp(prefix)).build();
    Flow origFlow = _flowBuilder.setDstIp(dstIp).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(
        transformedFlow, equalTo(origFlow.toBuilder().setDstIp(new Ip("15.16.3.4")).build()));
  }

  @Test
  public void testNoSteps() {
    Transformation transformation = always().apply().build();
    Flow origFlow = _flowBuilder.setSrcIp(new Ip("1.1.1.1")).setDstIp(new Ip("2.2.2.2")).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(origFlow));
  }

  @Test
  public void testMultipleSteps() {
    Ip srcIp = new Ip("1.2.3.4");
    Ip dstIp = new Ip("9.8.7.6");
    Prefix subnet = Prefix.parse("15.16.0.0/16");
    Ip poolIp = new Ip("5.5.5.5");

    // 2 steps transforming different fields
    Transformation transformation =
        always().apply(shiftDestinationIp(subnet), assignSourceIp(poolIp, poolIp)).build();
    Flow origFlow = _flowBuilder.setSrcIp(srcIp).setDstIp(dstIp).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(
        transformedFlow,
        equalTo(origFlow.toBuilder().setSrcIp(poolIp).setDstIp(new Ip("15.16.7.6")).build()));

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
    Ip thenIp = new Ip("1.1.1.1");
    Transformation andThen = always().apply(assignDestinationIp(thenIp, thenIp)).build();

    // else branch sets destIp = 2.2.2.2
    Ip elseIp = new Ip("2.2.2.2");
    Transformation orElse = always().apply(assignDestinationIp(elseIp, elseIp)).build();

    // branch on if destIp is in 5.0.0.0/8
    Prefix matchPrefix = Prefix.parse("5.0.0.0/8");
    Transformation transformation =
        when(matchDst(matchPrefix)).apply().setAndThen(andThen).setOrElse(orElse).build();

    Flow origFlow = _flowBuilder.setDstIp(new Ip("5.5.5.5")).build();
    Flow transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(_flowBuilder.setDstIp(thenIp).build()));

    origFlow = _flowBuilder.setDstIp(new Ip("6.6.6.6")).build();
    transformedFlow = eval(transformation, origFlow);
    assertThat(transformedFlow, equalTo(_flowBuilder.setDstIp(elseIp).build()));
  }
}
