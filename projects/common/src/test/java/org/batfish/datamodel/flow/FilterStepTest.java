package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.junit.Test;

public class FilterStepTest {
  @Test
  public void testJsonSerialization() {
    FilterStep step =
        new FilterStep(
            new FilterStepDetail(
                "filter",
                FilterType.EGRESS_FILTER,
                "iface",
                Flow.builder().setIngressNode("node").build()),
            StepAction.DENIED);

    Step<?> clonedGenericStep = BatfishObjectMapper.clone(step, Step.class);
    assertThat(clonedGenericStep, instanceOf(FilterStep.class));
    FilterStep clonedStep = (FilterStep) clonedGenericStep;
    assertThat(clonedStep.getAction(), equalTo(StepAction.DENIED));
    assertThat(clonedStep.getDetail().getFilter(), equalTo("filter"));
    assertThat(clonedStep.getDetail().getType(), equalTo(FilterType.EGRESS_FILTER));
    assertThat(
        clonedStep.getDetail().getFlow(), equalTo(Flow.builder().setIngressNode("node").build()));
    assertThat(clonedStep.getDetail().getInputInterface(), equalTo("iface"));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new FilterStep(
                new FilterStepDetail(
                    "filter",
                    FilterType.EGRESS_FILTER,
                    "iface",
                    Flow.builder().setIngressNode("node").build()),
                StepAction.DENIED),
            new FilterStep(
                new FilterStepDetail(
                    "filter",
                    FilterType.EGRESS_FILTER,
                    "iface",
                    Flow.builder().setIngressNode("node").build()),
                StepAction.DENIED))
        .addEqualityGroup(
            new FilterStep(
                new FilterStepDetail(
                    "filter1",
                    FilterType.EGRESS_FILTER,
                    "iface",
                    Flow.builder().setIngressNode("node").build()),
                StepAction.DENIED))
        .addEqualityGroup(
            new FilterStep(
                new FilterStepDetail(
                    "filter1",
                    FilterType.INGRESS_FILTER,
                    "iface",
                    Flow.builder().setIngressNode("node").build()),
                StepAction.DENIED))
        .addEqualityGroup(
            new FilterStep(
                new FilterStepDetail(
                    "filter1",
                    FilterType.INGRESS_FILTER,
                    "iface1",
                    Flow.builder().setIngressNode("node").build()),
                StepAction.DENIED))
        .addEqualityGroup(
            new FilterStep(
                new FilterStepDetail(
                    "filter1",
                    FilterType.INGRESS_FILTER,
                    "iface1",
                    Flow.builder().setIngressNode("node1").build()),
                StepAction.DENIED))
        .addEqualityGroup(
            new FilterStep(
                new FilterStepDetail(
                    "filter1",
                    FilterType.INGRESS_FILTER,
                    "iface1",
                    Flow.builder().setIngressNode("node1").build()),
                StepAction.PERMITTED))
        .testEquals();
  }
}
