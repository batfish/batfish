package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class LoopStepTest {
  @Test
  public void testJsonSerialization() {
    {
      LoopStep clone = BatfishObjectMapper.clone(LoopStep.INSTANCE, LoopStep.class);
      assertEquals(clone, LoopStep.INSTANCE);
    }

    {
      LoopStep clone = (LoopStep) BatfishObjectMapper.clone(LoopStep.INSTANCE, Step.class);
      assertEquals(clone, LoopStep.INSTANCE);
    }
  }
}
