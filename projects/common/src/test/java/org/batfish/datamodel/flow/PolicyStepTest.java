package org.batfish.datamodel.flow;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.PolicyStep.PolicyStepDetail;
import org.junit.jupiter.api.Test;

/** Tests of {@link PolicyStep} */
class PolicyStepTest {
  @Test
  void testJsonSerialization() {
    PolicyStep ps = new PolicyStep(new PolicyStepDetail("pol"), StepAction.PERMITTED);
    // Don't trow
    BatfishObjectMapper.clone(ps, PolicyStep.class);
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new PolicyStep(new PolicyStepDetail("pol"), StepAction.PERMITTED),
            new PolicyStep(new PolicyStepDetail("pol"), StepAction.PERMITTED))
        .addEqualityGroup(new PolicyStep(new PolicyStepDetail("pol1"), StepAction.PERMITTED))
        .addEqualityGroup(new PolicyStep(new PolicyStepDetail("pol1"), StepAction.DENIED))
        .testEquals();
  }
}
