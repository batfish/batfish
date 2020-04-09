package org.batfish.datamodel.flow;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.PolicyStep.PolicyStepDetail;
import org.junit.Test;

/** Tests of {@link PolicyStep} */
public class PolicyStepTest {
  @Test
  public void testJsonSerialization() {
    PolicyStep ps = new PolicyStep(new PolicyStepDetail("pol"), StepAction.PERMITTED);
    // Don't trow
    BatfishObjectMapper.clone(ps, PolicyStep.class);
  }
}
